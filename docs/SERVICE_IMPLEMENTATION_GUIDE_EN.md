# Service Implementation Guide

Follow this guide to create services with `BaseService<Request, Response>`,
validate input, run business operations, and call them from controllers.

## A. Before You Start

Prepare the `core`, `common`, and runtime `foundation-starter` dependencies at
version `1.1.0` as described in the [Configuration Guide](CONFIGURATION_GUIDE_EN.md).
Provide a `ValidationUtility` bean through `ValidationConfig.builder()` or enable
`reyga.config.default-bean.validation-handler=true`. The DTO examples use Lombok;
enable annotation processing in the application project.

Create DTOs first, implement the service, then connect the controller.
Start with the non-servlet case; use the servlet case only when business logic
requires direct access to the HTTP request or response.

Complete examples include class declarations. Add method snippets in later
sections to the appropriate application classes; domain types such as
`CustomerData` and `CustomerRepository` are supplied by the application.

## B. Library Components Used

| Component | Responsibility |
|---|---|
| `BaseRequest` | Base request DTO and temporary storage for `HttpServletRequest` and `HttpServletResponse`. |
| `FoundationService<T, R>` | Base contract declaring `R execute(T request)`. |
| `BaseService<T, R>` | Service lifecycle template that already implements `FoundationService<T, R>`. |
| `BaseHttpServletBuilder` | Helpers for attaching or reading servlet objects on `BaseRequest`. |
| `BaseController` | Base controller with servlet helpers and a standard response builder. |
| `ValidationUtility` | Programmatic Jakarta Bean Validation within a service. |

Since `BaseService` already implements `FoundationService`, application services
do not need to add `implements FoundationService`. The basic form is:

```java
@Service
public class FindCustomerService extends BaseService<FindCustomerRequest, FindCustomerResponse> {

    @Override
    protected boolean useHttpServletParameter() {
        return false;
    }

    @Override
    protected void validateRequest(FindCustomerRequest request) {
        // validation
    }

    @Override
    protected FindCustomerResponse buildProcess(FindCustomerRequest request) {
        throw new UnsupportedOperationException("Implement application business logic here");
    }
}
```

## C. The `execute()` Lifecycle

Controllers must call `execute()` as the only service entry point:

```text
Controller
  -> creates or receives a request DTO
  -> attaches servlet context if required by the service
  -> service.execute(request)
       -> checks servlet context if required
       -> validateRequest(request)
       -> buildProcess(request)
  -> builds the HTTP response
```

Do not call `validateRequest()` or `buildProcess()` directly from a controller.
Direct calls bypass parts of the `BaseService` lifecycle.

| Required method | Responsibility |
|---|---|
| `useHttpServletParameter()` | Return `true` only when the business flow reads the servlet request/response. |
| `validateRequest(request)` | Validate request fields and business preconditions before calling primary dependencies. |
| `buildProcess(request)` | Assemble and execute the business flow, then return the response. |

`BaseService` is used as a singleton Spring bean. Do not store requests,
responses, query results, or servlet objects in service fields. Use local
variables for each execution.

## D. Recommended Package Structure

```text
customer/
├── controller/
│   └── CustomerController.java
├── repository/
│   └── CustomerRepository.java
└── service/
    ├── find/
    │   ├── FindCustomerRequest.java
    │   ├── FindCustomerResponse.java
    │   └── FindCustomerService.java
    └── update/
        ├── UpdateCustomerRequest.java
        ├── UpdateCustomerResponse.java
        └── UpdateCustomerService.java
```

Use one service class per business flow. Names such as `FindCustomerService`,
`CreateCustomerService`, and `UpdateCustomerService` are clearer than a single
large class containing every customer operation.

## E. Services Without Servlet Parameters

Use this case when the business flow only needs DTO data. The service does not
read headers, cookies, sessions, remote addresses, or the servlet response.

### Request and response DTOs

All service requests must extend `BaseRequest`:

```java
package com.example.customer.service.find;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import reyga.starter.foundation.common.model.dto.request.BaseRequest;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class FindCustomerRequest extends BaseRequest {

    @NotBlank
    private String customerId;
}
```

```java
package com.example.customer.service.find;

public record FindCustomerResponse(
        String customerId,
        String name,
        String status
) {
}
```

Avoid using database entities directly as requests or responses so business/API
contracts are not tied to the persistence schema.

### Implement the service

```java
package com.example.customer.service.find;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reyga.starter.foundation.core.service.base.BaseService;
import reyga.starter.foundation.core.validation.ValidationUtility;

@Service
@RequiredArgsConstructor
public class FindCustomerService extends BaseService<FindCustomerRequest, FindCustomerResponse> {

    private final CustomerRepository customerRepository;
    private final ValidationUtility validationUtility;

    @Override
    protected boolean useHttpServletParameter() {
        return false;
    }

    @Override
    protected void validateRequest(FindCustomerRequest request) {
        validationUtility.validateRequest(request);
    }

    @Override
    protected FindCustomerResponse buildProcess(FindCustomerRequest request) {
        CustomerData customer = customerRepository
                .findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Customer not found: " + request.getCustomerId()
                ));

        return new FindCustomerResponse(
                customer.id(),
                customer.name(),
                customer.status()
        );
    }
}
```

`CustomerRepository` and `CustomerData` are application contracts. Use custom
exceptions matching the application's error standard in production code.

### Controller with a JSON body

```java
package com.example.customer.controller;

import com.example.customer.service.find.FindCustomerRequest;
import com.example.customer.service.find.FindCustomerResponse;
import com.example.customer.service.find.FindCustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reyga.starter.foundation.common.model.dto.response.ResponseData;
import reyga.starter.foundation.core.controller.BaseController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController extends BaseController {

    private final FindCustomerService findCustomerService;

    @PostMapping("/find")
    public ResponseEntity<ResponseData<FindCustomerResponse>> find(@Valid @RequestBody FindCustomerRequest request) {
        return createResponse(findCustomerService.execute(request), HttpStatus.OK, "200000", "Customer retrieved successfully");
    }
}
```

### Controller with path or query parameters

The controller constructs the service DTO, then calls `execute()`:

```java
@GetMapping("/{customerId}")
public ResponseEntity<ResponseData<FindCustomerResponse>> findById(@PathVariable String customerId) {
    FindCustomerRequest request = FindCustomerRequest.builder()
            .customerId(customerId)
            .build();

    return createResponse(findCustomerService.execute(request), HttpStatus.OK, "200000", "Customer retrieved successfully");
}
```

```java
@GetMapping
public ResponseEntity<ResponseData<FindCustomerResponse>> findByQuery(@RequestParam String customerId) {
    FindCustomerRequest request = FindCustomerRequest.builder()
            .customerId(customerId)
            .build();

    return createResponse(findCustomerService.execute(request), HttpStatus.OK, "200000", "Customer retrieved successfully");
}
```

Using `@RequestBody`, `@PathVariable`, or `@RequestParam` does not imply that the
service needs servlet parameters. The value remains `false` as long as the
service does not read `HttpServletRequest` or `HttpServletResponse`.

## F. Services with Servlet Parameters

Use this case only when the business flow needs HTTP context, such as reading
headers, cookies, sessions, request attributes, client information, or adding
response headers.

When `useHttpServletParameter()` returns `true`, `BaseService` requires
**both objects**:

- `HttpServletRequest` in `BaseRequest.servletRequest`;
- `HttpServletResponse` in `BaseRequest.servletResponse`.

Even if the service only reads one of them, the controller must attach both.
If either is `null`, `execute()` throws `AppFaultException` with code
`SERVLET_CONTEXT_NOT_FOUND` and HTTP `500` before logging, validation, and
business processing begin.

### Request and response DTOs

```java
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class UpdateCustomerRequest extends BaseRequest {

    @NotBlank
    private String customerId;

    @NotBlank
    private String name;
}
```

```java
public record UpdateCustomerResponse(
        String customerId,
        String name,
        String updatedBy
) {
}
```

Servlet fields are inherited from `BaseRequest` and are `transient`. Do not
redeclare servlet objects in derived DTOs.

### Implement the service

```java
@Service
@RequiredArgsConstructor
public class UpdateCustomerService extends BaseService<UpdateCustomerRequest, UpdateCustomerResponse> {

    private final CustomerRepository customerRepository;
    private final ValidationUtility validationUtility;

    @Override
    protected boolean useHttpServletParameter() {
        return true;
    }

    @Override
    protected void validateRequest(UpdateCustomerRequest request) {
        validationUtility.validateRequest(request);

        String actor = request.getServletRequest().getHeader("X-User-Id");
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException("X-User-Id header is required");
        }
    }

    @Override
    protected UpdateCustomerResponse buildProcess(UpdateCustomerRequest request) {
        String actor = request.getServletRequest().getHeader("X-User-Id");

        CustomerData customer = customerRepository.updateName(
                request.getCustomerId(),
                request.getName(),
                actor
        );

        request.getServletResponse().setHeader("X-Updated-By", actor);

        return new UpdateCustomerResponse(
                customer.id(),
                customer.name(),
                actor
        );
    }
}
```

The `X-User-Id` example only demonstrates reading a header. For user identity,
use an authenticated principal or a header verified by the authentication
system; do not trust arbitrary client headers.

Do not store actors or servlet objects in service fields. Process HTTP metadata
using local variables or stateless private helpers.

### Controller using `BaseController`

`BaseController` inherits `setHttpServletParameter()` from
`BaseHttpServletBuilder`. Call this helper before `execute()`:

```java
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerCommandController extends BaseController {

    private final UpdateCustomerService updateCustomerService;

    @PutMapping("/{customerId}")
    public ResponseEntity<ResponseData<UpdateCustomerResponse>> update(
            @PathVariable String customerId,
            @Valid @RequestBody UpdateCustomerHttpRequest body,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        UpdateCustomerRequest request = UpdateCustomerRequest.builder()
                .customerId(customerId)
                .name(body.name())
                .build();

        setHttpServletParameter(request, servletRequest, servletResponse);

        return createResponse(updateCustomerService.execute(request), HttpStatus.OK, "200000", "Customer updated successfully");
    }

    public record UpdateCustomerHttpRequest(@NotBlank String name) {
    }
}
```

The correct order is:

```text
build request DTO
  -> setHttpServletParameter(request, servletRequest, servletResponse)
  -> updateCustomerService.execute(request)
  -> createResponse(...)
```

### Controller without `BaseController`

Servlet context can be attached using `BaseRequest` setters:

```java
request.setServletRequest(servletRequest);
request.setServletResponse(servletResponse);

UpdateCustomerResponse result = updateCustomerService.execute(request);
return ResponseEntity.ok(result);
```

Always call both setters.

### Multipart requests

`MultipartFile` is not the servlet parameter referred to by
`useHttpServletParameter()`. If the service only needs the file and DTO metadata,
it still returns `false`:

```java
@PostMapping(path = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<ResponseData<UploadDocumentResponse>> upload(@RequestPart MultipartFile file, @RequestParam String customerId)
{
    UploadDocumentRequest request = UploadDocumentRequest.builder()
            .customerId(customerId)
            .file(file)
            .build();

    UploadDocumentResponse result = uploadDocumentService.execute(request);

    return createResponse(result, HttpStatus.CREATED, "201000", "Document uploaded successfully");
}
```

Change this to `true` and attach both servlet objects only when the service also
needs HTTP context beyond the file itself.

## G. Calling Services from Schedulers or Listeners

Schedulers, message listeners, batch jobs, and command-line runners have no
servlet context. These services must use `useHttpServletParameter() == false`:

```java
@Component
@RequiredArgsConstructor
public class CustomerSynchronizationJob {

    private final SynchronizeCustomerService synchronizeCustomerService;

    @Scheduled(cron = "${customer.sync.cron}")
    public void synchronize() {
        SynchronizeCustomerRequest request = SynchronizeCustomerRequest.builder()
                .source("SCHEDULER")
                .build();

        synchronizeCustomerService.execute(request);
    }
}
```

If a business flow is used by both HTTP and non-HTTP callers, do not create mock
servlet objects for non-HTTP callers. Separate the HTTP adapter from the domain
service that accepts ordinary data.

## H. Creating Services Without Business Input

`execute()` still requires a request instance extending `BaseRequest`. Do not
pass `null`; create an explicit empty request:

```java
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class GetSystemStatusRequest extends BaseRequest {
}
```

```java
@GetMapping("/status")
public ResponseEntity<ResponseData<SystemStatusResponse>> status() {
    SystemStatusResponse result = getSystemStatusService.execute(new GetSystemStatusRequest());

    return createResponse(result, HttpStatus.OK, "200000", "System status retrieved successfully");
}
```

If this empty service is servlet-aware, attach both servlet objects before
calling `execute()`.

## I. Controller Case Summary

| Input/caller | `useHttpServletParameter()` | Before `execute()` |
|---|---:|---|
| JSON body without servlet access | `false` | Call `execute(request)` directly. |
| Path/query parameters without servlet access | `false` | Construct a DTO, then call `execute(request)`. |
| Multipart without additional servlet access | `false` | Construct a DTO containing the file, then call `execute(request)`. |
| Reading headers/cookies/sessions/request attributes | `true` | Attach the servlet request and response. |
| Writing servlet responses/headers | `true` | Attach the servlet request and response. |
| Scheduler/listener/batch | `false` | Construct a DTO without servlet objects, then call `execute(request)`. |

## J. Standard Controller Responses

`BaseController` provides two response forms:

```java
// Direct body
return createResponse(result, HttpStatus.OK);

// Standard ResponseData<T> envelope
return createResponse(result, HttpStatus.OK,"200000","Operation completed successfully");
```

Business services return response DTOs. HTTP status, success code, and success
message remain the controller's responsibility. Standardized business exceptions
can be passed to the foundation/application exception handler.

## K. Preparing Service Validation

Inject `ValidationUtility` through the constructor and call
`validationUtility.validateRequest(request)` in `validateRequest()` as shown
above. Choose annotations for the DTO's needs, such as `@FieldPresence` for
null/blank checks or `@FieldFormat` for format validation.

Manual/YAML bean configuration, validation groups, and annotation references
are available in the [Utilities Guide](UTILITIES_GUIDE_EN.md).
Controller `@Valid` applies only to HTTP paths; service validation also protects
calls from schedulers and listeners.

## L. Dependency Injection and Transactions

Use constructor injection. Do not make concrete services `final` when the
application needs class-based Spring proxies for transactions or aspects.
Avoid self-invocation for methods that rely on Spring AOP because calls through
`this` do not pass through the proxy.

## M. Testing Application Services

Mock `ValidationUtility` and the repository dependency, then inject both into
the service constructor during unit tests. The example below assumes fixtures
and mocks have already been prepared.

Follow the `should_ExpectedOutput_When_InputCondition` naming pattern and use
`given`, `when`, and `then` sections.

### Services without servlet parameters

- `should_ReturnResponse_When_RequestIsValid`
- `should_ThrowException_When_RequestIsInvalid`
- `should_ThrowException_When_DataIsNotFound`
- `should_ExecuteWithoutServletContext_When_ServletParameterIsNotRequired`

### Services with servlet parameters

- `should_ReturnResponse_When_RequestAndServletContextAreValid`
- `should_ThrowAppFaultException_When_ServletRequestIsNull`
- `should_ThrowAppFaultException_When_ServletResponseIsNull`
- `should_ThrowException_When_RequiredHeaderIsMissing`

In the `then` section, verify the complete response/exception and dependency interactions:

```java
@Test
void should_ReturnResponse_When_RequestAndServletContextAreValid() {
    // given
    UpdateCustomerRequest request = validRequest();
    request.setServletRequest(servletRequest);
    request.setServletResponse(servletResponse);
    when(servletRequest.getHeader("X-User-Id")).thenReturn("user-01");
    when(customerRepository.updateName("CUS-01", "Reyga", "user-01"))
            .thenReturn(updatedCustomer());

    // when
    UpdateCustomerResponse result = service.execute(request);

    // then
    assertThat(result.customerId()).isEqualTo("CUS-01");
    assertThat(result.name()).isEqualTo("Reyga");
    assertThat(result.updatedBy()).isEqualTo("user-01");
    verify(servletRequest, times(2)).getHeader("X-User-Id");
    verify(servletResponse).setHeader("X-Updated-By", "user-01");
    verify(customerRepository).updateName("CUS-01", "Reyga", "user-01");
    verify(validationUtility).validateRequest(request);
    verifyNoMoreInteractions(validationUtility, customerRepository, servletRequest, servletResponse);
}
```

For missing servlet context, ensure business dependencies are not called:

```java
verifyNoInteractions(customerRepository);
```

## N. Implementation Checklist

- Requests extend `BaseRequest`.
- Concrete services extend `BaseService<Request, Response>` directly.
- Services have `@Service` and use constructor injection.
- All three lifecycle methods are implemented.
- `useHttpServletParameter()` is `true` only for dependencies on HTTP context.
- Controllers attach **both servlet request and response** before `execute()`
  for servlet-aware services.
- Controllers call only `execute()`, not internal lifecycle methods.
- Controllers build HTTP status and response envelopes.
- Services do not store per-request state or servlet objects in fields.
- A `ValidationUtility` bean is available through manual configuration or YAML.
- Positive and negative tests verify results and dependency interactions.

## O. Troubleshooting

| Symptom | Cause and action |
|---|---|
| `Servlet Context Not Found` | The service returns `true`, but one servlet object is missing. Call `setHttpServletParameter()` before `execute()`. |
| `ValidationUtility bean is not available` | Provide a `ValidationUtility` bean manually or enable `reyga.config.default-bean.validation-handler=true`. |
| Service bean not found | Ensure the service has `@Service` and is included in component scanning. |
| Servlet data mixed between requests | Do not store servlet objects or request DTOs in singleton service fields. |
| Business flow bypasses the lifecycle | Ensure callers use `execute()`, not internal service methods. |
| Non-HTTP callers always fail | Ensure non-HTTP services return `false` from `useHttpServletParameter()`. |

More controller examples are available in the
[Controller Implementation Guide](CONTROLLER_IMPLEMENTATION_GUIDE_EN.md).