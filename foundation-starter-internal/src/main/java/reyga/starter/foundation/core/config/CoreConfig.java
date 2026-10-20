package reyga.starter.foundation.core.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import reyga.starter.foundation.core.aspect.AspectAfter;
import reyga.starter.foundation.core.aspect.AspectAfterReturning;
import reyga.starter.foundation.core.aspect.AspectAfterThrowing;
import reyga.starter.foundation.core.aspect.AspectAround;
import reyga.starter.foundation.core.aspect.AspectBefore;
import reyga.starter.foundation.core.component.TransactionalExecutor;
import reyga.starter.foundation.core.config.properties.ConfigProperties;
import reyga.starter.foundation.core.exception.handler.DefaultAppValidationExceptionHandler;
import reyga.starter.foundation.core.exception.handler.DefaultExceptionHandler;
import reyga.starter.foundation.core.exception.handler.DefaultValidationExceptionHandler;
import reyga.starter.foundation.core.service.foundation.ResilienceService;

/**
 * Compatibility configuration retaining the original bean methods and property conditions.
 * New starter registration uses the feature auto-configurations; explicit imports remain supported.
 */
@Configuration
@EnableConfigurationProperties(ConfigProperties.class)
public class CoreConfig {

    /** Creates the compatibility configuration. */
    public CoreConfig() {
    }

    /**
     * Creates the existing defaultExceptionHandler bean with unchanged activation and defaults.
     * @return the configured bean using the legacy factory behavior
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.default-bean.exception-handler", havingValue = "true")
    public DefaultExceptionHandler defaultExceptionHandler() {
        return new ExceptionHandlingAutoConfiguration().defaultExceptionHandler();
    }

    /**
     * Creates the existing defaultAppValidationExceptionHandler bean with unchanged activation and defaults.
     * @return the configured bean using the legacy factory behavior
     */
    @Bean
    public DefaultAppValidationExceptionHandler defaultAppValidationExceptionHandler() {
        return new ExceptionHandlingAutoConfiguration().defaultAppValidationExceptionHandler();
    }

    /**
     * Creates the existing defaultValidationExceptionHandler bean with unchanged activation and defaults.
     * @return the configured bean using the legacy factory behavior
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.default-bean.validation-handler", havingValue = "true")
    public DefaultValidationExceptionHandler defaultValidationExceptionHandler() {
        return new ExceptionHandlingAutoConfiguration().defaultValidationExceptionHandler();
    }

    /**
     * Creates the existing resilienceService bean with unchanged activation and defaults.
     * @return the configured bean using the legacy factory behavior
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.default-bean.utilities", havingValue = "true")
    public ResilienceService resilienceService() {
        return new UtilitiesAutoConfiguration().resilienceService();
    }

    /**
     * Creates the existing transactionalExecutor bean with unchanged activation and defaults.
     * @param transactionManager required transaction manager used by the executor
     * @return the configured bean using the legacy factory behavior
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.default-bean.utilities", havingValue = "true")
    public TransactionalExecutor transactionalExecutor(PlatformTransactionManager transactionManager) {
        return new UtilitiesAutoConfiguration().transactionalExecutor(transactionManager);
    }

    /**
     * Creates the existing aspectAround bean with unchanged activation and defaults.
     * @param configProperties bound properties supplying the configured behavior bean name
     * @param beanFactory application bean factory used to resolve and validate the behavior
     * @return the configured bean using the legacy factory behavior
     * @throws IllegalStateException if the behavior name is blank, missing, or has the wrong type
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.aspect.around", havingValue = "true")
    public AspectAround aspectAround(ConfigProperties configProperties, ListableBeanFactory beanFactory) {
        return new AspectAdviceAutoConfiguration().aspectAround(configProperties, beanFactory);
    }

    /**
     * Creates the existing aspectBefore bean with unchanged activation and defaults.
     * @param configProperties bound properties supplying the configured behavior bean name
     * @param beanFactory application bean factory used to resolve and validate the behavior
     * @return the configured bean using the legacy factory behavior
     * @throws IllegalStateException if the behavior name is blank, missing, or has the wrong type
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.aspect.before", havingValue = "true")
    public AspectBefore aspectBefore(ConfigProperties configProperties, ListableBeanFactory beanFactory) {
        return new AspectAdviceAutoConfiguration().aspectBefore(configProperties, beanFactory);
    }

    /**
     * Creates the existing aspectAfter bean with unchanged activation and defaults.
     * @param configProperties bound properties supplying the configured behavior bean name
     * @param beanFactory application bean factory used to resolve and validate the behavior
     * @return the configured bean using the legacy factory behavior
     * @throws IllegalStateException if the behavior name is blank, missing, or has the wrong type
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.aspect.after", havingValue = "true")
    public AspectAfter aspectAfter(ConfigProperties configProperties, ListableBeanFactory beanFactory) {
        return new AspectAdviceAutoConfiguration().aspectAfter(configProperties, beanFactory);
    }

    /**
     * Creates the existing aspectAfterReturning bean with unchanged activation and defaults.
     * @param configProperties bound properties supplying the configured behavior bean name
     * @param beanFactory application bean factory used to resolve and validate the behavior
     * @return the configured bean using the legacy factory behavior
     * @throws IllegalStateException if the behavior name is blank, missing, or has the wrong type
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.aspect.after-returning", havingValue = "true")
    public AspectAfterReturning aspectAfterReturning(ConfigProperties configProperties, ListableBeanFactory beanFactory) {
        return new AspectAdviceAutoConfiguration().aspectAfterReturning(configProperties, beanFactory);
    }

    /**
     * Creates the existing aspectAfterThrowing bean with unchanged activation and defaults.
     * @param configProperties bound properties supplying the configured behavior bean name
     * @param beanFactory application bean factory used to resolve and validate the behavior
     * @return the configured bean using the legacy factory behavior
     * @throws IllegalStateException if the behavior name is blank, missing, or has the wrong type
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.aspect.after-throwing", havingValue = "true")
    public AspectAfterThrowing aspectAfterThrowing(ConfigProperties configProperties, ListableBeanFactory beanFactory) {
        return new AspectAdviceAutoConfiguration().aspectAfterThrowing(configProperties, beanFactory);
    }

    /**
     * Creates the existing defaultRateLimiterConfig bean with unchanged activation and defaults.
     * @param properties bound foundation properties supplying the resilience settings
     * @return the configured bean using the legacy factory behavior
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.rate-limiter.required", havingValue = "true")
    public RateLimiterConfig defaultRateLimiterConfig(ConfigProperties properties) {
        return new ResilienceAutoConfiguration().defaultRateLimiterConfig(properties);
    }

    /**
     * Creates the existing rateLimiterRegistry bean with unchanged activation and defaults.
     * @param defaultConfig configuration used for new instances in the registry
     * @return the configured bean using the legacy factory behavior
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.rate-limiter.required", havingValue = "true")
    public RateLimiterRegistry rateLimiterRegistry(RateLimiterConfig defaultConfig) {
        return new ResilienceAutoConfiguration().rateLimiterRegistry(defaultConfig);
    }

    /**
     * Creates the existing defaultCircuitBreakerConfig bean with unchanged activation and defaults.
     * @param properties bound foundation properties supplying the resilience settings
     * @return the configured bean using the legacy factory behavior
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.circuit-breaker.required", havingValue = "true")
    public CircuitBreakerConfig defaultCircuitBreakerConfig(ConfigProperties properties) {
        return new ResilienceAutoConfiguration().defaultCircuitBreakerConfig(properties);
    }

    /**
     * Creates the existing circuitBreakerRegistry bean with unchanged activation and defaults.
     * @param defaultConfig configuration used for new instances in the registry
     * @return the configured bean using the legacy factory behavior
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.circuit-breaker.required", havingValue = "true")
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig defaultConfig) {
        return new ResilienceAutoConfiguration().circuitBreakerRegistry(defaultConfig);
    }

    /**
     * Creates the existing defaultRetryConfig bean with unchanged activation and defaults.
     * @param properties bound foundation properties supplying the resilience settings
     * @return the configured bean using the legacy factory behavior
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.retry.required", havingValue = "true")
    public RetryConfig defaultRetryConfig(ConfigProperties properties) {
        return new ResilienceAutoConfiguration().defaultRetryConfig(properties);
    }

    /**
     * Creates the existing retryRegistry bean with unchanged activation and defaults.
     * @param retryConfig configuration used for new retry instances in the registry
     * @return the configured bean using the legacy factory behavior
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.retry.required", havingValue = "true")
    public RetryRegistry retryRegistry(RetryConfig retryConfig) {
        return new ResilienceAutoConfiguration().retryRegistry(retryConfig);
    }

}
