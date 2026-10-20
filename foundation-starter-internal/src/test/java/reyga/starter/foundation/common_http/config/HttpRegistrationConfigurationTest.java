package reyga.starter.foundation.common_http.config;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;
import reyga.starter.foundation.common_http.client.HttpBodyCodec;
import reyga.starter.foundation.common_http.client.HttpOperationRegistry;
import reyga.starter.foundation.common_http.client.StarterHttpClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpRegistrationConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CommonHttpAutoConfiguration.class));
    private final String prefix = "reyga.config.http-client.";

    @Test
    void should_ExecuteIdOnlyGetAndPerRequestPost_When_YamlDefinesEndpointDefaults() throws IOException {
        var sources = new YamlPropertySourceLoader().load("http-id-only", new ClassPathResource("http-client-id-only-test.yml"));
        OkHttpClient transport = new OkHttpClient.Builder().addInterceptor(chain -> new Response.Builder()
                .request(chain.request()).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .header("Content-Type", "application/json")
                .body(ResponseBody.create("{\"name\":\"Ada\"}", MediaType.get("application/json"))).build()).build();
        runner.withBean(OkHttpClient.class, () -> transport)
                .withInitializer(context -> sources.forEach(source -> context.getEnvironment().getPropertySources().addLast(source)))
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    StarterHttpClient client = context.getBean(StarterHttpClient.class);
                    var get = client.<Void, Customer>operationBuilder().id("customer-id-only").build();
                    assertEquals("https://example.test/v1/customers/CUS-01?include=address", get.request().url().toString());
                    assertEquals("GET", get.request().method());
                    assertEquals("web", get.request().header("X-Channel"));
                    assertEquals("application/json", get.request().header("Content-Type"));
                    assertEquals("application/json", get.request().header("Accept"));
                    assertEquals(java.time.Duration.ofSeconds(2), get.callTimeout());
                    try (Response response = client.execute(get)) {
                        assertEquals(new Customer("Ada"), get.readResponse(response));
                    }
                    assertThrows(IllegalArgumentException.class, () -> client.operationBuilder().id("create-id-only").build());
                    var post = client.<Customer, Customer>operationBuilder().id("create-id-only").requestBody(new Customer("Ada")).build();
                    Buffer bytes = new Buffer();
                    post.request().body().writeTo(bytes);
                    assertEquals("{\"name\":\"Ada\"}", bytes.readUtf8());
                    assertEquals("POST", post.request().method());
                    try (Response response = client.execute(post)) {
                        assertEquals(new Customer("Ada"), post.readResponse(response));
                    }
                });
    }

    record Customer(String name) {
    }

    @Test
    void should_BindYamlListsAndTimeouts_When_NewActivationFlagIsEnabled() throws IOException {
        var sources = new YamlPropertySourceLoader().load("http-test", new ClassPathResource("http-client-test.yml"));
        runner.withInitializer(context -> sources.forEach(source -> context.getEnvironment().getPropertySources().addLast(source)))
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    OkHttpClient transport = context.getBean(OkHttpClient.class);
                    assertEquals(1500, transport.connectTimeoutMillis());
                    assertEquals(3000, transport.callTimeoutMillis());
                    assertTrue(!transport.followRedirects());
                    var operation = context.getBean(StarterHttpClient.class).operationBuilder().id("customer").build();
                    assertEquals("https://example.test/v1/customers?page=1&page=2", operation.request().url().toString());
                    assertEquals(2, operation.request().headers("X-Tag").size());
                });
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "register-api[0].base-url=ftp://example.test",
            "register-api[0].id= ",
            "register-api[0].request-body-bean=missingBean",
            "register-api[0].request-body={}",
            "register-api[0].request-body.name=Ada",
            "register-api[0].request-body-required=true",
            "register-api[0].request-body-required=invalid",
            "register-api[0].response-type=missing.Dto",
            "register-api[0].content-type=invalid",
            "register-api[0].content-type=APPLICATION_UNKNOWN",
            "register-api[0].accept=APPLICATION_UNKNOWN",
            "register-api[0].method= ",
            "register-api[0].path=https://other.test",
            "register-api[0].headers[0].name=bad header",
            "call-timeout=-1ms"
    })
    void should_FailStartup_When_RegistrationIsInvalid(String invalidProperty) {
        runner.withPropertyValues(prefix + "enabled=true", prefix + "register-api[0].id=customer",
                        prefix + "register-api[0].base-url=https://example.test", prefix + invalidProperty)
                .run(context -> assertNotNull(context.getStartupFailure()));
    }

    @Test
    void should_FailStartup_When_IdsAreDuplicated() {
        runner.withPropertyValues(prefix + "enabled=true", prefix + "register-api[0].id=customer",
                        prefix + "register-api[0].base-url=https://example.test",
                        prefix + "register-api[1].id=customer", prefix + "register-api[1].base-url=https://example.test")
                .run(context -> {
                    assertNotNull(context.getStartupFailure());
                    assertTrue(context.getStartupFailure().getMessage().contains("Duplicate HTTP"));
                });
    }

    @Test
    void should_IgnoreInvalidRegistrations_When_HttpFeatureIsDisabled() {
        runner.withPropertyValues(prefix + "enabled=false", prefix + "register-api[0].base-url=invalid")
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    assertTrue(context.getBeansOfType(HttpOperationRegistry.class).isEmpty());
                });
    }

    @Test
    void should_PreserveConsumerTransportAndCodec_When_CustomBeansExist() {
        OkHttpClient transport = new OkHttpClient.Builder().connectTimeout(java.time.Duration.ofSeconds(7)).build();
        HttpBodyCodec codec = new reyga.starter.foundation.common_http.client.DefaultHttpBodyCodec();
        runner.withBean(OkHttpClient.class, () -> transport).withBean(HttpBodyCodec.class, () -> codec)
                .withPropertyValues(prefix + "enabled=true", prefix + "connect-timeout=1s")
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    assertSame(transport, context.getBean(OkHttpClient.class));
                    assertSame(codec, context.getBean(HttpBodyCodec.class));
                });
    }
}
