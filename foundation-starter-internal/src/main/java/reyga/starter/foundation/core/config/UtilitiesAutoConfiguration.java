package reyga.starter.foundation.core.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

import reyga.starter.foundation.core.component.DefaultTransactionalExecutor;
import reyga.starter.foundation.core.component.TransactionalExecutor;
import reyga.starter.foundation.core.config.properties.ConfigProperties;
import reyga.starter.foundation.core.service.DefaultResilienceService;
import reyga.starter.foundation.core.service.foundation.ResilienceService;

/**
 * Configures the Utilities feature using existing bean names and properties.
 * Explicit legacy CoreConfig imports retain priority to avoid duplicate bean definitions.
 */
@AutoConfiguration
@ConditionalOnMissingBean(CoreConfig.class)
@EnableConfigurationProperties(ConfigProperties.class)
public class UtilitiesAutoConfiguration {

    /** Creates this feature configuration. */
    public UtilitiesAutoConfiguration() {
    }

    /**
     * Creates the existing resilienceService bean with unchanged activation and defaults.
     * @return the configured bean using the legacy factory behavior
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.default-bean.utilities", havingValue = "true")
    public ResilienceService resilienceService() {
        return new DefaultResilienceService();
    }

    /**
     * Creates the existing transactionalExecutor bean with unchanged activation and defaults.
     * @param transactionManager required transaction manager used by the executor
     * @return the configured bean using the legacy factory behavior
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.default-bean.utilities", havingValue = "true")
    public TransactionalExecutor transactionalExecutor(PlatformTransactionManager transactionManager) {
        return new DefaultTransactionalExecutor(transactionManager);
    }

}
