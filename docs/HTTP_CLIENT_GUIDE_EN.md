# Starter HTTP Client Guide

The public `common-http` artifact supplies `StarterHttpClient`,
`HttpOperations<T, R>`, and `HttpBodyCodec`. The `foundation-starter` runtime
supplies implementations and auto-configuration. Installation examples are in
the [utilities guide](UTILITIES_GUIDE_EN.md#l-common-http-client).

## Configuration

```yaml
reyga:
  config:
    http-client:
      enabled: true
      connect-timeout: 5s
      read-timeout: 20s
      write-timeout: 20s
      call-timeout: 30s
      follow-redirects: false
      retry-on-connection-failure: false
      register-api:
        - id: customer-api
          base-url: https://customer.example.com/v1
          path: customers/{customerId}
          method: GET
          content-type: application/json
          accept: application/json
          headers:
            - name: Authorization
              value: "Bearer ${CUSTOMER_API_TOKEN}"
          query-parameters:
            - name: include
              value: address
          response-type: com.example.customer.CustomerResponse
          call-timeout: 10s
        - id: create-customer-xml
          base-url: https://customer.example.com/v1
          path: customers
          method: POST
          content-type: application/xml
          request-body-required: true
          response-type: com.example.customer.CustomerResponse
```

`register-api`, `headers`, and `query-parameters` are lists of records. Repeated
header/query names are supported. Optional `path-variables` is a map of default
placeholder values. There is no starter-defined registration count limit.

| Setting | Default |
|---|---|
| `enabled` | `false` |
| `connect-timeout`, `read-timeout`, `write-timeout` | `10s` each |
| `call-timeout` | `0ms` (unlimited) |
| `follow-redirects`, `retry-on-connection-failure` | `true` |
| `register-api` | empty list |
| Registration `id`, `base-url` | required; unique id and absolute HTTP(S) URL |
| Registration `path` | empty string |
| Registration `method` | `GET`, normalized to uppercase |
| Registration `content-type` | `application/json` |
| Registration `accept` | same as content type |
| Registration `headers`, `query-parameters`, `path-variables` | empty collections |
| Registration `request-body-required` | true for POST/PUT/PATCH/PROPPATCH/REPORT; false otherwise |
| Registration `response-type`, `call-timeout` | absent |

The compatibility flag `reyga.config.default-bean.http-client=true` also enables
HTTP support. Either flag enables the configuration; with both inactive, no HTTP
infrastructure is created or validated. Invalid URLs, duplicate ids, blank or
unresolved required values, invalid GET/HEAD body requirements, and unloadable response classes
fail startup. Path placeholders without defaults must be supplied at build time.
Timeouts must be zero or between `1ms` and `2147483647ms`.

Paths append to the base URL path even with a leading slash. Use query parameters
for queries. Paths cannot contain absolute URLs, query/fragment suffixes, or
`.`/`..` segments. Base URLs cannot contain userinfo or fragments. Path variables
are encoded as single segments, including embedded slashes.

## Builder and execution

### Simplest use: id without a body, id and payload with a body

Endpoint metadata remains in YAML. No request payload or payload bean reference
is stored there. GET/HEAD and other bodyless operations use only id; requests
with a body must use a registered id and requestBody(payload):

```java
var get = httpClient.<Void, CustomerResponse>operationBuilder()
        .id("customer-config-only").build();
var create = httpClient.<CustomerRequest, CustomerResponse>operationBuilder()
        .id("create-customer-config-only")
        .requestBody(new CustomerRequest("Ada"))
        .build();
```

See the [complete GET/POST YAML and execution example](HTTP_CLIENT_GUIDE.md#11-pemakaian-paling-sederhana-metadata-dari-yaml-payload-per-request).
All path placeholders need configured defaults for minimal code. Runtime response
type comes from YAML; Java generic arguments help source typing. DTOs are created
by consumers on each request and do not need Spring beans. Execution and response
closure remain caller responsibilities; async uses the same builder rules.

request-body-required is metadata only: it defaults to true for the final method
POST/PUT/PATCH/PROPPATCH/REPORT and false for other methods. Set true for a
DELETE/custom endpoint that requires payload. Set false for a POST endpoint whose
upstream contract has no payload; id alone then produces an empty transport body.
False permits an optional body, it does not forbid one. GET/HEAD always reject
payloads and cannot configure request-body-required=true.

Missing/null required payload fails build. An explicit empty String/byte array
is allowed when required by the upstream contract. Any supplied body requires an
id, including forms, multipart, and binary. Direct builders without id remain
available for bodyless requests; low-level OkHttp Request APIs remain unchanged.

### Enum and String support

Import `HttpMethod`, `HttpContentType`, and `HttpHeader` from
`reyga.starter.foundation.common_http.enums`. All String setters remain supported:

```java
var operation = httpClient.<CustomerRequest, CustomerResponse>operationBuilder()
        .id("create-customer-config-only")
        .method(HttpMethod.POST)
        .contentType(HttpContentType.APPLICATION_XML)
        .accept(HttpContentType.APPLICATION_XML)
        .header(HttpHeader.AUTHORIZATION, "Bearer " + token)
        .addHeader(HttpHeader.X_REQUEST_ID, requestId)
        .requestBody(new CustomerRequest("Ada"))
        .build();
```

Methods: GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE, CONNECT, PROPPATCH,
REPORT. Header enum values: ACCEPT, CONTENT_TYPE, AUTHORIZATION,
PROXY_AUTHORIZATION, CACHE_CONTROL, IF_NONE_MATCH, IF_MODIFIED_SINCE, USER_AGENT,
COOKIE, X_REQUEST_ID. Header values remain dynamic Strings. Header/media `value()`
returns the wire string; method `name()` returns its wire token.

Media constants: APPLICATION_JSON, APPLICATION_XML, TEXT_XML, TEXT_PLAIN,
TEXT_HTML, APPLICATION_FORM_URLENCODED, MULTIPART_FORM_DATA, APPLICATION_OCTET_STREAM,
APPLICATION_PDF, APPLICATION_PROBLEM_JSON, APPLICATION_JSON_PATCH,
APPLICATION_MERGE_PATCH_JSON. See the
[wire-value table](HTTP_CLIENT_GUIDE.md#12-enum-dan-string-pada-builder-dan-yaml).
Non-JSON/XML structured formats still require a custom codec or raw body.

YAML method values such as GET are textual tokens. YAML content-type/accept and
default-builder String media setters accept literal MIME types or media enum
names without case sensitivity (`APPLICATION_JSON`, `application_xml`). Unknown
enum names fail media validation; valid custom MIME strings remain supported.
YAML header names use wire names (`Content-Type`), not symbolic CONTENT_TYPE.
Use Strings for charset/boundary parameters. Enum overloads reject null; cast
null to the intended type in tests to select the overload. Override behavior and
defaults are unchanged.

Inject `StarterHttpClient` through a constructor. Build one operation per request:

```java
HttpOperations<Void, CustomerResponse> operation = httpClient
        .<Void, CustomerResponse>operationBuilder()
        .id("customer-api")
        .pathVariable("customerId", customerId)
        .header("X-Request-Id", requestId)
        .queryParameter("include", "address")
        .addQueryParameter("include", "contact")
        .callTimeout(Duration.ofSeconds(8))
        .build();

try (Response response = httpClient.execute(operation)) {
    if (!response.isSuccessful()) {
        throw new IOException("Upstream HTTP " + response.code());
    }
    CustomerResponse customer = operation.readResponse(response);
}
```

The simplest registered operation is
`httpClient.operationBuilder().id("customer-config-only").build()` for a bodyless endpoint; a body request also requires requestBody.
Without an id, supply `baseUrl(...)`; GET/JSON are defaults.
Overrides apply after YAML defaults regardless of setter order. Available setters
are `id`, `baseUrl`, `path`, `method`, `header`, `addHeader`, `queryParameter`,
`addQueryParameter`, `pathVariable`, `contentType`, `accept`, `requestBody`,
`responseType`, and `callTimeout`.

`header` replaces values without case sensitivity; `addHeader` appends.
`queryParameter` replaces matching query values; `addQueryParameter` appends.
`contentType` also changes the default accept when request/response representations
were identical. Explicit media setters replace registered media headers; explicit
header setters then take precedence in their own order.
`requestBody(null)` clears the current builder payload and fails if a body is required; `responseType(null)` disables
decoding. `responseType` also accepts a parameterized `java.lang.reflect.Type`.

`build()` validates and serializes without network I/O. Missing values, unknown
ids, invalid URLs/headers, missing placeholders, GET/HEAD bodies, and serialization
failures throw `IllegalArgumentException`; serialization causes are retained.

`execute` returns raw `okhttp3.Response`, including non-2xx statuses.
`executeAsync(operation, callback)` returns an already enqueued `okhttp3.Call`.
Keep the Call for cancellation. Callbacks run on the OkHttp dispatcher; close the
received response, including on parsing/status failure. Transport errors remain
`IOException`. `readResponse` optionally consumes the body once, does not check
the status, and returns null for empty bodies. Callers own response closure.

## Body models and customization

Payloads are supplied exclusively through requestBody on each request. Request
DTOs and response models do not need Spring beans. Use immutable explicit DTOs;
the body is serialized into a snapshot at build time. The response-type YAML
property still names the response class/record for optional decoding.
The default codec uses Jackson 2 JSON/XML, aligned to `jacksonDatabindVersion`,
without modifying Spring Boot's mapper. Structured payloads require UTF-8.
Strings and byte arrays are raw documents; arrays are copied. OkHttp `RequestBody`
is supported for custom formats/streaming, with content type and replayability
owned by callers. POST/PUT/PATCH require a per-request payload by default; GET/HEAD
reject payloads. Buffered prepared operations are reusable; builders are mutable
and must not be shared between threads.

DTD and external entity resolution are disabled for XML decoding. JSON/XML suffix
media types are supported. XML namespaces, roots, and collections follow the DTO
mapping and Jackson XML capabilities; raw XML can represent specialized schemas.
Provide a custom `HttpBodyCodec` bean to change mapper settings or formats.

Consumer `OkHttpClient`, `HttpBodyCodec`, `HttpOperationRegistry`, and
`StarterHttpClient` beans take precedence. Transport properties do not modify
custom OkHttp beans. The default implementation applies operation timeout on each
new Call while sharing transport infrastructure. Custom starter implementations
remain compatible but must implement `operationBuilder` and override execution
methods to honor their own operation timeouts.

Existing `call`, `asynchronousCall`, and `callWithResponseCaching` APIs remain.
Pass `operation.request()` to the caching API when needed; operation timeout is
applied by the default `execute`/`executeAsync` methods. Configure caching,
interceptors, proxy, authentication, and TLS using a consumer OkHttp bean.
Avoid logging sensitive payloads/credentials and automatic business retries for
non-idempotent requests. Keep tokens in environment variables or a secret store.

## Complete usage reference

The [Indonesian canonical guide](HTTP_CLIENT_GUIDE.md) contains the full installation,
DTO/gateway source, property tables, and examples for every case below. Fragment
examples assume a constructor-injected `httpClient`; application inputs such as
customer ids, tokens, upload paths, and download destinations belong to the consumer.
Public DTOs belong in separate files and YAML class names must match their packages.

### Installation and models

Use the foundation BOM version `1.1.0`, `common-http` on the compile classpath,
and `foundation-starter` at runtime. See
[installation in all three build formats](HTTP_CLIENT_GUIDE.md#0-instalasi-dan-constructor-injection)
for Gradle Kotlin/Groovy and Maven examples. A runtime-only starter is sufficient
when consumer source imports only public HTTP contracts and OkHttp. If source
imports runtime implementation/configuration classes, make the starter a compile
dependency (`implementation` in Gradle, default Maven scope).

Examples use `com.example.customer.CustomerRequest(String name)` and
`CustomerResponse(String id, String name)` records. Neither response models nor
explicit builder request DTOs need Spring bean registration.

### Every entry point and setter

| API | Result/behavior |
|---|---|
| `operationBuilder()` | New mutable `HttpOperations.Builder<T, R>`. |
| `execute(operation)` | Synchronous raw `Response`; caller closes it. |
| `executeAsync(operation, callback)` | Already enqueued `Call`; do not enqueue/execute it again. |
| `call(request)` | Synchronous raw Request API. |
| `asynchronousCall(request, callback)` | Asynchronous raw Request API. |
| `callWithResponseCaching(request, cache)` | Synchronous caller-selected cache API. |
| `operation.request()` | Prepared Request snapshot, without network I/O. |
| `operation.callTimeout()` | Override Duration or null to inherit transport timeout. |
| `operation.readResponse(response)` | Optional one-shot decoding; null for an empty body. |

Use `httpClient.operationBuilder()`, not a nonexistent static
`HttpOperations.builder()`. The empty compatibility constructor cannot execute requests.

| Builder setter | Contract |
|---|---|
| `id(String)` | Optional, nonblank registered id; unknown ids fail at build. |
| `baseUrl(String)` | Required without id; replaces the full base URL. |
| `path(String)` | Replaces registration path; empty clears it, null is invalid. |
| `method(String/HttpMethod)` | Nonblank letters only or non-null enum; normalized uppercase. |
| `header` / `addHeader` | String/HttpHeader name, non-null value; replace/append without name case sensitivity. |
| `queryParameter` / `addQueryParameter` | Nonblank name, non-null value; replace/append with case-sensitive names. |
| `pathVariable` | Replaces one encoded placeholder value; non-null, empty rejected at build. |
| `contentType` / `accept` | String MIME/enum name or HttpContentType; Accept expects a single media type. |
| `requestBody` | Per-request payload; supplied body requires id; required body cannot be null. |
| `responseType(Type)` | Runtime class/parameterized Type; null disables decoding. |
| `callTimeout(Duration)` | Non-null valid duration; zero disables the overall limit. |
| `build()` | Validates and serializes without sending requests. |

Scalar setters use the last value. The last requestBody setter replaces the current payload. Explicit
overrides apply after registration defaults regardless of when `id` is called.
Header/query replace/append setters retain their own order. Query replacement
also replaces values originally present in the base URL. No default header/query
removal setter exists; an empty value still sends an entry. Use another registration
or a direct builder when a default must be absent. Path clearing keeps the base URL path.
Do not pre-encode query/path values. See
[precedence and URL examples](HTTP_CLIENT_GUIDE.md#5-referensi-api-dan-prioritas-override).

Media headers are applied in this order: final media fields, YAML header list,
explicit contentType/accept setters, then ordered header/addHeader setters.
An explicitly registered Accept header can therefore override an inferred Accept
change from contentType; use accept explicitly to replace it.

### HTTP method cases

| Method | Without payload | With payload |
|---|---|---|
| GET / HEAD | No request body | Rejected. |
| POST / PUT / PATCH | Rejected by default; body-required=false allows empty transport body | Serialized/raw payload. |
| DELETE / OPTIONS | No request body | Allowed by builder; upstream defines semantics. |
| PROPPATCH / REPORT | Rejected by default; body-required=false allows empty transport body | Serialized/raw payload. |
| Other letter-only methods | OkHttp validation applies | OkHttp/upstream validation applies. |

```java
var create = httpClient.<CustomerRequest, CustomerResponse>operationBuilder()
        .id("create-customer-config-only")
        .baseUrl("https://customer.example.com/v1").path("customers")
        .method("POST").requestBody(new CustomerRequest("Ada"))
        .responseType(CustomerResponse.class).build();
try (Response response = httpClient.execute(create)) {
    if (response.code() != 201) {
        throw new IOException("Expected HTTP 201, received " + response.code());
    }
    CustomerResponse customer = create.readResponse(response);
}
```

Use PUT for the upstream replacement contract, PATCH with the upstream patch
document/media type, DELETE without decoding when no body is expected, HEAD for
headers such as ETag, and OPTIONS for Allow. The starter does not construct patch
documents. See [CRUD and metadata examples](HTTP_CLIENT_GUIDE.md#6-case-method-http-dan-body-kosong).

### JSON, XML, generics, and raw payloads

Nested DTOs, Maps, lists, and arrays can be structured request payloads when
supported by the codec. Generic `T`/`R` declarations alone do not specify the
runtime decoding target. For generic response wrappers, supply a reflective Type:

```java
// Import com.fasterxml.jackson.core.type.TypeReference and java.util.List.
var target = new TypeReference<List<CustomerResponse>>() {}.getType();
var operation = httpClient.<Void, List<CustomerResponse>>operationBuilder()
        .baseUrl("https://customer.example.com/v1").path("customers")
        .responseType(target).build();
```

Declare Jackson 2 databind as a compile dependency when importing TypeReference;
`CustomerResponse[].class` is an alternative requiring no Jackson TypeReference.
YAML class names cannot contain generic type expressions. Spring Boot 4's Jackson
3 API uses a different package and must not be mixed with Jackson 2 APIs.

Raw Strings are sent unchanged without JSON quoting or XML validation; send a
complete document. Structured payloads require UTF-8, raw String encoding follows
the OkHttp media charset, and bytes must already have the intended encoding.
XML DTO root/namespace annotations require `jackson-dataformat-xml` on the compile
classpath, aligned to the repository Jackson version. JSON auto-discovers mapper
modules; the default XML mapper does not. XML custom modules/Java time need a
consumer codec. SOAP envelopes, XML signatures, schema validation, and WS-Security
require consumer implementation. See
[generic and XML DTO examples](HTTP_CLIENT_GUIDE.md#7-case-json-xml-dan-generic-response).

Decoding selects the actual response Content-Type, falling back to the operation
Accept only when that header is absent. Accept does not transform response bytes.
Nonempty text/plain or text/html fails default structured decoding. Invalid or
empty Content-Type is not silently treated as JSON. `responseType(String.class)`
deserializes a JSON/XML String model; read `response.body().string()` for raw text.

### Form, multipart, binary upload, and streaming download

Use OkHttp `FormBody` for form URL encoding and `MultipartBody` for file upload.
Pass either as `requestBody` with `T=RequestBody`; they bypass the body codec.
Use the multipart body's generated content type/boundary rather than hardcoding
a boundary. File sources must remain available through completion, including
async requests. Raw RequestBody replayability and effective content type remain
owned by the caller. Raw byte arrays are copied during build.

```java
RequestBody form = new FormBody.Builder().add("name", "Ada").build();
var upload = httpClient.<RequestBody, Void>operationBuilder()
        .id("submit-customer-form")
        .baseUrl("https://customer.example.com").path("forms/customer")
        .method("POST").contentType("application/x-www-form-urlencoded")
        .requestBody(form).build();
```

For large downloads, stream instead of decoding:

```java
var download = httpClient.operationBuilder()
        .baseUrl("https://customer.example.com").path("exports/latest")
        .accept("application/octet-stream").build();
try (Response response = httpClient.execute(download)) {
    if (!response.isSuccessful()) {
        throw new IOException("Download failed with HTTP " + response.code());
    }
    try (InputStream input = response.body().byteStream()) {
        Files.copy(input, destination);
    }
}
```

Import java.io.InputStream and java.nio.file.Files. The example rejects existing
targets; interrupted downloads can leave partial files. Validate output paths
and sizes, or write a temporary file and move after success. `readResponse` buffers
the whole body and provides no size limit. See
[form, multipart, and binary examples](HTTP_CLIENT_GUIDE.md#8-form-multipart-upload-dan-download).

### Errors and response ownership

| Stage | Outcome |
|---|---|
| Invalid configuration | Startup failure while the HTTP feature is enabled. |
| Invalid operation/payload | IllegalArgumentException; serialization causes retained. |
| Null mandatory Java arguments | May throw NullPointerException. |
| DNS/TLS/connectivity/timeout/cancellation | IOException on sync, onFailure on async. |
| HTTP non-2xx | Raw Response, not onFailure solely because of status. |
| Malformed body/unsupported media/schema mismatch | IOException during decoding. |
| Missing response target | IllegalStateException before consuming the body. |

Empty bodies, including HEAD/204, decode to null; JSON literal null may also
decode to null. Check status before choosing the model. Decode success and error
bodies through separate branches; do not read the same body twice. Preserve causes
when wrapping errors. Retry-After, 404/409/429 policies, pagination, fallback, and
status retries belong to the consumer. Close responses before retrying. See
[error DTO example](HTTP_CLIENT_GUIDE.md#9-status-http-response-kosong-dan-error-handling).

### Async future adapter and cancellation

The starter returns Call, not CompletableFuture. A consumer adapter must complete
its future on both callback branches and catch parsing/status exceptions inside
onResponse. An exception thrown by onResponse does not automatically route to
onFailure. Propagate future cancellation to `call.cancel()` explicitly. A timeout
while waiting on `future.get(...)` does not itself cancel the network request.
Build/scheduling failures remain synchronous before callback registration.
Cancellation can race with completion, so callbacks still close received responses.
See the [complete future adapter](HTTP_CLIENT_GUIDE.md#10-async-dengan-completablefuture-dan-cancellation).

Callbacks use the OkHttp dispatcher. MDC, Spring transactions, and request-thread
context are not automatically propagated. Hand off decoded values to an application
executor for long work after consuming/closing the response.

### Timeouts, authentication, redirects, and TLS

Global connect/read/write and call properties configure only the default transport.
A consumer OkHttp bean keeps its own settings. Registration call-timeout overrides
the overall timeout on each default-starter Call; builder callTimeout wins, including
zero. Raw/caching APIs accept only Request and do not carry operation timeout metadata.
There are no builder connect/read/write setters. Connect/read/write limits are not
the total-operation limit. Global call timeout zero does not disable read/write limits.

Use `header("Authorization", "Bearer " + token)` for dynamic bearer authentication
or `Credentials.basic(username, password)` for Basic authentication. Replace rather
than append Authorization. YAML tokens are bound at startup; rotating credentials
fit a builder/interceptor. Do not embed credentials in base URL userinfo or reuse
credential-bearing registration defaults with an untrusted overridden host.

Supply an OkHttp bean for interceptors, proxy, Authenticator, certificate pinning,
trust configuration, and advanced redirect behavior. Preserve TLS/hostname validation.
Interceptors are shared and must be thread-safe; they can override headers after
the operation builder. Bound authenticator refresh attempts to prevent 401 loops.
`follow-redirects` configures followRedirects, not every custom policy such as
followSslRedirects. See [transport examples](HTTP_CLIENT_GUIDE.md#11-timeout-authentication-interceptor-dan-tls).

### Caching and legacy Request APIs

Configure one reusable Cache on a consumer OkHttp bean and close it at application
shutdown, not per response. Use one Cache per directory. Caching follows HTTP
method/status/server directives; adding request Cache-Control does not force
non-cacheable responses into the cache. Revalidation can involve cache and network.
An unsatisfied only-if-cached request can return raw HTTP 504.

```java
var operation = httpClient.operationBuilder()
        .baseUrl("https://customer.example.com").path("catalog").build();
try (Response response = httpClient.callWithResponseCaching(operation.request(), cache)) {
    boolean cacheUsed = response.cacheResponse() != null;
    String body = response.body().string();
}
```

The caller owns the cache passed to the selected-cache API. That API shares pool
and dispatcher but does not apply operation timeout overrides. Use a cached
transport plus execute when both cache and per-operation timeout are needed.
Raw Request calls bypass registration/body-codec defaults. See
[cache bean and raw Request examples](HTTP_CLIENT_GUIDE.md#12-response-cache-dan-api-request-lama).

### Per-request payloads, custom codecs, and consumer clients

Each request builds its own payload snapshot. Registry metadata is shared but
payload data is not resolved from a singleton/prototype bean or configuration.
The removed request-body-bean YAML key and requestBodyBean builder method must be
migrated to id(...).requestBody(payload).build(). Old YAML payload source keys
are rejected at startup with a migration message. request-body-required carries
only contract metadata. All endpoint registrations remain validated at startup.
Use a consumer HttpBodyCodec to change mapper settings, handle unknown fields, add
modules, or support structured formats. A JSON-customized codec can delegate XML
to DefaultHttpBodyCodec; importing that implementation requires the starter at
compile time. Configure shared mappers before concurrent use. See the
[complete codec factory](HTTP_CLIENT_GUIDE.md#13-payload-per-request-dan-custom-codec).

Custom HttpOperationRegistry beans replace default registration/validation behavior.
Custom StarterHttpClient implementations must supply operationBuilder; the legacy
interface default throws UnsupportedOperationException. Interface execute methods
delegate to raw Request APIs and do not apply operation timeout automatically.
No AOP proxy is required for the client. Consumer transaction annotations follow
normal Spring proxy limits and do not make asynchronous network work transactional.

### Profiles, environment, lifecycle, and testing

Profiles replacing register-api replace the whole list rather than append entries.
Provide every required registration in an overriding profile. Environment examples:

```text
REYGA_CONFIG_HTTPCLIENT_ENABLED=true
REYGA_CONFIG_HTTPCLIENT_CONNECTTIMEOUT=5s
REYGA_CONFIG_HTTPCLIENT_CALLTIMEOUT=30s
CUSTOMER_API_TOKEN=<value from secret store>
```

For binding/list replacement rules see
[Spring Boot external configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html).
The registry is a context snapshot, not a dynamic YAML watcher. Prepared buffered
payloads remain unchanged after the builder/input DTO changes. Do not share mutable
builders, reuse already executed Calls, or shut down shared transports per request.
Consumer-owned transports/executors/caches need shutdown lifecycle management.
See [profiles/lifecycle](HTTP_CLIENT_GUIDE.md#14-profiles-environment-dan-lifecycle).

Mock the gateway/StarterHttpClient for service unit tests. Use a local server or
synthetic-response OkHttp interceptor for HTTP integration tests. Cover non-2xx,
empty/malformed bodies, cancellation, duplicates/overrides, generic DTOs, and response
closure. ApplicationContextRunner covers feature flags and invalid registrations.
See the [JUnit example](HTTP_CLIENT_GUIDE.md#15-testing-consumer) and repository
[context tests](../foundation-starter-internal/src/test/java/reyga/starter/foundation/common_http/config/HttpRegistrationConfigurationTest.java).

### Troubleshooting and current limits

| Symptom | Check |
|---|---|
| Missing client bean | Runtime artifact and either activation flag. |
| Disabled new flag still creates client | Compatibility flag may still be true. |
| Unknown id or missing placeholder | Active profile list, exact id, and all path values. |
| Unexpected URL prefix | Paths append to base URL, including leading slash. |
| Missing/null body or body without id | Supply id and the per-request payload; review the endpoint body requirement. |
| Unloadable response model | Fully qualified class name; generic targets belong in builder Type. |
| Parsing fails on HTTP 200 | Actual Content-Type/schema/charset, or HTML authentication page. |
| Timeout setting ignored | Custom transport or raw/caching Request API. |
| Async future hangs after parsing failure | Catch failures in onResponse and complete exceptionally. |
| Cache miss | HTTP caching directives and single writable cache directory. |
| Builder unsupported | Consumer client must implement operationBuilder. |

Current API has no registry hot reload, default header/query removal, per-operation
connect/read/write limits, DTO-response size limit, built-in typed async callback,
HTTP status retry, SOAP implementation, WebSocket, or SSE adapter. Use consumer
configuration/adapters or dedicated OkHttp APIs. Unsupported YAML keys do not
introduce those behaviors. Registry/codec/transport defaults are global beans,
not automatically separate clients for each registration id.
