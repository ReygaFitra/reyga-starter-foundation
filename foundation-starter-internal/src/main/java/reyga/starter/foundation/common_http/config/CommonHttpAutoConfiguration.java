package reyga.starter.foundation.common_http.config;

import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import reyga.starter.foundation.common_http.client.DefaultStarterHttpClient;
import reyga.starter.foundation.common_http.client.StarterHttpClient;

/**
 * Auto-configuration for the default foundation HTTP client.
 *
 * <p>The configuration is enabled by setting
 * {@code reyga.config.default-bean.http-client=true}. Consumer-defined
 * {@link OkHttpClient} and {@link StarterHttpClient} beans take precedence.</p>
 */
@AutoConfiguration
@ConditionalOnProperty(name = "reyga.config.default-bean.http-client", havingValue = "true")
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
    @Bean
    @ConditionalOnMissingBean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }

    /**
     * Creates the default foundation HTTP client.
     *
     * @param okHttpClient reusable OkHttp client selected from the application context
     * @return the default foundation HTTP client
     */
    @Bean
    @ConditionalOnMissingBean
    public StarterHttpClient starterHttpClient(OkHttpClient okHttpClient) {
        return new DefaultStarterHttpClient(okHttpClient);
    }
}
