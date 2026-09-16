package reyga.starter.foundation.core.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import reyga.starter.foundation.core.validation.ValidationConfig;
import reyga.starter.foundation.core.validation.ValidationUtility;

/** Optional validation utility configuration that backs off for application-defined beans. */
@AutoConfiguration
@EnableConfigurationProperties(ValidationProperties.class)
public class ValidationAutoConfiguration {
    /** Creates the YAML-configured utility when no application utility exists.
     * @param properties validation detail settings
     * @return independent validation utility managed by Spring
     */
    @Bean
    @ConditionalOnMissingBean(ValidationUtility.class)
    @ConditionalOnProperty(name = "reyga.config.default-bean.validation-handler", havingValue = "true")
    public ValidationUtility validationUtility(ValidationProperties properties) {
        var builder = ValidationConfig.builder().useDefaultBehavior();
        if (!properties.mapDetails()) builder.useMessageDetails();
        return builder.build();
    }
}
