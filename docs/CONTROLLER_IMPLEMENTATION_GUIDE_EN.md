# Controller Implementation Guide

This guide provides controller implementation examples using the
`BaseController` and `ResilienceBaseController` base classes.

For end-to-end examples covering request DTOs, service implementations, servlet
parameters, and calling `execute()` from a controller, see the
[Service Implementation Guide](SERVICE_IMPLEMENTATION_GUIDE_EN.md).

When an application service extends `BaseService`, the controller must call
`service.execute(request)` as the business flow entry point. If the service
returns `true` from `useHttpServletParameter()`, call
`setHttpServletParameter(request, servletRequest, servletResponse)` immediately
before `execute()`.

## A. Using `BaseController`

`BaseController` is the standard choice for REST controllers. It provides helpers
for building consistent JSON responses.

### Main features

- `createResponse(T data, HttpStatus status)` creates a simple `ResponseEntity`
  containing the supplied data and HTTP status.
- `createResponse(T data, HttpStatus status, String code, String message)` creates
  a `ResponseEntity<ResponseData<T>>` containing status, code, message, and data.
  Use this form when the application uses a standard response envelope.

The JSON property order for `ResponseData` is `status`, `code`, `message`, then
`data`. This ordering is part of the library's response contract and applies
even when the application enables alphabetical Jackson property ordering.

### Step 1: Prepare the service and DTOs

Use `FindCustomerService`, `FindCustomerRequest`, and `FindCustomerResponse`
from the [Service Guide](SERVICE_IMPLEMENTATION_GUIDE_EN.md).
That service does not require a servlet context.

### Step 2: Create the controller and call execute

```java
package com.example.customer.controller;

import com.example.customer.service.find.FindCustomerRequest;
import com.example.customer.service.find.FindCustomerResponse;
import com.example.customer.service.find.FindCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reyga.starter.foundation.common.model.dto.response.ResponseData;
import reyga.starter.foundation.core.controller.BaseController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController extends BaseController {

    private final FindCustomerService findCustomerService;

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<FindCustomerResponse>> find(@PathVariable String id) {
        FindCustomerRequest request = FindCustomerRequest.builder().customerId(id).build();
        FindCustomerResponse result = findCustomerService.execute(request);
        return createResponse(result, HttpStatus.OK, "00", "Customer found successfully");
    }
}
```

For a JSON body, accept `@Valid @RequestBody FindCustomerRequest request`, then
call `findCustomerService.execute(request)`. For query parameters, use
`@RequestParam` and construct a DTO as in the path parameter example above.

### Step 3: Attach the servlet context when needed

For an endpoint calling a service with `useHttpServletParameter() == true`,
accept `HttpServletRequest` and `HttpServletResponse` as controller method
parameters. Attach both before execution:

```java
setHttpServletParameter(request, servletRequest, servletResponse);
var result = updateCustomerService.execute(request);
return createResponse(result, HttpStatus.OK, "00", "Customer updated successfully");
```

Use servlet types from `jakarta.servlet.http`. Complete DTO and endpoint examples
are available in the servlet-aware service section of the service guide.

## B. Using `ResilienceBaseController`

`ResilienceBaseController` extends `BaseController` and adds Resilience4j
capabilities, such as Rate Limiter and Circuit Breaker, to response creation.

> Helper behavior: every overload accepts `data`, rather than `Supplier<T>`.
> This means the business service has already completed before the resilience
> helper runs. Helpers can select a success or fallback response, but do not
> prevent earlier repository, HTTP client, or business operations from executing.
> Use `ResilienceService` in the service layer when the downstream operation
> itself needs protection.

### Prerequisites

To use `ResilienceBaseController` with the library's default beans, enable the
following configuration:

```yaml
reyga:
  config:
    default-bean:
      utilities: true
    rate-limiter:
      required: true
      timeout-milis: 500
      max-request: 10
      refresh-period-seconds: 60
    circuit-breaker:
      required: true
      failure-rate-threshold: 50
      slow-call-rate-threshold: 50
      slow-call-duration-threshold-seconds: 2
      minimum-number-of-calls: 10
      sliding-window-size: 20
      permitted-number-of-calls-in-half-open-state: 5
      wait-duration-in-open-state-seconds: 30
      automatic-transition-from-open-to-half-open-enabled: true
```

This configuration creates the following beans in the Spring application context:

- `ResilienceService`;
- `RateLimiterConfig`;
- `RateLimiterRegistry`;
- `CircuitBreakerRegistry`.

`RateLimiterConfig` is built from all `rate-limiter` properties, and
`RateLimiterRegistry` uses it as the default configuration. The example grants
each limiter 10 permissions per 60 seconds and allows it to wait up to
500 milliseconds for a permission.

Although the example controller only uses a rate limiter,
`circuit-breaker.required` is enabled because the current
`ResilienceBaseController` constructor always requires `CircuitBreakerRegistry`.
Also provide an application transaction manager when using `utilities: true`.

Property details and bounds are described in the
[Foundation Configuration Guide](CONFIGURATION_GUIDE_EN.md).

### Main features

- `createResponseWithRateLimiter(...)` builds a response through a Rate Limiter.
  If no permission is available, it returns HTTP `429 Too Many Requests`;
  the data passed to the method has already been processed.
- `createResponseWithCircuitBreaker(...)` builds a response through a Circuit
  Breaker. When the circuit is open, it creates an HTTP `500` fallback; this
  helper does not wrap the business service call that produced the data.

### Rate Limiter example using YAML configuration

Inject the `RateLimiterConfig` available after rate limiter configuration is
enabled. The controller does not need to call `RateLimiterConfig.custom()` or
repeat values from YAML:

```java
package com.example.controller;

import com.example.customer.service.find.FindCustomerRequest;
import com.example.customer.service.find.FindCustomerResponse;
import com.example.customer.service.find.FindCustomerService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reyga.starter.foundation.common.model.dto.response.ResponseData;
import reyga.starter.foundation.core.controller.ResilienceBaseController;
import reyga.starter.foundation.core.service.foundation.ResilienceService;

@RestController
@RequestMapping("/api/rate-limited-customers")
public class RateLimitedCustomerController extends ResilienceBaseController {

    private final FindCustomerService findCustomerService;
    private final RateLimiterConfig rateLimiterConfig;

    public RateLimitedCustomerController(
            ResilienceService resilienceService,
            RateLimiterRegistry rateLimiterRegistry,
            CircuitBreakerRegistry circuitBreakerRegistry,
            RateLimiterConfig rateLimiterConfig,
            FindCustomerService findCustomerService
    ) {
        super(resilienceService, rateLimiterRegistry, circuitBreakerRegistry);
        this.rateLimiterConfig = rateLimiterConfig;
        this.findCustomerService = findCustomerService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<FindCustomerResponse>> getCustomerById(
            @PathVariable String id
    ) {
        // The business service runs before the rate limiter helper.
        FindCustomerRequest request = FindCustomerRequest.builder().customerId(id).build();
        FindCustomerResponse result = findCustomerService.execute(request);

        return createResponseWithRateLimiter(result, "00", "Customer found successfully", rateLimiterConfig, "customer-get-by-id");
    }
}
```

Use a stable, unique `rlKey` for each policy, such as `customer-get-by-id`.
Calls with the same key use the same limiter instance from the registry.
Different keys create separate limiter instances, but all still use the same
default `RateLimiterConfig`.

The YAML above supplies global rate limiter configuration. If an endpoint needs
a different limit, the application must explicitly provide additional
configuration or use `ResilienceService` in the service layer.

## C. Controller Implementation Checklist

- Extend `BaseController` or `ResilienceBaseController` as needed.
- Inject service dependencies through constructors.
- For `BaseService`, call only `execute(request)` as the business entry point.
- Attach servlet request and response to servlet-aware services through
  `setHttpServletParameter()` before `execute()`.
- Build HTTP status, success code, message, and response envelope in the controller.
- Keep business logic, repository access, and domain response mapping in the service.
- Configure Rate Limiter or Circuit Breaker with unique keys and parameters
  appropriate for the application's SLA.