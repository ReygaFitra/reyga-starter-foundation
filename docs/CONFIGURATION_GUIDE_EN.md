# Configuration Guide

Use this guide to prepare application dependencies, select the features you need,
and configure their behavior through `application.yml` or `application.properties`.

Start with steps 1�3. Later sections explain feature options, defaults, profile
examples, and troubleshooting.

## A. Step 1: Prepare Application Dependencies

Add the API modules you use (`core`, `common`, `common-database`, `common-io`,
`common-http`, or `logging`) as compile dependencies. Align them with the library's
`projectVersion` (currently `1.1.0`). Add the following starter for built-in features:

```xml
<dependency>
    <groupId>com.reyga-dev.starter</groupId>
    <artifactId>foundation-starter</artifactId>
<version>1.1.0</version>
    <scope>runtime</scope>
</dependency>
```

For JDBC applications, include `spring-boot-starter-jdbc` and your database driver.
Configure a datasource so `JdbcTemplate` is available; `QueryProcessor` does not
require an additional activation flag.

PostgreSQL example (Spring Boot dependencies follow application dependency management):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

Set environment variables for your application environment; do not store production
passwords in the repository. This starter version requires a datasource/JdbcTemplate.
If using `utilities: true`, also provide an application transaction manager.

For examples of requests, interface contracts, concrete services, servlet context,
and calling `execute()` from controllers, see the
[Service Implementation Guide](SERVICE_IMPLEMENTATION_GUIDE_EN.md).

> All `reyga.config.*` feature flags default to `false` unless otherwise stated.
> There is no single master switch for the entire library.

## B. Step 2: Enable the Features You Use

Combine the example below with your datasource configuration. It enables validation,
exception handling, and HTTP logging. Adjust flags as needed; do not duplicate YAML keys.

```yaml
#
# Use the library's built-in Logback configuration
#
logging:
  config: classpath:META-INF/logback-spring.xml

reyga:
  config:
    default-bean:
      exception-handler: true
      validation-handler: true
      logging-handler: true
      http-client: false
      utilities: false
    aspect:
      around: false
      behavior: ""
      before: false
      before-behavior: ""
      after: false
      after-behavior: ""
      after-returning: false
      after-returning-behavior: ""
      after-throwing: false
      after-throwing-behavior: ""
    rate-limiter:
      required: false
    circuit-breaker:
      required: false
    retry:
      required: false
    logging:
      file:
        enable: false
        summary:
          enable: true
        async-logs:
          core-pool-size: 5
          max-pool-size: 10
          queue-capacity: 100
          thread-name: async-logging-
```

If you use no opt-in features, you may omit `reyga.config`. Dependency and datasource
prerequisites still apply.

## C. Step 3: Choose an Implementation Guide

- [Service](SERVICE_IMPLEMENTATION_GUIDE_EN.md): DTOs, validation, business processing, and servlet context.
- [Controller](CONTROLLER_IMPLEMENTATION_GUIDE_EN.md): calling `execute()`, responses, and rate limiting.
- [Repository](REPOSITORY_IMPLEMENTATION_GUIDE_EN.md): repository interfaces, queries, and pagination.

## D. Feature Activation Summary

| Feature | Property | Default | Effect when enabled |
|---|---|---:|---|
| Global exception handler | `reyga.config.default-bean.exception-handler` | `false` | Handles general and database exceptions using the standard error format. |
| Programmatic validation | `reyga.config.default-bean.validation-handler` | `false` | Enables programmatic validation through `ValidationUtility` injection. |
| HTTP request/response logging | `reyga.config.default-bean.logging-handler` | `false` | Records HTTP requests and responses. |
| Default HTTP client | `reyga.config.default-bean.http-client` | `false` | Registers reusable `OkHttpClient` and `StarterHttpClient` beans. |
| Default utilities | `reyga.config.default-bean.utilities` | `false` | Registers `ResilienceService` and `TransactionalExecutor`. |
| Around aspect | `reyga.config.aspect.around` | `false` | Registers an aspect for methods annotated with `@AroundExecution`. |
| Around behavior | `reyga.config.aspect.behavior` | empty string | Name of the `BaseAspectAround` subclass bean used when the around aspect is enabled. |
| Before aspect | `reyga.config.aspect.before` | `false` | Registers an aspect for methods annotated with `@BeforeExecution`. |
| Before behavior | `reyga.config.aspect.before-behavior` | empty string | Name of the `BaseAspectBefore` subclass bean used when the before aspect is enabled. |
| After aspect | `reyga.config.aspect.after` | `false` | Registers an aspect for methods annotated with `@AfterExecution`. |
| After behavior | `reyga.config.aspect.after-behavior` | empty string | Name of the `BaseAspectAfter` subclass bean used when the after aspect is enabled. |
| After Returning aspect | `reyga.config.aspect.after-returning` | `false` | Registers an aspect for methods annotated with `@AfterReturningExecution`. |
| After Returning behavior | `reyga.config.aspect.after-returning-behavior` | empty string | Name of the `BaseAspectAfterReturning` subclass bean used when the advice is enabled. |
| After Throwing aspect | `reyga.config.aspect.after-throwing` | `false` | Registers an aspect for methods annotated with `@AfterThrowingExecution`. |
| After Throwing behavior | `reyga.config.aspect.after-throwing-behavior` | empty string | Name of the `BaseAspectAfterThrowing` subclass bean used when the advice is enabled. |
| Rate limiter | `reyga.config.rate-limiter.required` | `false` | Registers default `RateLimiterConfig` and `RateLimiterRegistry` beans. |
| Circuit breaker | `reyga.config.circuit-breaker.required` | `false` | Registers default `CircuitBreakerConfig` and `CircuitBreakerRegistry` beans. |
| Retry | `reyga.config.retry.required` | `false` | Registers default `RetryConfig` and `RetryRegistry` beans. |
| Summary log file | `reyga.config.logging.file.summary.enable` | `true` | Enables the dedicated Logback summary file appender. |
| Rolling application log | `reyga.config.logging.file.enable` | `false` | Enables the rolling file appender for application logs. |
| Async logging executor | No flag | always created | Registers the MDC-propagating bean named `asyncExecutor`. |

## E. Default Exception Handler

### Global handler

```yaml
reyga:
  config:
    default-bean:
      exception-handler: true
```

Enable this flag to return standard error responses:

- `IllegalArgumentException`: HTTP `400`, code `91`, message `GENERAL ERROR`;
- `JpaSystemException` and `DataAccessException`: HTTP `500`, code `99`, message `DATABASE ERROR`;
- Other exceptions: HTTP `500`, code `99`, message `INTERNAL SERVER ERROR`.

Disable this property if the application has its own global exception handler.
Avoid two handlers for the same exception to keep handler selection and response
format consistent.

### Validation and application fault handlers

Application fault handling remains available without enabling either flag.
Its behavior is:

- `AppFaultException`: status, code, and message from `AppFaultContent`;
- `IOFaultException`: HTTP `400` with file metadata safe for responses.

Thus, `exception-handler: false` disables only global and database handlers;
application fault handling remains available. HTTP validation error handling
follows the `validation-handler` flag.

## F. Enabling Validation

Enable `reyga.config.default-bean.validation-handler=true` to provide the built-in
validation bean. `reyga.custom.validation.map-details` controls error details
(default `true`). If the application already provides a `ValidationUtility` bean,
that manual bean takes precedence.

Manual/YAML bean tutorials, validation groups, and field annotations are covered
in the [Utilities Guide](UTILITIES_GUIDE_EN.md).

## G. HTTP Request/Response Logging

### Required configuration for the custom logging library

Projects using the library's built-in custom logging **must** select its Logback
configuration through `logging.config` in `application.yml` or
`application.properties`. This applies whether or not the `logging` module is
used on its own.

Example `application.yml`:

```yaml
#
# Use the library's built-in Logback configuration
#
logging:
  config: classpath:META-INF/logback-spring.xml
```

Alternatively, for projects using `application.properties`:

```properties
# Use the library's built-in Logback configuration
logging.config=classpath:META-INF/logback-spring.xml
```

Choose the format used by your project. If YAML already has a `logging` block,
add `config` to it instead of duplicating the `logging` key. Ensure the `logging`
artifact is on the runtime classpath.

Enabling only `reyga.config.default-bean.logging-handler` or setting
`reyga.config.logging.*` does not select this XML file. `logging.config` selects
the library's appender, pattern, and filter configuration; `logging-handler`
enables HTTP log collection. If the application deliberately uses its own Logback
XML, point `logging.config` to that file and configure the necessary foundation
appenders/filters as described below.

### Using the logging module on its own

Use the `logging` module and the following configuration when you only need
console output without rolling or summary files.

To use the built-in configuration in a Spring Boot application:

```yaml
logging:
  config: classpath:META-INF/logback-spring.xml
reyga:
  config:
    logging:
      file:
        enable: false
        summary:
          enable: false
```

This enables console output. HTTP log collection through request/response advice
still requires the runtime `foundation-starter` artifact and the `logging-handler`
flag below.

### Adding your own console filter

Extend `BaseConsoleLogFilter` and implement `filter(event)`. This example only
passes logs from the application package:

```java
package com.example.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import reyga.starter.foundation.logging.filter.BaseConsoleLogFilter;

public class ApplicationConsoleLogFilter extends BaseConsoleLogFilter {
    @Override
    protected FilterReply filter(ILoggingEvent event) {
        return event.getLoggerName().startsWith("com.example")
                ? FilterReply.NEUTRAL : FilterReply.DENY;
    }
}
```

In the console appender of the application's Logback configuration:

```xml
<filter class="com.example.logging.ApplicationConsoleLogFilter"/>
```

Use `logging.config` to select that application XML. The custom class must be
on the runtime classpath and have a no-argument constructor accessible to Logback.
The filter does not need `@Component`. For rolling or summary files, use
`BaseRollingLogFilter` or `BaseSummaryLogFilter` with the same pattern.

### Enabling HTTP logging

```yaml
reyga:
  config:
    default-bean:
      logging-handler: true
```

Logging applies to request/response bodies supported by HTTP message converters.
Logs include request ID, method, endpoint, remote address, username, user agent,
response time, request body, response body, exception, and some additional headers.
A new UUID is created when no request ID is available.

Consider data security when using this feature. The current implementation can
record access tokens, request bodies, and response bodies. Applications must
ensure masking/redaction, retention policies, and log file access match the
data classification.

### Standard logging context with `StarterHeaderEnum`

`StarterHeaderEnum` supplies consistent key names for HTTP headers, MDC, and
servlet request attributes in logging. Use `getValue()` instead of writing
key names manually.

```java
import org.springframework.http.HttpHeaders;
import reyga.starter.foundation.common.enumeration.StarterHeaderEnum;

HttpHeaders headers = new HttpHeaders();
headers.set(StarterHeaderEnum.REQUEST_ID.getValue(), requestId);
headers.set(StarterHeaderEnum.USERNAME.getValue(), username);
```

Despite the name `StarterHeaderEnum`, not every constant should be sent as an
HTTP header. Constants populated by the application or library serve as context
keys for a single request.

| Constant | Value | Category and purpose |
|---|---|---|
| `ACCESS_TOKEN` | `x-starter-access-token` | Optional HTTP header carrying an access token into log context. Sensitive; mask or omit it if security policy prohibits token logging. |
| `USERNAME` | `x-starter-user-name` | Optional HTTP header associating logs with the caller's username. |
| `REQUEST_ID` | `x-starter-request-id` | Cross-service correlation ID. The logging handler uses the incoming value or creates a UUID if absent. Forward this key in outbound requests to maintain trace continuity. |
| `METHOD` | `x-starter-request-method` | Context populated from the request's HTTP method, such as `GET` or `POST`. |
| `STATUS_CODE` | `x-starter-status-code` | Standard key for storing or forwarding HTTP response status in custom logging context. |
| `REQUEST_ENDPOINT` | `x-starter-request-uri` | Context populated with the URI of the endpoint being processed. |
| `FORWARDED_FOR` | `x-starter-forwarded-for` | Request source address context. The default logging handler populates it from the request's remote address. |
| `PACKAGE_INFO` | `x-starter-package-name` | Optional context for package, component, or execution scope names shown in summary logs. Applications may populate it through MDC. |
| `EXCEPTION` | `x-starter-request-exception` | Servlet request attribute holding an exception captured by an exception handler and included in the summary log. Not a client-supplied header. |
| `REQUEST` | `x-starter-request` | Servlet request attribute holding the request body captured by request body advice. Not a client-supplied header. |
| `RESPONSE` | `x-starter-response` | Servlet request attribute holding the response body captured by response body advice. Not a client-supplied header. |
| `USER_AGENT` | `x-starter-user-agent` | Optional HTTP header identifying the client in log context. |
| `RESPONSE_TIME` | `x-starter-response-time` | Request processing duration calculated by the logging handler in milliseconds. |
| `SUMMARY_LOG` | `x-starter-summary-log` | Internal MDC marker routing request completion events to the summary appender. Clients must not send or change it. |

Use the same constants when reading MDC in custom filters or loggers. Always
remove manually created context after processing to prevent request data from
leaking when a thread is reused.

```java
import org.slf4j.MDC;

try {
    MDC.put(StarterHeaderEnum.PACKAGE_INFO.getValue(), "customer-service");
    // Run operations that require logging context.
} finally {
    MDC.remove(StarterHeaderEnum.PACKAGE_INFO.getValue());
}
```

### Feature flags versus Logback output

`reyga.config.default-bean.logging-handler` controls HTTP log collection.
`reyga.config.logging.*` controls Logback formatting and appenders.
These settings are independent:

- The console appender is always configured.
- The rolling appender is active only when `rolling.enable: true`.
- The summary appender is enabled by default because `summary.enable` is `true`.
- Without `logging-handler`, HTTP summaries are normally not produced because
  the service does not set the `AFTER COMPLETION` MDC marker.

### Logback properties

```yaml
reyga:
  config:
    logging:
      console:
        pattern: "[%d{yyyy-MM-dd HH:mm:ss.SSS}] %X{x-starter-request-id} %-5level %logger{150} - %msg%n"
      file:
        enable: true
        pattern: "[%d{yyyy-MM-dd HH:mm:ss.SSS}] | %X{x-starter-request-id} | %-5level | %logger{150} | - %msg%n"
        file-path: logs/
        active-file-name: application.log
        file-name: "%d{dd-MM-yyyy}.app.log.%i.gz"
        clean-history-on-start: false
        summary:
          enable: true
          pattern: "%d{yyyy-MM-dd HH:mm:ss.SSS} | %X{x-starter-request-id} | %-5level | %logger{150} | - %msg%n"
          summary-active-file-name: monitoring/summary.log
          summary-file-name: "monitoring/%d{dd-MM-yyyy}.summary.log.%i.gz"
          summary-clean-history-on-start: false
        max-history: 30
        max-file-size: 20MB
        async-logs:
          core-pool-size: 5
          max-pool-size: 10
          queue-capacity: 100
          thread-name: async-logging-
```

| Property | Logback default | Purpose |
|---|---|---|
| `reyga.config.logging.console.pattern` | See example above | Console log pattern. |
| `reyga.config.logging.file.enable` | `false` | Enables regular application log files. |
| `reyga.config.logging.file.pattern` | See example above | Pattern for each regular log line. |
| `reyga.config.logging.file.file-path` | `logs/` | Base log directory; relative to the working directory unless absolute. |
| `reyga.config.logging.file.active-file-name` | `application.log` | Active application file receiving log events directly. |
| `reyga.config.logging.file.file-name` | `%d{dd-MM-yyyy}.app.log.%i.gz` | Application log archive naming pattern. |
| `reyga.config.logging.file.clean-history-on-start` | `false` | Cleans application archives according to retention at startup. |
| `reyga.config.logging.file.summary.enable` | `true` | Enables the summary file, which accepts only summary-marked events. |
| `reyga.config.logging.file.summary.pattern` | See example above | Pattern for each summary log line. |
| `reyga.config.logging.file.summary.summary-active-file-name` | `monitoring/summary.log` | Active summary filename under `file-path`. |
| `reyga.config.logging.file.summary.summary-file-name` | `monitoring/%d{dd-MM-yyyy}.summary.log.%i.gz` | Summary archive pattern under `file-path`. |
| `reyga.config.logging.file.summary.summary-clean-history-on-start` | `false` | Cleans summary archives according to retention at startup. |
| `reyga.config.logging.file.max-history` | `30` | Number of archive periods retained by Logback. |
| `reyga.config.logging.file.max-file-size` | `20MB` | Maximum size per archive before creating the next index. |

The table defaults apply when using
`logging.config=classpath:META-INF/logback-spring.xml`.

Active files are written as soon as the appender receives an event. `.gz` files
are archives created only during rollover by date or `max-file-size`.
Configuration inside a JAR is not monitored for hot reload; restart the application
after changing logging properties.

If the application provides its own `logback-spring.xml`, configure the required
foundation appenders/filters there or move all settings to the application file.

## H. Async Logging and MDC Propagation

Set logging executor capacity according to application load. Log context (MDC)
is propagated to asynchronous work so request identity remains traceable.

```yaml
reyga:
  config:
    logging:
      file:
        async-logs:
          core-pool-size: 5
          max-pool-size: 10
          queue-capacity: 100
          thread-name: async-logging-
```

| Property | Default | Purpose |
|---|---:|---|
| `reyga.config.logging.file.async-logs.core-pool-size` | `5` | Minimum worker thread count. |
| `reyga.config.logging.file.async-logs.max-pool-size` | `10` | Maximum worker thread count. |
| `reyga.config.logging.file.async-logs.queue-capacity` | `100` | Queue capacity before the pool grows toward its maximum. |
| `reyga.config.logging.file.async-logs.thread-name` | `async-logging` | Worker thread name prefix; a trailing `-` improves readability. |

The library uses the bean name `asyncExecutor`. Use a different name for additional
application executors to avoid conflicts.

## I. Default Utilities

```yaml
reyga:
  config:
    default-bean:
      utilities: true
```

Enable this flag to use the following APIs:

- `ResilienceService`: a facade for running suppliers/runnables with rate limiting,
  circuit breaking, retries, and fallbacks;
- `TransactionalExecutor`: execution with fallback and rollback on exceptions.

`TransactionalExecutor` requires a `PlatformTransactionManager`. If enabled without
an application transaction manager, context startup fails. This flag does not
create Resilience4j registries automatically; enable each resilience configuration
that you intend to use.

### Default HTTP client

Add the `common-http` artifact, then enable the built-in beans:

```yaml
reyga:
  config:
    default-bean:
      http-client: true
```

This creates one reusable `OkHttpClient` and one `StarterHttpClient`. To configure
timeouts, interceptors, proxies, authenticators, or TLS, declare your own
`OkHttpClient` bean; auto-configuration uses it without creating a second client.
An application-provided `StarterHttpClient` bean also takes precedence over the
default implementation.

Close every synchronous call's `Response` with try-with-resources. Asynchronous
callbacks are also responsible for closing their responses. Caching methods
follow HTTP cache headers and rules and do not own the lifecycle of a
consumer-supplied `Cache`.

### Registered HTTP APIs

Use `reyga.config.http-client.enabled=true` for YAML registrations and request
builders. The existing `reyga.config.default-bean.http-client` flag also enables
this configuration. Defaults: connect/read/write timeout `10s`, call timeout
`0ms`, redirects and connection recovery `true`, and an empty `register-api` list.
Each registration requires a unique `id` and an absolute HTTP(S) `base-url`.
Headers and query parameters are repeated name/value records; request payloads
are always supplied per request through the builder, while `response-type` names a response class.
Invalid registrations fail startup only while the HTTP feature is enabled.
Consumer transport and codec beans take precedence. See the
[HTTP client guide](HTTP_CLIENT_GUIDE_EN.md) for the full schema and overrides.

With complete endpoint/placeholder defaults, bodyless code only needs
`httpClient.operationBuilder().id("api-id").build()`. Body requests require
`id("api-id").requestBody(payload).build()`. request-body-required defaults to
true for POST/PUT/PATCH/PROPPATCH/REPORT and false otherwise; GET/HEAD prohibit
bodies. content-type/accept accept literal MIME types or symbolic media
enum names such as APPLICATION_JSON; YAML headers retain wire names. See the
[id/body example](HTTP_CLIENT_GUIDE.md#11-pemakaian-paling-sederhana-metadata-dari-yaml-payload-per-request).

## J. Rate Limiter

```yaml
reyga:
  config:
    rate-limiter:
      required: true
      timeout-milis: 60000
      max-request: 3
      refresh-period-seconds: 6000
```

| Property | Default | Purpose |
|---|---:|---|
| `required` | `false` | Creates `RateLimiterConfig` and `RateLimiterRegistry`. |
| `timeout-milis` | `60000` | Maximum permission wait in milliseconds. The property follows the library field spelling: `milis`, not `millis`. |
| `max-request` | `3` | Permissions per refresh period. |
| `refresh-period-seconds` | `6000` | Permission replenishment interval in seconds. |

`utilities: true` is required when using the registry through the default
`ResilienceService`. It is not required when the application injects and uses the
registry directly.

Inject `RateLimiterConfig` into controllers or services to use the YAML values.
There is no need to rebuild it with `RateLimiterConfig.custom()` when every
endpoint uses the global YAML settings.

For `ResilienceBaseController`, also enable
`reyga.config.circuit-breaker.required=true` because its constructor always requires
`CircuitBreakerRegistry`, even if the endpoint only calls rate limiter helpers.
Constructor injection and helper examples are in the
[Controller Implementation Guide](CONTROLLER_IMPLEMENTATION_GUIDE_EN.md#rate-limiter-example-using-yaml-configuration).

## K. Circuit Breaker

There are no circuit breaker parameter defaults other than `required`. When
enabling this feature, explicitly provide every value:

```yaml
reyga:
  config:
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

| Property | Default | Purpose |
|---|---:|---|
| `required` | `false` | Creates `CircuitBreakerConfig` and `CircuitBreakerRegistry`. |
| `failure-rate-threshold` | `0.0` | Failure percentage that opens the circuit. |
| `slow-call-rate-threshold` | `0.0` | Slow-call percentage that opens the circuit. |
| `slow-call-duration-threshold-seconds` | `0` | Minimum duration for a call to be considered slow. |
| `minimum-number-of-calls` | `0` | Minimum calls before calculating failure/slow-call rates. |
| `sliding-window-size` | `0` | Sliding window size. |
| `permitted-number-of-calls-in-half-open-state` | `0` | Calls permitted in half-open state. |
| `wait-duration-in-open-state-seconds` | `0` | Does not currently change the open duration; see behavior note below. |
| `automatic-transition-from-open-to-half-open-enabled` | `false` | Enables automatic transition to half-open. |

A bound value of `0` does not imply validity for Resilience4j. Enabling the feature
without values within Resilience4j bounds can cause startup failure.

> Current behavior: the wait duration in the open state follows
> `slow-call-duration-threshold-seconds`. Changing only
> `wait-duration-in-open-state-seconds` does not yet change that duration.
> Consider this relationship when configuring the circuit breaker.

## L. Retry

```yaml
reyga:
  config:
    retry:
      required: true
      max-attempts: 3
      wait-duration-millis: 10000
      fail-after-max-attempts: true
```

| Property | Default | Purpose |
|---|---:|---|
| `required` | `false` | Creates `RetryConfig` and `RetryRegistry`. |
| `max-attempts` | `3` | Total attempts, including the initial call. |
| `wait-duration-millis` | `10000` | Delay between attempts in milliseconds. |
| `fail-after-max-attempts` | `true` | Follows Resilience4j's `failAfterMaxAttempts` option. |

Each retry produces a warning log. Use retries only for operations safe to repeat,
such as reads or operations with an idempotency key.

## M. Aspect Advice

Each Aspect Advice has its own activation flag and behavior bean name:

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

Every flag set to `true` requires its corresponding behavior property to name a
Spring bean whose class extends that advice's base behavior. Tutorials covering
behavior creation, method annotations, lifecycle, exception handling, and
troubleshooting are in
[Aspect Advice in the Utilities Guide](UTILITIES_GUIDE_EN.md#k-aspect-advice).

## N. Current Feature Options

Use `logging-handler` to enable HTTP logging, rather than `request-response-advice`.
Advice for `around`, `before`, `after`, `after-returning`, and `after-throwing` is
available independently. Enable the required flags and set the corresponding
behavior properties to the appropriate bean names.

## O. Development Profile Example

```yaml
logging:
  config: classpath:META-INF/logback-spring.xml

reyga:
  config:
    default-bean:
      exception-handler: true
      validation-handler: true
      logging-handler: true
      utilities: true
    logging:
      file:
        enable: false
        summary:
          enable: false
```

This configuration keeps HTTP logs in the console without creating rolling or
summary files.

## P. Production Profile Example

```yaml
logging:
  config: classpath:META-INF/logback-spring.xml

reyga:
  config:
    default-bean:
      exception-handler: true
      validation-handler: true
      logging-handler: true
      utilities: true
    rate-limiter:
      required: true
      timeout-milis: 500
      max-request: 100
      refresh-period-seconds: 1
    retry:
      required: true
      max-attempts: 3
      wait-duration-millis: 500
      fail-after-max-attempts: true
    logging:
      file:
        enable: true
        file-path: /var/log/my-service/
        active-file-name: application.log
        file-name: "%d{dd-MM-yyyy}.app.log.%i.gz"
        clean-history-on-start: true
        summary:
          enable: true
          summary-active-file-name: monitoring/summary.log
          summary-file-name: "monitoring/%d{dd-MM-yyyy}.summary.log.%i.gz"
          summary-clean-history-on-start: true
        max-history: 30
        max-file-size: 20MB
        async-logs:
          core-pool-size: 5
          max-pool-size: 20
          queue-capacity: 1000
          thread-name: async-logging-
```

The capacity, timeout, rate limit, and retention values above are examples, not a
universal baseline. Adjust them through load testing, SLAs, instance counts, and
application resource limits. Ensure the log directory exists and is writable by
the process.

## Q. Environment Variables

Spring Boot relaxed binding allows properties to be supplied through environment
variables. For example:

```text
REYGA_CONFIG_DEFAULT_BEAN_LOGGING_HANDLER=true
REYGA_CONFIG_LOGGING_FILE_ENABLE=true
REYGA_CONFIG_LOGGING_FILE_FILE_PATH=/var/log/my-service/
REYGA_CONFIG_LOGGING_FILE_ASYNC_LOGS_MAX_POOL_SIZE=20
```

## R. Troubleshooting Checklist

| Symptom | Check |
|---|---|
| `ValidationUtility` bean missing | Create a bean with `ValidationConfig.builder()` or enable `reyga.config.default-bean.validation-handler=true`. |
| `StarterHttpClient` bean missing | Add `common-http` and enable `reyga.config.default-bean.http-client=true`, or declare your own bean. |
| Context fails because of `PlatformTransactionManager` | Disable `utilities` or provide a transaction manager. |
| Context fails because of `JdbcTemplate` | Ensure application JDBC dependencies, driver, and datasource configuration are available. |
| Context fails when circuit breaker is enabled | Supply all parameters with valid Resilience4j values. |
| Request/response not logged | Enable `logging-handler` and ensure requests pass through an HTTP message converter. |
| Built-in library patterns or appenders not used | Ensure `logging.config=classpath:META-INF/logback-spring.xml` is active and the `logging` artifact is on the runtime classpath. |
| Empty summary file | Enable `logging-handler`; summaries accept only events with the summary MDC marker. |
| Log files not created | Check `rolling.enable`/`summary.enable`, resolved `file-path`, and permissions; look for `application.log` or `monitoring/summary.log`. `.gz` files are created only on rollover. |
| Duplicate/ambiguous beans | Avoid duplicate bean names and do not enable library and application handlers for the same exception simultaneously. |

## S. Auto-Configuration Pattern for Starter Development

Java configuration classes stay in their feature packages under
`foundation-starter-internal/src/main/java`.
`src/main/resources/META-INF/spring` contains metadata listing fully qualified
class names; Java classes do not belong in the resource directory.

### Entry points and feature groups

`org.springframework.boot.autoconfigure.AutoConfiguration.imports` continues
to register these three entry points:

- `FoundationDefaultAutoConfiguration`: the default group for common, database,
  I/O, logging, exception handling, resilience, utilities, and Aspect Advice.
- `ValidationAutoConfiguration`: the validation utility with its existing flag.
- `CommonHttpAutoConfiguration`: the HTTP client with its existing flag.

`FoundationDefaultAutoConfiguration` uses `@ImportAutoConfiguration`.
Its default features are listed in the companion resource
`META-INF/spring/reyga.starter.foundation.defaults.config.FoundationDefaultAutoConfiguration.imports`:

| Auto-configuration | Responsibility |
|---|---|
| `CommonAutoConfiguration` | Imports the existing logger and logger injector factories |
| `CommonDatabaseAutoConfiguration` | Imports the existing query processor factory |
| `CommonIOAutoConfiguration` | Imports the existing file inspector and sanitizer factories |
| `LoggingAutoConfiguration` | Imports the existing logging service and MVC configuration factories |
| `AsyncLoggingAutoConfiguration` | Imports the existing asynchronous executor factory with MDC propagation |
| `ExceptionHandlingAutoConfiguration` | Application fault, validation fault, and generic exception handlers |
| `ResilienceAutoConfiguration` | Configuration and registry factories for rate limiting, circuit breaking, and retry |
| `UtilitiesAutoConfiguration` | Resilience service and transactional executor |
| `AspectAdviceAutoConfiguration` | Five Aspect Advice dispatchers and behavior bean validation |

This grouping preserves the existing exclusion behavior: excluding
`FoundationDefaultAutoConfiguration` through `spring.autoconfigure.exclude`
disables the entire default group. The validation utility and HTTP client retain
their independent entry points and flags.

`CoreConfig` remains available as a compatibility configuration for explicit
imports and existing factory method calls. Its methods delegate bean creation
to the feature configurations. The four extracted core auto-configurations use
`@ConditionalOnMissingBean(CoreConfig.class)` to prevent duplicate beans when
the legacy configuration is imported explicitly. This guard does not introduce
a new policy for consumer overrides of default beans.

### Rules for adding features

1. Create one `*AutoConfiguration` per responsibility with `@AutoConfiguration`;
   avoid scanning the entire starter package.
2. Use `@EnableConfigurationProperties` for feature properties and retain
   explicit runtime activation through `reyga.config.*`.
3. Register features belonging to the default group in the companion
   `FoundationDefaultAutoConfiguration.imports` resource. Reserve the top-level
   list for independent entry points; assess exclusion behavior before changing
   group membership.
4. Keep helper configurations annotated with `@Configuration` and import them
   from the feature auto-configuration. When ordering matters, declare it with
   `@AutoConfiguration(before = ..., after = ...)` rather than relying on
   metadata line order.
5. Use `@ConditionalOnMissingBean` for new defaults that support consumer
   overrides. Do not add conditions to existing factories during a refactor,
   since that can change application behavior.
6. Verify metadata discovery, defaults and disabled flags, feature activation,
   missing requirements, and compatibility imports and exclusions with
   `ApplicationContextRunner`.

This refactor preserves bean names, properties, default values, implementations,
logging, and startup validation. A `JdbcTemplate` remains required for the query
processor; a `PlatformTransactionManager` remains required when utilities are
enabled. Existing logging, async, common, database, and I/O configurations retain
their original factories and configuration proxies. Consumer dependencies and
`ServiceLoader` registration remain unchanged.
