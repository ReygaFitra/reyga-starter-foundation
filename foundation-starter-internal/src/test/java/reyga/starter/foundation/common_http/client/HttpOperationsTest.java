package reyga.starter.foundation.common_http.client;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reyga.starter.foundation.common_http.enums.HttpContentType;
import reyga.starter.foundation.common_http.enums.HttpHeader;
import reyga.starter.foundation.common_http.enums.HttpMethod;
import reyga.starter.foundation.common_http.config.HttpClientProperties;
import reyga.starter.foundation.common_http.config.HttpClientProperties.Api;
import reyga.starter.foundation.common_http.config.HttpClientProperties.NameValue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpOperationsTest {
    private final HttpBodyCodec codec = new DefaultHttpBodyCodec();

    @ParameterizedTest
    @EnumSource(HttpMethod.class)
    void should_UseWireMethod_When_EnumMethodIsSelected(HttpMethod method) {
        boolean needsBody = List.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH,
                HttpMethod.PROPPATCH, HttpMethod.REPORT).contains(method);
        var builder = client(api(method.name(), null, List.of())).operationBuilder().id("customer").method(method);
        if (needsBody) {
            builder.requestBody("");
        }
        var operation = builder.build();
        assertEquals(method.name(), operation.request().method());
        assertEquals(needsBody, operation.request().body() != null);
    }

    @ParameterizedTest
    @EnumSource(HttpContentType.class)
    void should_UseMimeValues_When_EnumMediaTypesAreSelected(HttpContentType contentType) {
        var operation = new DefaultStarterHttpClient().operationBuilder()
                .baseUrl("https://example.test").contentType(contentType).accept(contentType).build();

        assertEquals(contentType.value(), operation.request().header("Content-Type"));
        assertEquals(contentType.value(), operation.request().header("Accept"));
    }

    @Test
    void should_ReplaceConfiguredHeadersAndSerializeXml_When_EnumOverridesAreApplied() throws IOException {
        var operation = client(api("POST", null, List.of(new NameValue("Authorization", "configured"))))
                .<Customer, Customer>operationBuilder().id("customer")
                .method(HttpMethod.PUT).contentType(HttpContentType.APPLICATION_XML)
                .accept(HttpContentType.APPLICATION_XML).header(HttpHeader.AUTHORIZATION, "replacement")
                .addHeader(HttpHeader.X_REQUEST_ID, "first").addHeader(HttpHeader.X_REQUEST_ID, "second")
                .requestBody(new Customer("Ada")).build();

        Buffer bytes = new Buffer();
        operation.request().body().writeTo(bytes);
        assertTrue(bytes.readUtf8().contains("<name>Ada</name>"));
        assertEquals("PUT", operation.request().method());
        assertEquals(List.of("replacement"), operation.request().headers("Authorization"));
        assertEquals(List.of("first", "second"), operation.request().headers("X-Request-Id"));
        try (Response response = response(operation, "application/xml", "<Customer><name>Ada</name></Customer>", 200)) {
            assertEquals(new Customer("Ada"), operation.readResponse(response));
        }
    }

    @Test
    void should_PreserveCustomValuesAndResolveAliases_When_StringAndEnumSettersAreMixed() {
        var operation = new DefaultStarterHttpClient().operationBuilder()
                .baseUrl("https://example.test").method(HttpMethod.GET).method("CUSTOM")
                .contentType("APPLICATION_JSON").accept("application/vnd.customer+json")
                .header(HttpHeader.USER_AGENT, "foundation").header("X-Custom", "custom").build();

        assertEquals("CUSTOM", operation.request().method());
        assertEquals("application/json", operation.request().header("Content-Type"));
        assertEquals("application/vnd.customer+json", operation.request().header("Accept"));
        assertEquals("custom", operation.request().header("X-Custom"));
    }

    @Test
    void should_RejectNullEnums_When_EnumSetterIsCalled() {
        var builder = new DefaultStarterHttpClient().operationBuilder();
        assertThrows(NullPointerException.class, () -> builder.method((HttpMethod) null));
        assertThrows(NullPointerException.class, () -> builder.contentType((HttpContentType) null));
        assertThrows(NullPointerException.class, () -> builder.accept((HttpContentType) null));
        assertThrows(NullPointerException.class, () -> builder.header((HttpHeader) null, "value"));
        assertThrows(NullPointerException.class, () -> builder.addHeader((HttpHeader) null, "value"));
    }

    @Test
    void should_MergeOverridesAndEncodeValues_When_RegisteredOperationIsBuilt() throws IOException {
        Api api = api("GET", null, List.of(new NameValue("X-Tag", "one"), new NameValue("X-Tag", "two")));
        StarterHttpClient client = client(api);
        var operation = client.operationBuilder().header("X-Tag", "override")
                .queryParameter("page", "2").id("customer").pathVariable("id", "a/b ?")
                .addHeader("X-Tag", "extra").addQueryParameter("page", "3").build();

        assertEquals("/v1/customers/a%2Fb%20%3F", operation.request().url().encodedPath());
        assertEquals(List.of("2", "3"), operation.request().url().queryParameterValues("page"));
        assertEquals(List.of("override", "extra"), operation.request().headers("X-Tag"));
        assertEquals("GET", operation.request().method());
        assertNull(operation.request().body());
    }

    @Test
    void should_SnapshotDifferentPayloads_When_EachBuildSuppliesRequestBody() throws IOException {
        StarterHttpClient client = client(api("POST", null, List.of()));
        HttpOperations.Builder<Customer, Customer> builder = client.<Customer, Customer>operationBuilder()
                .id("customer").requestBody(new Customer("override"));
        var first = builder.build();
        builder.requestBody(new Customer("later"));

        Buffer bytes = new Buffer();
        first.request().body().writeTo(bytes);
        assertEquals("{\"name\":\"override\"}", bytes.readUtf8());
        builder.build().request().body().writeTo(bytes);
        assertEquals("{\"name\":\"later\"}", bytes.readUtf8());
        assertThrows(IllegalArgumentException.class, () -> builder.requestBody(null).build());
    }

    @Test
    void should_EncodeAndDecodeXml_When_XmlIsSelected() throws IOException {
        var operation = client(api("POST", null, List.of()))
                .<Customer, Customer>operationBuilder().id("customer")
                .contentType("application/xml").accept("application/xml")
                .requestBody(new Customer("Ada")).responseType(Customer.class).build();
        Buffer bytes = new Buffer();
        operation.request().body().writeTo(bytes);
        assertTrue(bytes.readUtf8().contains("<name>Ada</name>"));
        try (Response response = response(operation, "application/xml", "<Customer><name>Ada</name></Customer>", 200)) {
            assertEquals(new Customer("Ada"), operation.readResponse(response));
        }
    }

    @Test
    void should_DecodeGenericTypeAndEmptyBody_When_ResponseTypeIsProvided() throws IOException {
        Type listType = new ParameterizedType() {
            @Override public Type[] getActualTypeArguments() { return new Type[]{Customer.class}; }
            @Override public Type getRawType() { return List.class; }
            @Override public Type getOwnerType() { return null; }
        };
        var operation = new DefaultStarterHttpClient().<Void, List<Customer>>operationBuilder()
                .baseUrl("https://example.test").responseType(listType).build();
        try (Response response = response(operation, "application/problem+json", "[{\"name\":\"Ada\"}]", 400)) {
            assertFalse(response.isSuccessful());
            assertEquals(List.of(new Customer("Ada")), operation.readResponse(response));
        }
        try (Response response = response(operation, "application/json", "", 204)) {
            assertNull(operation.readResponse(response));
        }
    }

    @Test
    void should_RejectInvalidOperations_When_RequiredValuesOrMethodBodyAreInvalid() {
        StarterHttpClient client = client(api("GET", null, List.of()));
        assertThrows(IllegalArgumentException.class, () -> client.operationBuilder().id("missing").build());
        assertThrows(IllegalArgumentException.class, () -> client.operationBuilder().build());
        assertThrows(IllegalArgumentException.class, () -> client.operationBuilder().id("customer").pathVariable("id", "..").build());
        assertThrows(IllegalArgumentException.class, () -> client.operationBuilder().id("customer").requestBody("body").build());
        assertThrows(IllegalArgumentException.class, () -> client.operationBuilder().id("customer").path("customers/{missing}").build());
        assertThrows(IllegalArgumentException.class, () -> client.operationBuilder().id("customer").callTimeout(Duration.ofMillis(-1)).build());
        assertThrows(IllegalArgumentException.class, () -> client.operationBuilder().id("customer").path("https://elsewhere.test").build());
    }

    @Test
    void should_PreserveSerializationCause_When_PayloadCannotBeEncoded() {
        var builder = client(api("POST", null, List.of())).operationBuilder().id("customer")
                .method("POST").contentType("text/plain").requestBody(new Customer("Ada"));
        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class, builder::build);
        assertTrue(failure.getCause() instanceof IOException);
    }

    @Test
    void should_RejectExternalEntities_When_DecodingXml() {
        String xml = "<!DOCTYPE Customer [<!ENTITY xxe SYSTEM 'file:///not-readable'>]><Customer><name>&xxe;</name></Customer>";
        assertThrows(IOException.class, () -> codec.decode(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8), "application/xml", Customer.class));
    }

    @Test
    void should_ReturnRawResponseAndEnqueueCall_When_ExecutingPreparedOperation() throws Exception {
        OkHttpClient transport = new OkHttpClient.Builder().addInterceptor(chain -> new Response.Builder()
                .request(chain.request()).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .header("Content-Type", "application/json")
                .body(ResponseBody.create("{\"name\":\"Ada\"}", okhttp3.MediaType.get("application/json"))).build()).build();
        DefaultStarterHttpClient client = new DefaultStarterHttpClient(transport);
        var operation = client.<Void, Customer>operationBuilder().baseUrl("https://example.test")
                .responseType(Customer.class).callTimeout(Duration.ofSeconds(2)).build();
        try (Response response = client.execute(operation)) {
            assertEquals(new Customer("Ada"), operation.readResponse(response));
        }
        CompletableFuture<Customer> result = new CompletableFuture<>();
        try {
            Call call = client.executeAsync(operation, new Callback() {
                @Override public void onFailure(Call call, IOException exception) { result.completeExceptionally(exception); }
                @Override public void onResponse(Call call, Response response) throws IOException {
                    try (response) { result.complete(operation.readResponse(response)); }
                }
            });
            assertNotNull(call);
            assertEquals(Duration.ofSeconds(2).toNanos(), call.timeout().timeoutNanos());
            assertEquals(new Customer("Ada"), result.get(5, TimeUnit.SECONDS));
        } finally {
            transport.dispatcher().executorService().shutdown();
            transport.connectionPool().evictAll();
        }
    }

    private StarterHttpClient client(Api api) {
        HttpClientProperties properties = new HttpClientProperties(null, null, null, null, null, null, List.of(api));
        return new DefaultStarterHttpClient(new OkHttpClient(), new HttpOperationRegistry(properties, codec));
    }

    private Api api(String method, Boolean bodyRequired, List<NameValue> headers) {
        return new Api("customer", "https://example.test/v1", "customers/{id}", method, headers,
                List.of(new NameValue("page", "1")), Map.of("id", "default"), null, null,
                bodyRequired, Customer.class.getName(), null);
    }

    @ParameterizedTest
    @EnumSource(value = HttpMethod.class, names = {"POST", "PUT", "PATCH", "PROPPATCH", "REPORT"})
    void should_RejectMissingPayload_When_MethodRequiresBody(HttpMethod method) {
        var builder = client(api(method.name(), null, List.of())).operationBuilder().id("customer");
        IllegalArgumentException missing = assertThrows(IllegalArgumentException.class, builder::build);
        assertTrue(missing.getMessage().contains("requestBody"));
        assertThrows(IllegalArgumentException.class, () -> builder.requestBody(null).build());
    }

    @Test
    void should_RequireRegisteredId_When_PayloadIsSupplied() {
        var builder = new DefaultStarterHttpClient().operationBuilder()
                .baseUrl("https://example.test").method("POST").requestBody(new Customer("Ada"));
        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class, builder::build);
        assertTrue(failure.getMessage().contains("id(...)"));
    }

    @Test
    void should_AllowIdOnlyPost_When_RegistrationExplicitlyHasNoBody() throws IOException {
        var operation = client(api("POST", false, List.of())).operationBuilder().id("customer").build();
        assertEquals("POST", operation.request().method());
        assertEquals(0, operation.request().body().contentLength());
    }

    @Test
    void should_RequirePayloadForDelete_When_RegistrationRequiresBody() {
        var client = client(api("DELETE", true, List.of()));
        assertThrows(IllegalArgumentException.class, () -> client.operationBuilder().id("customer").build());
        var operation = client.operationBuilder().id("customer").requestBody("{}").build();
        assertNotNull(operation.request().body());
    }

    private Response response(HttpOperations<?, ?> operation, String type, String body, int status) {
        return new Response.Builder().request(operation.request()).protocol(Protocol.HTTP_1_1).code(status)
                .message("test").header("Content-Type", type).body(ResponseBody.create(body, okhttp3.MediaType.get(type))).build();
    }

    record Customer(String name) {
    }
}
