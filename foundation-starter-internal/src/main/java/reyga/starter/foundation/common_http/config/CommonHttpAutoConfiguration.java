package reyga.starter.foundation.common_http.config;

import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;
import reyga.starter.foundation.common_http.client.DefaultHttpBodyCodec;
import reyga.starter.foundation.common_http.client.DefaultStarterHttpClient;
import reyga.starter.foundation.common_http.client.HttpBodyCodec;
import reyga.starter.foundation.common_http.client.HttpOperationRegistry;
import reyga.starter.foundation.common_http.client.StarterHttpClient;

/**
 * Auto-configuration for the default foundation HTTP client.
 *
 * <p>The configuration is enabled by setting
 * {@code reyga.config.http-client.enabled=true} or the compatibility flag
 * {@code reyga.config.default-bean.http-client=true}. Consumer-defined
 * {@link OkHttpClient} and {@link StarterHttpClient} beans take precedence.</p>
 */
@AutoConfiguration
@Conditional(HttpClientEnabledCondition.class)
@EnableConfigurationProperties(HttpClientProperties.class)
public class CommonHttpAutoConfiguration {

    /**
     * Creates the HTTP client auto-configuration.
     */
    public CommonHttpAutoConfiguration() {
    }

    /**
     * Creates the reusable OkHttp client used by the default foundation client.
     *
     * @return an OkHttp client with library defaults
     */
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }

    /**
     * Creates a reusable transport from foundation timeout and recovery settings.
     * @param properties HTTP configuration
     * @return configured client; consumer transport beans take precedence
     */
    @Bean
    @ConditionalOnMissingBean
    public OkHttpClient okHttpClient(HttpClientProperties properties) {
        return new OkHttpClient.Builder()
                .connectTimeout(properties.connectTimeout())
                .readTimeout(properties.readTimeout())
                .writeTimeout(properties.writeTimeout())
                .callTimeout(properties.callTimeout())
                .followRedirects(properties.followRedirects())
                .retryOnConnectionFailure(properties.retryOnConnectionFailure())
                .build();
    }

    /**
     * Creates the default JSON/XML serialization strategy.
     * @return default thread-safe JSON/XML codec; consumer codecs take precedence
     */
    @Bean
    @ConditionalOnMissingBean
    public HttpBodyCodec httpBodyCodec() {
        return new DefaultHttpBodyCodec();
    }

    /**
     * Validates registrations whenever HTTP support is enabled.
     * @param properties HTTP configuration
     * @param environment application properties used to reject removed YAML payload sources
     * @param codec selected body codec
     * @return validated registry
     */
    @Bean
    @ConditionalOnMissingBean
    public HttpOperationRegistry httpOperationRegistry(HttpClientProperties properties, Environment environment, HttpBodyCodec codec) {
        for (int index = 0; index < properties.registerApi().size(); index++) {
            String prefix = "reyga.config.http-client.register-api[" + index + "].";
            if (environment.containsProperty(prefix + "request-body-bean")
                    || environment.containsProperty(prefix + "request-body")
                    || Binder.get(environment).bind(prefix + "request-body", Bindable.mapOf(String.class, Object.class)).isBound()) {
                throw new IllegalArgumentException("HTTP request body configuration has been removed; use id(...) and requestBody(...) in the builder");
            }
        }
        return new HttpOperationRegistry(properties, codec);
    }

    /**
     * Creates the default foundation HTTP client.
     *
     * @param okHttpClient reusable OkHttp client selected from the application context
     * @return the default foundation HTTP client
     */
    public StarterHttpClient starterHttpClient(OkHttpClient okHttpClient) {
        return new DefaultStarterHttpClient(okHttpClient);
    }

    /**
     * Creates the registered foundation client, preserving consumer implementations.
     * @param okHttpClient reusable transport
     * @param registry validated registry
     * @return configured foundation client
     */
    @Bean
    @ConditionalOnMissingBean
    public StarterHttpClient starterHttpClient(OkHttpClient okHttpClient, HttpOperationRegistry registry) {
        return new DefaultStarterHttpClient(okHttpClient, registry);
    }
}
