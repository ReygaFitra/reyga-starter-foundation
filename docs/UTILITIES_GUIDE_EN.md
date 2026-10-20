# Utilities Guide

This guide explains the library's public utilities for applications. Complete
feature flag configuration is in the [Configuration Guide](CONFIGURATION_GUIDE_EN.md).

## A. Validation

### A.1. Usage concept

`ValidationUtility` runs programmatic Jakarta Bean Validation from services.
Typically, validation is called in `validateRequest()` within `BaseService`:

```java
@Service
@RequiredArgsConstructor
public class CreateCustomerService
        extends BaseService<CreateCustomerRequest, CreateCustomerResponse> {

    private final ValidationUtility validationUtility;

    @Override
    protected void validateRequest(CreateCustomerRequest request) {
        validationUtility.validateRequest(request);
    }

    @Override
    protected CreateCustomerResponse buildProcess(CreateCustomerRequest request) {
        // Run business logic.
        return new CreateCustomerResponse();
    }

    @Override
    protected boolean useHttpServletParameter() {
        return false;
    }
}
```

Service examples and controller `execute()` calls are in the
[Service Implementation Guide](SERVICE_IMPLEMENTATION_GUIDE_EN.md).

### A.2. Required validation handler configuration

To use the library's validation handler, including beans created with
`.useDefaultBehavior()`, enable this configuration:

```yaml
reyga:
  config:
    default-bean:
      validation-handler: true
```

The flag serves two purposes:

- Enables `DefaultValidationExceptionHandler` to convert `ValidationFaultException`
  to standard HTTP validation responses.
- Provides a default `ValidationUtility` bean when the application has none.

Manual `ValidationUtility` beans select validation behavior but do not remove the
requirement to enable the YAML handler. Without the flag, provide your own HTTP exception handler.

`reyga.config.default-bean.exception-handler` may be `false`. The validation
handler remains active with `validation-handler: true`:

```yaml
reyga:
  config:
    default-bean:
      exception-handler: false
      validation-handler: true
```

### A.3. Using default behavior

For all YAML defaults, inject `ValidationUtility` directly; the library creates
it if no application bean exists.

To define a bean explicitly, use `ValidationConfig`:

```java
@Configuration(proxyBeanMethods = false)
public class ApplicationValidationConfig {

    @Bean
    public ValidationUtility validationUtility() {
        return ValidationConfig.builder()
                .useDefaultBehavior()
                .build();
    }
}
```

`useDefaultBehavior()` uses the default Jakarta Validator, map-based details, and
Jakarta `Default` validation group. Failed constraints produce
`ValidationFaultException` with HTTP `400` and standard field error responses.

Configure the default diagnostic detail format in YAML:

```yaml
reyga:
  custom:
    validation:
      map-details: true
```

`map-details: true` includes per-field diagnostic information. Use `false` for
unique messages only. This applies to the YAML default bean; manual beans follow builder choices.

### A.4. `ValidationConfig.Builder` options

| Method | Purpose |
|---|---|
| `useDefaultBehavior()` | Resets validator, details, and groups to defaults. |
| `withValidator(validator)` | Uses an application Jakarta `Validator` with standard violation handling. |
| `withProcessor(processor)` | Uses an application `BaseValidationProcessor` subclass. |
| `useMapDetails()` | Selects `violationsMapHandle()` and per-field diagnostics. |
| `useMessageDetails()` | Selects `violationsSetHandle()` and unique messages. |
| `withValidationGroups(Group.class, ...)` | Sets default groups for calls without explicit groups. |
| `build()` | Builds `ValidationUtility` from the configuration. |

`withValidator()` and `withProcessor()` replace each other. Call
`useDefaultBehavior()` first when combining options, so later choices are not reset.

Example using the application's Spring validator:

```java
@Bean
public ValidationUtility validationUtility(jakarta.validation.Validator validator) {
    return ValidationConfig.builder()
            .useDefaultBehavior()
            .withValidator(validator)
            .useMessageDetails()
            .build();
}
```

### A.5. Creating a custom validation processor

Use a custom processor for different exception creation, logging, or violation
grouping. Extend `BaseValidationProcessor` and implement both violation paths:

```java
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import reyga.starter.foundation.core.exception.ValidationFaultException;
import reyga.starter.foundation.core.validation.BaseValidationProcessor;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;

public final class ApplicationValidationProcessor extends BaseValidationProcessor {

    public ApplicationValidationProcessor(Validator validator) {
        super(validator);
    }

    @Override
    protected <T> void violationsSetHandle(Set<ConstraintViolation<T>> violations) {
        if (violations.isEmpty()) {
            return;
        }

        Set<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

        throw new ValidationFaultException(
                "400441", "Invalid Request", messages, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected <T> void violationsMapHandle(Set<ConstraintViolation<T>> violations) {
        if (violations.isEmpty()) {
            return;
        }

        var errors = violations.stream()
                .map(violation -> {
                    var detail = new LinkedHashMap<String, Object>();
                    detail.put("field", violation.getPropertyPath().toString());
                    detail.put("message", violation.getMessage());
                    return detail;
                })
                .toList();

        throw new ValidationFaultException(
                "400441", "Invalid Request", errors, HttpStatus.BAD_REQUEST);
    }
}
```

Register the processor through the builder:

```java
@Bean
public ValidationUtility validationUtility(Validator validator) {
    return ValidationConfig.builder()
            .withProcessor(new ApplicationValidationProcessor(validator))
            .useMapDetails()
            .build();
}
```

`useMapDetails()` calls `violationsMapHandle()`; `useMessageDetails()` calls
`violationsSetHandle()`. Custom processors must not retain requests across calls
and must be safe for singleton use. For `ValidationFaultException`, keep
`validation-handler: true`. For other exceptions, provide suitable `@RestControllerAdvice`.

### A.6. Validation groups

```java
public interface CreateValidationGroup {}
public interface UpdateValidationGroup {}
```

Set groups on a bean or an individual call:

```java
return ValidationConfig.builder()
        .useDefaultBehavior()
        .withValidationGroups(CreateValidationGroup.class)
        .build();
```

```java
validationUtility.validateRequest(request);
validationUtility.validateRequest(
        request,
        java.util.List.of(UpdateValidationGroup.class)
);
```

Explicit method groups replace configured groups. An empty list selects Jakarta
`Default`. Each group must be a non-null interface.

### A.7. Validation annotations

Four annotations are provided: `@FieldPresence`, `@FieldFormat`, `@FieldLength`,
and `@FieldNumberRange`. `message`, `groups`, and `payload` follow Jakarta Bean Validation.

#### `@FieldPresence`

Controls null and empty values independently for all field types.

| Attribute | Default | Purpose |
|---|---:|---|
| `nullable` | `false` | Whether `null` references are accepted. |
| `allowBlank` | `false` | Whether empty non-null values are accepted. |
| `message` | Default constraint message | Validation failure message. |

```java
@FieldPresence(
        nullable = false,
        allowBlank = false,
        message = "customerId is required"
)
private UUID customerId;
```

| `nullable` | `allowBlank` | `null` | Empty value | Populated value |
|---:|---:|---|---|---|
| `false` | `false` | Rejected | Rejected | Accepted |
| `false` | `true` | Rejected | Accepted | Accepted |
| `true` | `false` | Accepted | Rejected | Accepted |
| `true` | `true` | Accepted | Accepted | Accepted |

Empty includes blank/whitespace `CharSequence`, empty collections/maps, zero-length
arrays, and empty `Optional`. `0`, `false`, UUIDs, dates, enums, and other non-null
objects count as populated. Use `@Valid` for nested objects and element type
constraints for container contents.

```java
@FieldPresence
private java.util.List<@FieldPresence String> names;
```

#### `@FieldFormat`

Validates `String` and `Number` formats. `UUID` objects are supported only when
`formatType = FieldFormatTypeEnum.UUID`.

| Attribute | Default | Purpose |
|---|---:|---|
| `fieldName` | Empty string | Field name in default messages. |
| `formatType` | `ALLOW_ALL` | Built-in format policy. |
| `customFormat` | Empty string | Application regex replacing the built-in format. |
| `onlyContains` | Empty array | Text representation allowlist. |
| `onlyNumbers` | Empty array | Numeric value allowlist. |
| `message` | Empty string | Overrides processor error messages. |

```java
@FieldPresence(message = "email is required")
@FieldFormat(
        fieldName = "email",
        formatType = FieldFormatTypeEnum.EMAIL,
        message = "Invalid email format"
)
private String email;
```

| `FieldFormatTypeEnum` | Accepted format |
|---|---|
| `UUID` | Canonical `8-4-4-4-12`; versions 1�8 with RFC variant, Nil or Max UUID; `String` or `java.util.UUID`. |
| `DATE` | Valid calendar date in `yyyy-MM-dd`. |
| `IP_ADDRESS` | IPv4 with four octets from `0` to `255`. |
| `EMAIL` | ASCII email with valid local part and domain. |
| `PHONE_NUMBER` | Digits, spaces/hyphens, parentheses, and optional `+` prefix. |
| `LETTER_ONLY` | ASCII `A-Z` or `a-z`. |
| `ALPHANUMERIC` | ASCII letters and digits. |
| `NUMERIC_ONLY` | ASCII digits without signs or fractions. |
| `PASSWORD_STRONG` | At least eight characters with upper/lowercase letters, digits, and punctuation. |
| `ALLOW_ALL` | All supported text representations. |

Additional attributes:

- `customFormat` replaces the built-in regex.
- `onlyContains` restricts text to an allowlist.
- `onlyNumbers` restricts numbers exactly, without truncating fractions.
- `fieldName` appears in default messages.

`@FieldFormat` rejects `null`; combine presence/format rules as needed. Values are
not trimmed. `customFormat` regex must be valid and from trusted configuration.

Invalid JSON `UUID` fields are rejected by deserialization before validation.
Use `String` when all UUID text errors should be handled by `@FieldFormat`,
then convert to `UUID` after validation succeeds.

#### `@FieldLength`

Validates `String` length or `Number` digit count. `min`/`max` are inclusive.
Null is accepted; use `@FieldPresence` for required fields.

| Attribute | Default | Purpose |
|---|---:|---|
| `fieldName` | Empty string | Field name in default messages. |
| `min` | `0` | Minimum accepted length. |
| `max` | `Long.MAX_VALUE` | Maximum accepted length. |
| `message` | Empty string | Overrides processor error messages. |

```java
@FieldPresence
@FieldLength(
        fieldName = "customerName",
        min = 3,
        max = 100,
        message = "Name must contain 3 to 100 characters"
)
private String customerName;
```

Use on `String` or `Number`. For collections/arrays, use appropriate Jakarta constraints.

#### `@FieldNumberRange`

Validates `Number` within inclusive `min`/`max` bounds. Null is accepted.

| Attribute | Default | Purpose |
|---|---:|---|
| `fieldName` | Empty string | Field name in default messages. |
| `min` | `Long.MIN_VALUE` | Minimum accepted value. |
| `max` | `Long.MAX_VALUE` | Maximum accepted value. |
| `message` | Empty string | Overrides processor error messages. |

```java
@FieldPresence
@FieldNumberRange(
        fieldName = "retryCount",
        min = 1,
        max = 5,
        message = "Retry count must be between 1 and 5"
)
private Integer retryCount;
```

Use for integral numbers. For fractional precision, use Jakarta decimal constraints
or an application validator.

### A.8. Annotation validation groups

```java
@FieldPresence(
        groups = CreateValidationGroup.class,
        message = "ID is required"
)
@FieldFormat(
        fieldName = "id",
        formatType = FieldFormatTypeEnum.UUID,
        groups = CreateValidationGroup.class
)
private String id;
```

Constraints run only when the matching group is passed to `ValidationUtility`
or Spring MVC `@Validated`.

## B. Resilience Utils

### B.1. Enabling beans and features

`ResilienceService` is a facade for rate limiting, circuit breaking, and retries.
Enable default utilities and only the resilience features you need:

Resilience4j is transitive through `core`. Consumers choosing the complete production
dependency bundle can use `foundation-dependencies`; no separate `resilience4j-all`
declaration is needed.

```yaml
reyga:
  config:
    default-bean:
      utilities: true
    rate-limiter:
      required: true
      timeout-milis: 500
      max-request: 100
      refresh-period-seconds: 1
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
    retry:
      required: true
      max-attempts: 3
      wait-duration-millis: 500
      fail-after-max-attempts: true
```

`utilities: true` provides `ResilienceService` and `TransactionalExecutor`.
Each `required: true` provides feature config/registry. Since `TransactionalExecutor`
is also created, an application `PlatformTransactionManager` is required.

### B.2. Rate limiter

Rate limiters restrict execution by key. Fallback runs when no permission is available.

```java
@Service
@RequiredArgsConstructor
public class NotificationGateway {
    private final ResilienceService resilienceService;
    private final RateLimiterConfig rateLimiterConfig;
    private final RateLimiterRegistry rateLimiterRegistry;

    public NotificationResponse send(NotificationRequest request) {
        return resilienceService.useRateLimiter(
                rateLimiterConfig,
                rateLimiterRegistry,
                "notification-send",
                () -> sendToProvider(request),
                () -> NotificationResponse.rateLimited()
        );
    }
}
```

Use stable, distinct keys for policies. `useRateLimiterWithCache(...)` accepts
Spring `Cache`, caching limiter instances by key, not business results. If the
cache cannot read/store instances, the utility still uses the registry.

### B.3. Circuit breaker

Circuit breakers run fallback on main operation failures or denied calls.

```java
return resilienceService.useCircuitBreaker(
        circuitBreakerConfig,
        circuitBreakerRegistry,
        "customer-provider",
        () -> customerClient.findById(customerId),
        () -> CustomerResponse.unavailable(customerId)
);
```

Circuit names identify instances and metric sets. Fallback must be safe, fast,
and avoid unbounded repetition of the main operation.

### B.4. Retry

Retry repeats processing according to `RetryConfig`. After all attempts fail,
the last exception propagates to the caller.

```java
return resilienceService.useRetry(
        retryConfig,
        retryRegistry,
        "customer-read",
        () -> customerClient.findById(customerId)
);
```

Retry only repeatable operations, such as reads or writes with idempotency keys.
`max-attempts` includes the first call. `Runnable` overloads support no return value.

### B.5. Controller resilience

`ResilienceBaseController` provides rate limiter/circuit breaker response helpers.
Its constructor requires `ResilienceService`, `RateLimiterRegistry`, and
`CircuitBreakerRegistry`; enable both registries even when an endpoint only uses rate limiting.

Controller examples are in the
[Controller Implementation Guide](CONTROLLER_IMPLEMENTATION_GUIDE_EN.md#rate-limiter-example-using-yaml-configuration).

## C. Transactional Utility

`TransactionalExecutor` runs `Supplier` or `Runnable` within Spring transactions
with fallback on exceptions.

```java
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final TransactionalExecutor transactionalExecutor;

    public PaymentResult save(Payment payment) {
        return transactionalExecutor.runInTransaction(
                () -> savePaymentAndLedger(payment),
                error -> PaymentResult.failed(error.getMessage())
        );
    }
}
```

On failure, the transaction is marked for rollback and fallback receives the
error. Its return value becomes the method result. `Runnable` uses `Consumer<Throwable>` fallback.

Enable `reyga.config.default-bean.utilities: true` and provide `PlatformTransactionManager`.

## D. Date Utility

`DateUtility` provides stateless UTC/system time operations, conversions among
`Date`, `Timestamp`, `LocalDate`, `LocalDateTime`, parsing, formatting, and
start/end boundaries for days, weeks, and months.

`DateUtility.DEFAULT_TIMEZONE_ID` is `UTC`; use it for the library's default time-zone identifier.

```java
LocalDateTime utcNow = DateUtility.now();
LocalDate invoiceDate = DateUtility.parseLocalDate("2026-09-11", "yyyy-MM-dd");
String label = DateUtility.format(invoiceDate, "dd MMMM yyyy", new Locale("id", "ID"));
LocalDate monthEnd = DateUtility.getEndOfMonth(invoiceDate);
```

`now()` uses UTC; `SystemTimezone` suffixes use JVM time zones. Configure zones
consistently across environments. Failed parsing throws `DateTimeParseException`;
use `isValidDate()` for boolean checks.

## E. File Utility

`FileUtility` wraps `java.nio.file.Path` for normalization, filenames, reads/writes,
copy/move, directory creation, listing, deletion, and filename masking.

```java
Path report = FileUtility.toPath("reports/daily.csv");
FileUtility.ensureParentDirectory(report);
FileUtility.writeString(report, "id,total\n1,1000");

String extension = FileUtility.extension(report);       // csv
String safeName = FileUtility.maskFileName("customer.csv");
```

Strings use UTF-8 unless a charset is supplied. I/O can throw `IOException`;
applications own path authorization, access limits, and overwrite choices.
Do not use request paths without validation and directory restrictions.

## F. Mapper

### F.1. Preparing MapStruct

`BaseMapper<E, D>` is the entity/DTO mapping contract; `CommonMapperConfig` provides
consistent Spring MapStruct configuration. Implementations are generated at
compile time, so enable annotation processing in consumer projects.

Gradle configuration example:

```kotlin
dependencies {
implementation("com.reyga-dev.starter:common:1.1.0")
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
}
```

Maven configuration example:

```xml
<properties>
    <org.mapstruct.version>1.6.3</org.mapstruct.version>
</properties>

<dependencies>
    <dependency>
        <groupId>com.reyga-dev.starter</groupId>
        <artifactId>common</artifactId>
<version>1.1.0</version>
    </dependency>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${org.mapstruct.version}</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>${org.mapstruct.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

Use a library-compatible MapStruct version. For Lombok projects, add Lombok
processing and `lombok-mapstruct-binding` according to the build configuration.

### F.2. Creating a mapper

Create an interface annotated with `@Mapper`, extending `BaseMapper` with
`<Entity, DTO>` generic ordering.

```java
import org.mapstruct.Mapper;
import reyga.starter.foundation.common.mapper.BaseMapper;
import reyga.starter.foundation.common.mapper.CommonMapperConfig;

@Mapper(config = CommonMapperConfig.class)
public interface CustomerMapper extends BaseMapper<Customer, CustomerDto> {
}
```

It inherits these contracts:

| Method | Behavior |
|---|---|
| `toDto(E entity)` | Converts an entity to DTO |
| `toEntity(D dto)` | Converts a DTO to entity |
| `toDtoList(List<E> entities)` | Converts an entity list to DTO list |
| `toEntityList(List<D> dtos)` | Converts a DTO list to entity list |
| `updateEntity(D dto, E entity)` | Maps DTO values onto the supplied entity |

Matching property names/types need no additional methods. For differences,
override the appropriate contract and add mapping annotations.

```java
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public interface CustomerMapper extends BaseMapper<Customer, CustomerDto> {

    @Override
    @Mapping(target = "fullName", source = "name")
    CustomerDto toDto(Customer entity);

    @Override
    @Mapping(target = "name", source = "fullName")
    Customer toEntity(CustomerDto dto);
}
```

`CommonMapperConfig` applies these behaviors:

- Registers mapper implementations as Spring beans.
- Injects other mapper dependencies through constructors.
- Reports unmapped target properties as compilation errors.
- Always generates null checks where mappings require them.

Unmapped target errors prevent silently omitting new fields. Map with `@Mapping`
or explicitly use `@Mapping(target = "fieldName", ignore = true)` when unnecessary.

### F.3. Using mappers in services

Since `CommonMapperConfig` creates Spring beans, inject mapper interfaces through
constructors instead of creating instances manually.

```java
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(
            CustomerRepository customerRepository,
            CustomerMapper customerMapper
    ) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    public CustomerDto findById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        return customerMapper.toDto(customer);
    }

    public List<CustomerDto> findAll() {
        return customerMapper.toDtoList(customerRepository.findAll());
    }

    public CustomerDto update(UUID id, CustomerDto request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        customerMapper.updateEntity(request, customer);
        return customerMapper.toDto(customerRepository.save(customer));
    }
}
```

For partial updates, explicitly define null behavior to avoid overwriting entity
fields absent from the request.

```java
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Override
@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
void updateEntity(CustomerDto dto, @MappingTarget Customer entity);
```

Separate DTOs when create/update/response contracts differ. Mappers can declare
additional methods beyond `BaseMapper` in these cases.

### F.4. Mapper Utility for diagnostics

`MapperUtility.convertDtoToJsonString()` creates a text representation of DTO
fields for diagnostics/logging. Its second overload includes superclass fields
and handles nested objects, collections, arrays, maps, and circular references.

```java
String requestDetail = MapperUtility.convertDtoToJsonString(request, true);
```

It uses reflection, not a replacement for JSON API/persistence serializers.
Avoid objects with passwords, tokens, personal data, or sensitive fields because
their values may be printed.

## G. File Inspector

`FileInspector` uses Apache Tika for content type detection, text extraction,
extraction as `Reader`, and translation. `foundation-starter` provides its bean
automatically without an extra flag.

```java
import lombok.RequiredArgsConstructor;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Service;
import reyga.starter.foundation.common_io.operations.FileInspector;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class DocumentInspectionService {

    private final FileInspector fileInspector;

    public InspectedDocument inspect(InputStream inputStream, String fileName) {
        Metadata metadata = new Metadata();
        metadata.set(Metadata.RESOURCE_NAME_KEY, fileName);

        String contentType = fileInspector.checkContentType(inputStream, metadata);
        return new InspectedDocument(contentType, metadata);
    }
}
```

Choose overloads according to available sources:

| Need | Main method | Supported sources |
|---|---|---|
| Content type detection | `checkContentType(...)` | `InputStream`, `byte[]`, `File`, `Path`, filename, or `URL` |
| Direct text extraction | `extractContent(...)` | `InputStream`, `URL`, `File`, or `Path` |
| Streaming extraction | `extract(...)` | `InputStream`, `URL`, `File`, or `Path` |
| Text translation | `translate(...)` | Text and target language, or source and target languages |

Use `checkContentType(InputStream, String)` or `checkContentType(InputStream, Metadata)`
when names/metadata help detection. `checkContentType(String)` only examines a
filename, not file content. Results are media types such as `application/pdf` or `image/png`.

For large documents, `extractContent(InputStream, Metadata, int maxLength)` limits
output characters. Callers read/close returned `Reader` instances from `extract(...)`
and manage supplied `InputStream` lifecycles.

```java
Metadata metadata = new Metadata();
String preview = fileInspector.extractContent(inputStream, metadata, 10_000);

try (Reader reader = fileInspector.extract(path)) {
    // Process extracted content as a stream.
}
```

Translation needs a runtime Tika translator. Detection/extraction/translation
failures become `IOFaultException` with operations such as `DETECT_CONTENT_TYPE`,
`PARSE_TO_STRING`, `PARSE`, or `TRANSLATE`.

Detection helps upload validation but does not replace size limits, media-type
allowlists, malware checks, or file location restrictions.

## H. File Sanitizer

`FileSanitizer` sanitizes HTML and checks XSS/SQL injection indicators. Its default
bean is automatic through `foundation-starter` without extra flags. Default charset is UTF-8.

### H.1. HTML sanitization and XSS checks

```java
String safeHtml = fileSanitizer.sanitizeHtml(untrustedHtml);

if (fileSanitizer.checkXssPayload(untrustedHtml)) {
    throw new IllegalArgumentException("HTML contains disallowed elements");
}
```

OWASP defaults combine `FORMATTING`, `BLOCKS`, `LINKS`, `IMAGES`, and `STYLES`.
Use `PolicyFactory` overloads for other HTML allowlists.

```java
PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
String safeHtml = fileSanitizer.sanitizeHtml(untrustedHtml, policy);
```

`sanitizeHtml(...)` returns filtered HTML. `checkXssPayload(...)` compares input
and sanitized output, returning `true` when policy changes it. This detects
policy changes, not all possible XSS attacks.

Sanitization/XSS methods support `String`, `byte[]`, `InputStream`, `Path`, `File`,
and `URL`. Callers own supplied streams; internally opened URL streams are closed.

### H.2. SQL injection checks

```java
boolean suspicious = fileSanitizer.checkSqlInjection(userInput);

Pattern applicationPattern = Pattern.compile(
        "(?i)\\bcopy\\b.+\\bto\\s+program\\b"
);
boolean matchesApplicationRule = fileSanitizer.checkSqlInjection(
        userInput,
        applicationPattern
);
```

No-pattern overloads use defaults. `Pattern...`/`List<Pattern>` check only supplied
non-null patterns; combine defaults in application code if needed. Bytes, streams,
paths, files, and URLs are also supported.

Regex checks can yield false positives/negatives. Prepared statements and parameter
binding are primary protection; do not concatenate SQL even after a `false` result.

Nulls, read failures, and invalid policies produce `IOFaultException`. Metadata
records `SANITIZE`, `CHECK_XSS`, or `CHECK_SQL`, `READ_ONLY`, source storage, and charset.

## I. Report Builder

`ReportBuilder` stages fluent JRXML compilation, filling, and JasperReports export.
Return types enforce flow: `CompileStep` yields `FillStep`, then `ExportStep`.

```java
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import reyga.starter.foundation.common_io.enumeration.ReportType;
import reyga.starter.foundation.common_io.reports.ReportBuilder;

Map<String, Object> parameters = Map.of(
        "REPORT_TITLE", "Laporan Transaksi"
);

byte[] pdf = ReportBuilder
        .from("reports/transaction.jrxml")
        .compile()
        .withParameters(parameters)
        .withLocale(new Locale("id", "ID"))
        .withTimeZone(TimeZone.getTimeZone("Asia/Jakarta"))
        .withDataSource(new JRBeanCollectionDataSource(rows))
        .fill()
        .exportTo(ReportType.PDF);
```

Sources include `InputStream`, String paths, or `JasperDesign`. Streams are fully
buffered and may throw `IOException`; compile/fill/export may throw `JRException`.

For filling, choose a source:

- `withDataSource(JRDataSource)` for collections/Jasper sources;
- `withConnection(Connection)` for JDBC query reports;
- Neither for `JREmptyDataSource`.

Connections take priority when both are supplied; callers own their lifecycle.
`withParameters(...)` calls merge values, later keys replacing earlier ones.
`withLocale(...)`/`withTimeZone(...)` set JasperReports built-in parameters.

```java
try (OutputStream outputStream = Files.newOutputStream(target)) {
    ReportBuilder.from(jrxmlInputStream)
            .compile()
            .withConnection(connection)
            .fill()
            .exportTo(ReportType.XLSX, outputStream);
}
```

Supported directly: `HTML`, `PDF`, `XLS`, `XLSX`, `CSV`, `DOCX`, `PPTX`, `RTF`, `ODT`,
`ODS`, `XML`, `JSON`, `TEXT`, `JRPRINT`. `ODP`, `PNG`, `JPEG`, `GIF`, `SVG` exist in
`ReportType` but lack exporters and throw `JRException`. Use `getExtension()` and
`getMimeType()` for filenames and response headers.

```java
ReportType type = ReportType.PDF;
return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(type.getMimeType()))
        .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=report." + type.getExtension()
        )
        .body(pdf);
```

`ReportBuilderProvider`/Java `ServiceLoader` discover implementations. Provide
runtime `foundation-starter`; API-only `common-io` without providers throws
`IllegalStateException`. Builders retain compile, parameters, data, and fill state;
create one per report and do not share across threads.

## J. `IOFaultException` and metadata

`IOFaultException` represents runtime file/content failures, with single
`IOFaultMetadata`, multi-file metadata lists, and original causes.

```java
IOFaultMetadata metadata = IOFaultMetadata.builder()
        .fileName(uploadedFile.getOriginalFilename())
        .mimeType(MediaType.APPLICATION_PDF)
        .charset(StandardCharsets.UTF_8.name())
        .operation(IOOperation.VALIDATE)
        .permissions(IOPermissions.READ_ONLY)
        .storageType(IOStorageType.MEMORY)
        .sizeBytes(uploadedFile.getSize())
        .build();

throw new IOFaultException(
        "Invalid uploaded document",
        metadata,
        cause
);
```

Available constructors:

| Constructor | Usage |
|---|---|
| `IOFaultException(message)` | No metadata/cause |
| `IOFaultException(message, cause)` | Original cause |
| `IOFaultException(message, metadata)` | Single resource |
| `IOFaultException(message, metadata, cause)` | Single resource with cause |
| `IOFaultException(message, metadataList)` | Multiple resources |
| `IOFaultException(message, metadataList, cause)` | Multiple resources with cause |

Populate these `IOFaultMetadata` fields for the operation:

| Field | Expected content |
|---|---|
| `fileName` | Filename, not a trusted path |
| `filePath` | Source/target storage path |
| `uri` | Local/remote resource URI |
| `mimeType` | Spring `MediaType` |
| `charset` | Charset name such as `UTF-8` |
| `operation` | `IOOperation`, e.g. `READ`, `WRITE`, `SANITIZE`, `VALIDATE` |
| `permissions` | `IOPermissions` access mode |
| `storageType` | `IOStorageType`, e.g. `LOCAL`, `MEMORY`, `S3`, `HTTPS` |
| `checksum` | Checksum if available |
| `directory` | Directory indicator |
| `sizeBytes` | Size in bytes |
| `lastModifiedEpochMillis` | Modification time in epoch milliseconds |
| `offset` | Start of failed data segment |
| `length` | Failed segment length |

`IOOperation` includes `COPY`, `MOVE`, `DELETE`, `CREATE`, `LIST`, `METADATA`.
Choose the actual failed operation for accurate diagnostics.

`DefaultAppValidationExceptionHandler` returns HTTP `400 Bad Request` with file
error service code. All metadata becomes file details: masked names, operations,
MIME types, charset, directory, size, modification, offset, length. Internal paths,
URIs, permissions, storage types, and checksums are excluded.

Do not include file content, credentials, tokens, or sensitive paths in messages/
metadata when prohibited in logs. Retain original causes for internal diagnostics.

## K. Aspect Advice

Advice runs application behavior at specific method lifecycle points. Each has
separate annotations, base behavior, flags, and bean names; enable only what is needed.

`spring-boot-starter-aspectj` is transitive through `core`; no manual Spring AOP/
AspectJ dependencies are needed unless excluded. It is also in the opt-in
`foundation-dependencies` production dependency bundle.

| Advice | Method annotation | Base behavior | Execution time |
|---|---|---|---|
| Around | `@AroundExecution` | `BaseAspectAround` | Controls before/after lifecycle. |
| Before | `@BeforeExecution` | `BaseAspectBefore` | Immediately before target. |
| After | `@AfterExecution` | `BaseAspectAfter` | After target, success/failure. |
| After Returning | `@AfterReturningExecution` | `BaseAspectAfterReturning` | After successful return. |
| After Throwing | `@AfterThrowingExecution` | `BaseAspectAfterThrowing` | After target error. |

### K.1. Around advice

Around suits audit, timing, context collection, or cross-cutting work needing the
full invocation lifecycle.

Usage flow:

1. Create context extending `BaseContent`.
2. Create `@Component` behavior extending `BaseAspectAround`.
3. Enable `reyga.config.aspect.around`.
4. Set `reyga.config.aspect.behavior` to its bean name.
5. Add `@AroundExecution` to the Spring bean method.

#### K.1.1. Creating behavior context

Context stores pre-execution data for reuse after execution or in exception hooks.
It must extend `BaseContent`:

```java
public final class AuditContent extends BaseContent {
    private final long startedAtNanos = System.nanoTime();

    public long getStartedAtNanos() {
        return startedAtNanos;
    }
}
```

Create context per invocation, not in singleton fields; multiple threads may
invoke the same behavior concurrently.

#### K.1.2. Creating Around behavior

Register a Spring bean extending `BaseAspectAround`. Explicit `@Component` names
are recommended so YAML does not depend on default Spring naming:

```java
@Component("auditAspectBehavior")
public class AuditAspectBehavior extends BaseAspectAround<AuditContent> {

    private static final Logger log =
            LoggerFactory.getLogger(AuditAspectBehavior.class);

    @Override
    protected AuditContent preHandle() {
        return new AuditContent();
    }

    @Override
    protected void postHandle(AuditContent content) {
        long durationNanos = System.nanoTime() - content.getStartedAtNanos();
        log.info("Method completed in {} ns", durationNanos);
    }

    @Override
    protected boolean shouldHandleException() {
        return true;
    }

    @Override
    protected void handleException(
            Exception exception,
            AuditContent content
    ) {
        log.error("Intercepted method failed", exception);
    }
}
```

Available hooks:

| Hook | Execution time | Responsibility |
|---|---|---|
| `preHandle()` | Before target | Creates invocation context. |
| `postHandle(content)` | `finally` after target | Closes lifecycle, records timing, or final results. |
| `shouldHandleException()` | After target throws `Exception` | Enables exception hook. |
| `handleException(exception, content)` | Exceptions with hook enabled | Logging, audit, or error actions. |

`handleException(...)` does not replace the target exception; it propagates after the hook.

#### K.1.3. Enabling and selecting behavior

Enable Around Aspect and set the bean name:

```yaml
reyga:
  config:
    aspect:
      around: true
      behavior: auditAspectBehavior
```

`behavior` must match `@Component("auditAspectBehavior")` exactly. Without a name,
Spring lowercases the first class letter: `AuditAspectBehavior` becomes `auditAspectBehavior`.

Multiple `BaseAspectAround` beans can exist; only the one selected through
`reyga.config.aspect.behavior` runs:

```yaml
reyga:
  config:
    aspect:
      around: true
      behavior: paymentAuditBehavior
```

Change this property to switch environment behavior without changing annotations.

When unused, disable the flag and clear behavior:

```yaml
reyga:
  config:
    aspect:
      around: false
      behavior: ""
```

With `around: false`, no `AspectAround` dispatcher exists and `@AroundExecution`
does not change execution.

#### K.1.4. Marking methods with `@AroundExecution`

Annotate Spring bean methods:

```java
@Service
public class PaymentService {

    @AroundExecution
    public PaymentResponse process(PaymentRequest request) {
        // Run business logic.
        return new PaymentResponse();
    }
}
```

Unannotated methods run normally without behavior:

```java
public PaymentStatus findStatus(String paymentId) {
    return repository.findStatus(paymentId);
}
```

Targets must be Spring-managed through `@Service`, `@Component`, or `@Bean`.
Calls must pass through proxies; direct calls within one instance do not trigger Around.

Annotate invoked implementation methods, use non-final methods, and call through
Spring-injected beans for consistent behavior.

#### K.1.5. Lifecycle order

On successful completion:

```text
preHandle()
    -> target method
    -> postHandle(content)
    -> return value propagates to the caller
```

When targets throw and `shouldHandleException()` returns `true`:

```text
preHandle()
    -> target method throws an exception
    -> shouldHandleException()
    -> handleException(exception, content)
    -> postHandle(content)
    -> exception propagates to the caller
```

If false, `handleException(...)` is skipped but `postHandle(...)` still runs.
If `preHandle()` fails, target and `postHandle(...)` do not run because no context was created.

#### K.1.6. Startup configuration validation

With `around: true`, invalid behavior fails startup to prevent apparently active
annotations with no implementation.

| Condition | Message and corrective action |
|---|---|
| Empty `behavior` | Set `reyga.config.aspect.behavior` to a bean name. |
| Bean missing | Check scan coverage and matching YAML/bean names. |
| Wrong type | Extend `BaseAspectAround`, not an ordinary/different bean. |
| Behavior not invoked | Check Spring management, annotation, proxy calls, and non-final methods. |
| Wrong behavior | Check `reyga.config.aspect.behavior`; only that name is selected. |

### K.2. Before advice

Before suits access checks, preconditions, initial logging, and data preparation.
Create a bean extending `BaseAspectBefore`:

```java
@Component("authorizationBeforeBehavior")
public class AuthorizationBeforeBehavior extends BaseAspectBefore {

    @Override
    protected void beforeHandle(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        // Check access or record execution start.
    }
}
```

Enable behavior and annotate the target:

```yaml
reyga:
  config:
    aspect:
      before: true
      before-behavior: authorizationBeforeBehavior
```

```java
@BeforeExecution
public PaymentResponse process(PaymentRequest request) {
    return paymentProcessor.process(request);
}
```

If `beforeHandle(...)` throws, the target is not run and the exception propagates.

### K.3. After advice

After runs whenever the target finishes, like `finally`. Use for resource cleanup
or recording completion regardless of outcome.

```java
@Component("cleanupAfterBehavior")
public class CleanupAfterBehavior extends BaseAspectAfter {

    @Override
    protected void afterHandle(JoinPoint joinPoint) {
        // Clean up context or resources for this invocation.
    }
}
```

```yaml
reyga:
  config:
    aspect:
      after: true
      after-behavior: cleanupAfterBehavior
```

```java
@AfterExecution
public void synchronizePayment() {
    paymentSynchronizer.synchronize();
}
```

`afterHandle(...)` runs on success/errors. Use After Returning/Throwing for one outcome only.

### K.4. After Returning advice

Runs only after success; returned values are available through `result`.

```java
@Component("resultAuditBehavior")
public class ResultAuditBehavior extends BaseAspectAfterReturning {

    @Override
    protected void afterReturningHandle(JoinPoint joinPoint, Object result) {
        // Audit results without changing the caller's return value.
    }
}
```

```yaml
reyga:
  config:
    aspect:
      after-returning: true
      after-returning-behavior: resultAuditBehavior
```

```java
@AfterReturningExecution
public PaymentStatus findStatus(String paymentId) {
    return paymentRepository.findStatus(paymentId);
}
```

For `void`, `result` is null. Behavior observes returns without replacing them.

### K.5. After Throwing advice

Runs only for errors. Original `Throwable` supports logging, failure audit, and metrics.

```java
@Component("failureAuditBehavior")
public class FailureAuditBehavior extends BaseAspectAfterThrowing {

    @Override
    protected void afterThrowingHandle(
            JoinPoint joinPoint,
            Throwable throwable
    ) {
        // Record failures without retaining sensitive data.
    }
}
```

```yaml
reyga:
  config:
    aspect:
      after-throwing: true
      after-throwing-behavior: failureAuditBehavior
```

```java
@AfterThrowingExecution
public void cancelPayment(String paymentId) {
    paymentProcessor.cancel(paymentId);
}
```

Target errors still propagate after behavior finishes.

### K.6. Complete configuration and usage rules

Each advice can be enabled independently:

```yaml
reyga:
  config:
    aspect:
      around: true
      behavior: auditAspectBehavior
      before: true
      before-behavior: authorizationBeforeBehavior
      after: true
      after-behavior: cleanupAfterBehavior
      after-returning: true
      after-returning-behavior: resultAuditBehavior
      after-throwing: true
      after-throwing-behavior: failureAuditBehavior
```

Enabled flags require corresponding behavior properties, matching Spring beans,
and correct base subclasses. Any violation fails startup. Disabled flags create
no dispatchers, so annotations do not affect execution.

Methods may use multiple advice annotations, but relative order is not guaranteed.
Use Around when before/after ordering must be related through shared context.

All advice requires Spring bean proxy calls. Self-invocation and `final` methods
are not intercepted.

#### K.6.1. Using advice on `BaseService` subclasses

To advise an inherited `execute()` lifecycle, override `execute()`, annotate the
override, and delegate to `super.execute()`:

```java
@Override
@BeforeExecution
public List<GetNotificationResponse> execute(EmptyRequest request) {
    return super.execute(request);
}
```

The override is the Spring proxy entry point; `super.execute(request)` still
runs servlet checks, logging, `validateRequest()`, and `buildProcess()`.
Call `execute()` through a Spring-injected service bean.

Do not annotate only `validateRequest()` or `buildProcess()`: `BaseService.execute()`
calls them internally on the same instance, bypassing the proxy. The same override
pattern applies to `@AroundExecution`, `@AfterExecution`, `@AfterReturningExecution`,
and `@AfterThrowingExecution`.

| Problem | Corrective action |
|---|---|
| Empty behavior property | Set the enabled flag's matching behavior property. |
| Bean missing | Check scan coverage and YAML/bean name matching. |
| Wrong type | Extend the advice's correct base behavior. |
| Behavior not invoked | Check Spring management, correct annotation, proxy calls, and non-final methods. For `BaseService`, override and annotate `execute()`. |

A brief flag configuration reference is in the
[Configuration Guide](CONFIGURATION_GUIDE_EN.md#m-aspect-advice).

## L. Common HTTP Client

`common-http` provides `StarterHttpClient` for synchronous, asynchronous, and
response-cached OkHttp requests. Consumers use its API; default implementations
and auto-configuration are in runtime `foundation-starter`.

`StarterHttpClient` is thread-safe. Default implementations reuse one `OkHttpClient`
for shared connection pools/dispatchers; do not create one per request.

### L.1. Dependencies and bean activation

With the Foundation BOM, add API modules without repeating versions:

```kotlin
val foundationVersion = "1.1.0"

dependencies {
    implementation(platform(
        "com.reyga-dev.starter:foundation-bom:$foundationVersion"
    ))
    implementation("com.reyga-dev.starter:common-http")
    runtimeOnly("com.reyga-dev.starter:foundation-starter")
}
```

Equivalent Maven configuration after importing `foundation-bom`:

```xml
<dependencies>
    <dependency>
        <groupId>com.reyga-dev.starter</groupId>
        <artifactId>common-http</artifactId>
    </dependency>
    <dependency>
        <groupId>com.reyga-dev.starter</groupId>
        <artifactId>foundation-starter</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

Enable defaults through this configuration:

```yaml
reyga:
  config:
    default-bean:
      http-client: true
```

The flag defaults to false. When enabled, the starter creates one `OkHttpClient`
and one `StarterHttpClient`. Application beans of those types take precedence,
allowing customization without changing service contracts.

### L.2. Synchronous requests

Build OkHttp `Request`, then call `call(...)`. Close every `Response`, including non-2xx:

```java
package com.example.customer.integration;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;
import reyga.starter.foundation.common_http.client.StarterHttpClient;

@Component
public class CustomerGateway {

    private final StarterHttpClient httpClient;

    public CustomerGateway(StarterHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String findCustomer(String customerId) throws IOException {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("customer.example.com")
                .addPathSegment("customers")
                .addPathSegment(customerId)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.call(request)) {
            if (!response.isSuccessful()) {
                throw new IOException(
                        "Customer API returned HTTP " + response.code()
                );
            }
            return response.body().string();
        }
    }
}
```

`call(...)` propagates connection/protocol/cancellation `IOException`. Bodies are
one-shot; do not read twice. Do not log authorization headers, tokens, or entire
potentially sensitive bodies.

### L.3. Asynchronous requests and cancellation

`asynchronousCall(...)` immediately returns `Call`. Retain it for cancellation
on business timeouts or caller lifecycle completion:

```java
import java.io.IOException;
import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public Call findCustomerAsync(
        Consumer<IOException> failureHandler,
        Consumer<String> responseHandler
) {
    Request request = new Request.Builder()
            .url("https://customer.example.com/customers/CUS-01")
            .get()
            .build();

    return httpClient.asynchronousCall(request, new Callback() {
        @Override
        public void onFailure(Call call, IOException exception) {
            failureHandler.accept(exception);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            try (response) {
                if (!response.isSuccessful()) {
                    failureHandler.accept(new IOException(
                            "Customer API returned HTTP " + response.code()
                    ));
                    return;
                }
                responseHandler.accept(response.body().string());
            }
        }
    });
}
```

Call and cancel it from the caller lifecycle:

```java
Call call = customerGateway.findCustomerAsync(
        failureHandler,
        responseHandler
);

// Call only when the request is no longer needed.
call.cancel();
```

Callbacks run on OkHttp dispatchers. Keep them thread-safe; avoid lengthy CPU or
blocking work. `onResponse(...)` must close responses even on parsing/validation failures.

### L.4. Response caching

Caching follows HTTP methods, `Cache-Control`, `Expires`, and response validators.
Providing `Cache` does not make non-cacheable responses cacheable.

For shared caches, use one long-lived `Cache` bean and `OkHttpClient` bean.
Use ordinary `call(...)`; OkHttp selects network/cache responses:

```java
package com.example.config;

import java.nio.file.Path;
import java.time.Duration;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class HttpClientConfiguration {

    @Bean(destroyMethod = "close")
    Cache httpResponseCache() {
        Path cacheDirectory = Path.of("var", "cache", "customer-http");
        return new Cache(cacheDirectory.toFile(), 50L * 1024L * 1024L);
    }

    @Bean
    OkHttpClient okHttpClient(Cache cache) {
        return new OkHttpClient.Builder()
                .cache(cache)
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(20))
                .callTimeout(Duration.ofSeconds(30))
                .build();
    }
}
```

Auto-configuration uses that client for `StarterHttpClient`. Only one `Cache`
instance should use a directory; provide suitable permissions and do not store
sensitive responses without appropriate server cache policies.

`callWithResponseCaching(request, cache)` supports per-call cache selection.
Derived clients share the main pool/dispatcher. Callers own cache lifecycles;
do not close while other requests use them.

### L.5. Customizing `OkHttpClient`

Declare your own bean for timeouts, interceptors, proxies, authenticators,
certificate pinning, or TLS. Auto-configuration does not create another client.

Set upstream-appropriate timeouts; retry only safe/idempotent operations.
Interceptors must not log credentials, authorization, sensitive cookies, or
unmasked bodies. Keep default certificate/hostname validation unless a reviewed
security requirement mandates otherwise.

API behavior summary:

| Method | Execution | Return | Caller responsibility |
|---|---|---|---|
| `call(request)` | Synchronous | `Response` | Close response, handle `IOException`. |
| `asynchronousCall(request, callback)` | Asynchronous | `Call` | Close callback responses; cancel only as needed. |
| `callWithResponseCaching(request, cache)` | Synchronous with selected cache | `Response` | Close response, manage cache lifecycle. |

Properties and troubleshooting are in the
[Configuration Guide](CONFIGURATION_GUIDE_EN.md#default-http-client).

### L.6. Registered APIs and operation builders

`StarterHttpClient.operationBuilder()` selects YAML defaults by `id` and supports
per-request URL, method, headers, query, path variables, JSON/XML payload, response
type, and call timeout overrides. `execute(operation)` returns raw OkHttp
`Response`; `executeAsync(operation, callback)` returns an enqueued `Call`.
`HttpOperations.readResponse(response)` optionally decodes the body once.
The caller owns response closure and HTTP status handling. See the
[HTTP client guide](HTTP_CLIENT_GUIDE_EN.md) for examples and validation rules.

With complete YAML defaults, bodyless builders only need id; body requests require
id and requestBody(payload). Payload sources are not stored in YAML or resolved
from beans. Method/media setters also
accept HttpMethod/HttpContentType; header/addHeader accept HttpHeader names. String
overloads remain available. All enums belong to
`reyga.starter.foundation.common_http.enums`; see the
[enum reference](HTTP_CLIENT_GUIDE.md#12-enum-dan-string-pada-builder-dan-yaml).

## M. Common Logger

`CommonLogger` supplies foundation formatting over SLF4J. Use `@InjectLogger`
on Spring beans with runtime `foundation-starter`:

```java
@Service
public class PaymentService {

    @InjectLogger
    private CommonLogger logger;

    public void process(String paymentId) {
        logger.info("Payment ID", paymentId);
    }
}
```

Fields must be exactly `CommonLogger`, neither static nor final, and may be inherited.
Existing consumer values are retained. Invalid annotated fields fail bean initialization.

For non-Spring objects, use the factory:

```java
private static final CommonLogger LOGGER =
        CommonLoggerFactory.getLogger(PaymentMapper.class);
```

Use key-value overloads for stable diagnostic labels:

```java
logger.info("Payment ID", paymentId);
logger.warn("Retry Count", retryCount);
logger.errorWithCause("Payment processing failed", exception);
```

Throwable overloads preserve error stack traces. Do not replace exceptions with
`exception.getMessage()`; this loses causes/traces. Formatting occurs only when
the corresponding level is enabled.

The runtime registers a Spring MVC `HandlerInterceptor`, logging one
`SERVICE_EXECUTION` through `afterCompletion` after each request. It includes
outcome, handler, ID, method, endpoint, status, duration, caller, metadata, and
errors, without reading request/response bodies. Example:

```text
SERVICE_EXECUTION | outcome=SUCCESS | handler=com.example.NotificationController#update | requestId=request-01 | http={method=PUT, endpoint=/notifications/1, status=200, duration=12 ms} | caller={username=operator, clientAddress=127.0.0.1, userAgent=client/1.0} | request={requestParams=null, requestMultiPart=null, requestPathVariable={id=1}, requestBody=null} | response= | error=
```

Consumers can call `infoServiceSummary(ServiceExecutionSummary)` for similar
summaries. Each is one line, escaping carriage returns/line feeds. Access tokens
are omitted. Sensitive headers, including `Authorization`, `Proxy-Authorization`,
cookies, API keys, and `x-starter-access-token`, are redacted before header maps/MDC.
Consumers must still mask requests, responses, usernames, network addresses, and
other personal data according to application needs.

## N. JDBC Result Set Mapping

For general and PostgreSQL/MySQL/Oracle/SQL Server/SQLite SQL expression helpers,
see [QueryFunction in the repository guide](REPOSITORY_IMPLEMENTATION_GUIDE_EN.md#i-queryfunction).
Helpers accept trusted SQL without binding. The guide includes string, regex,
numeric, date, and JSON reading/modification functions for each database group.

`JdbcResultSetBuilder` creates Spring `RowMapper` callbacks reading through
`JdbcResultSetMapper`. Nullable reads retain SQL NULL; mandatory reads reject it
with `SQLDataException` (state `22004`). Use numeric/boolean wrappers; `0` and
`false` are valid mandatory values.

For JSON, use `getNullableJson(column, decoder)`/`getMandatoryJson(column, decoder)`
with `JdbcJsonDecoder<T>`. Consumer decoders map nested objects, arrays, and generics.
`JdbcJsonColumnReaders` selects text, character streams, or UTF-8 binary streams;
custom driver readers can replace these. APIs are independent of vendor JSON/parser types.

No Spring bean or activation property is needed. Legacy static getters retain
behavior. Records, conversion, driver rules, thread-safety, and processor integration
are in the [Repository Guide](REPOSITORY_IMPLEMENTATION_GUIDE_EN.md#mapping-nullable-and-mandatory-columns).
Decoders, limits, SQL NULL/JSON null, and LOBs are in
[JSON Mapping](REPOSITORY_IMPLEMENTATION_GUIDE_EN.md#mapping-json-columns-and-nested-json).

## Utility Selection Summary

| Need | Primary API | Configuration |
|---|---|---|
| Programmatic request validation | `ValidationUtility`, `ValidationConfig` | `validation-handler: true` for library handler |
| Field constraints | `@FieldPresence`, `@FieldFormat`, `@FieldLength`, `@FieldNumberRange` | Jakarta Validator bean |
| Rate limiting, circuit breaking, retries | `ResilienceService` | `utilities: true`, per-feature `required: true` |
| Transactions with fallback | `TransactionalExecutor` | `utilities: true`, transaction manager |
| AOP lifecycle | `@AroundExecution`, `@BeforeExecution`, `@AfterExecution`, `@AfterReturningExecution`, `@AfterThrowingExecution` | Advice flags and matching behavior names |
| Dates/times | `DateUtility` | No bean required |
| File operations | `FileUtility` | No bean required |
| Content detection/extraction | `FileInspector` | Automatic `foundation-starter` bean |
| HTML sanitization/payload checks | `FileSanitizer` | Automatic `foundation-starter` bean |
| JasperReports | `ReportBuilder` | Runtime `foundation-starter` provider |
| Structured I/O errors | `IOFaultException`, `IOFaultMetadata` | Default handler: file error HTTP 400 |
| Entity/DTO mapping | `BaseMapper`, `CommonMapperConfig` | MapStruct processor |
| Explicit-nullability JDBC mapping | `JdbcResultSetBuilder`, `JdbcResultSetMapper` | No bean required |
| Diagnostic DTO representation | `MapperUtility` | No bean required |
| Sync/async/cached HTTP | `StarterHttpClient` | `http-client: true` or application bean |
| Formatted logs/logger injection | `CommonLogger`, `@InjectLogger` | Automatic `foundation-starter` or `CommonLoggerFactory` |

Mock `ValidationUtility`, `ResilienceService`, `TransactionalExecutor`, or
`StarterHttpClient` for service tests. Test DTO annotations using Jakarta `Validator`
or `ValidationUtility` built through `ValidationConfig`.

## QueryBuilder Subqueries

QueryBuilder supports WHERE SELECT subqueries via `conditionSubquery`, `exists`,
and `notExists`, and correlation via `compareColumns`. SQL/bindings are copied
on attachment. See [patterns and limits](REPOSITORY_IMPLEMENTATION_GUIDE_EN.md#subqueries-in-querybuilder).

QueryBuilder aliases are available through `.as(alias)` after select/from and
on JoinBuilder. Columns use AS; tables omit it. See
[alias examples](REPOSITORY_IMPLEMENTATION_GUIDE_EN.md#aliases-with-as).
