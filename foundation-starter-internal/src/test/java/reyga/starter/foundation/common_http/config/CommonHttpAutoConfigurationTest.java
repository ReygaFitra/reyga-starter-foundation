package reyga.starter.foundation.common_http.config;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reyga.starter.foundation.common_http.client.DefaultStarterHttpClient;
import reyga.starter.foundation.common_http.client.StarterHttpClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommonHttpAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CommonHttpAutoConfiguration.class));

    @Test
    void should_NotCreateHttpBeans_When_FeatureIsUnspecified() {
        runner.run(context -> {
            assertNull(context.getStartupFailure());
            assertTrue(context.getBeansOfType(OkHttpClient.class).isEmpty());
            assertTrue(context.getBeansOfType(StarterHttpClient.class).isEmpty());
        });
    }

    @Test
    void should_NotCreateHttpBeans_When_FeatureIsDisabled() {
        runner.withPropertyValues("reyga.config.default-bean.http-client=false")
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    assertTrue(context.getBeansOfType(OkHttpClient.class).isEmpty());
                    assertTrue(context.getBeansOfType(StarterHttpClient.class).isEmpty());
                });
    }

    @Test
    void should_NotCreateHttpBeans_When_FeatureValueIsInvalid() {
        runner.withPropertyValues("reyga.config.default-bean.http-client=invalid")
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    assertTrue(context.getBeansOfType(OkHttpClient.class).isEmpty());
                    assertTrue(context.getBeansOfType(StarterHttpClient.class).isEmpty());
                });
    }

    @Test
    void should_CreateDefaultHttpBeans_When_FeatureIsEnabled() {
        runner.withPropertyValues("reyga.config.default-bean.http-client=true")
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    assertEquals(1, context.getBeansOfType(OkHttpClient.class).size());
                    assertEquals(1, context.getBeansOfType(StarterHttpClient.class).size());
                    assertTrue(context.getBean(StarterHttpClient.class)
                            instanceof DefaultStarterHttpClient);
                });
    }

    @Test
    void should_UseConsumerOkHttpClient_When_CustomClientExists() {
        runner.withUserConfiguration(CustomOkHttpConfiguration.class)
                .withPropertyValues("reyga.config.default-bean.http-client=true")
                .run(context -> {
                    OkHttpClient customClient = context.getBean(OkHttpClient.class);
                    StarterHttpClient starterClient = context.getBean(StarterHttpClient.class);
                    Request request = new Request.Builder()
                            .url("https://example.test/resource")
                            .build();
                    Call call = mock(Call.class);
                    Callback callback = mock(Callback.class);
                    when(customClient.newCall(request)).thenReturn(call);

                    Call result = starterClient.asynchronousCall(request, callback);

                    assertSame(call, result);
                    verify(customClient).newCall(request);
                    verify(call).enqueue(callback);
                });
    }

    @Test
    void should_KeepConsumerStarterHttpClient_When_CustomClientExists() {
        runner.withUserConfiguration(CustomStarterHttpConfiguration.class)
                .withPropertyValues("reyga.config.default-bean.http-client=true")
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    assertEquals(1, context.getBeansOfType(OkHttpClient.class).size());
                    assertEquals(1, context.getBeansOfType(StarterHttpClient.class).size());
                    assertSame(
                            context.getBean("customStarterHttpClient"),
                            context.getBean(StarterHttpClient.class)
                    );
                });
    }

    @Test
    void should_KeepBothConsumerBeans_When_CustomClientsExist() {
        runner.withUserConfiguration(
                        CustomOkHttpConfiguration.class,
                        CustomStarterHttpConfiguration.class
                )
                .withPropertyValues("reyga.config.default-bean.http-client=true")
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    assertEquals(1, context.getBeansOfType(OkHttpClient.class).size());
                    assertEquals(1, context.getBeansOfType(StarterHttpClient.class).size());
                    assertSame(
                            context.getBean("customOkHttpClient"),
                            context.getBean(OkHttpClient.class)
                    );
                    assertSame(
                            context.getBean("customStarterHttpClient"),
                            context.getBean(StarterHttpClient.class)
                    );
                });
    }

    @Test
    void should_KeepConsumerBeansWithoutCreatingDefaults_When_FeatureIsDisabled() {
        runner.withUserConfiguration(
                        CustomOkHttpConfiguration.class,
                        CustomStarterHttpConfiguration.class
                )
                .withPropertyValues("reyga.config.default-bean.http-client=false")
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    assertEquals(1, context.getBeansOfType(OkHttpClient.class).size());
                    assertEquals(1, context.getBeansOfType(StarterHttpClient.class).size());
                    assertSame(
                            context.getBean("customOkHttpClient"),
                            context.getBean(OkHttpClient.class)
                    );
                    assertSame(
                            context.getBean("customStarterHttpClient"),
                            context.getBean(StarterHttpClient.class)
                    );
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomOkHttpConfiguration {

        @Bean
        OkHttpClient customOkHttpClient() {
            return mock(OkHttpClient.class);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomStarterHttpConfiguration {

        @Bean
        StarterHttpClient customStarterHttpClient() {
            return mock(StarterHttpClient.class);
        }
    }
}
