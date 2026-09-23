package reyga.starter.foundation.core.config;

import java.time.Duration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;
import reyga.starter.foundation.core.aspect.AspectAfter;
import reyga.starter.foundation.core.aspect.AspectAfterReturning;
import reyga.starter.foundation.core.aspect.AspectAfterThrowing;
import reyga.starter.foundation.core.aspect.AspectAround;
import reyga.starter.foundation.core.aspect.AspectBefore;
import reyga.starter.foundation.core.aspect.BaseAspectAfter;
import reyga.starter.foundation.core.aspect.BaseAspectAfterReturning;
import reyga.starter.foundation.core.aspect.BaseAspectAfterThrowing;
import reyga.starter.foundation.core.aspect.BaseAspectAround;
import reyga.starter.foundation.core.aspect.BaseAspectBefore;
import reyga.starter.foundation.core.aspect.AspectProcessor;
import reyga.starter.foundation.core.component.DefaultTransactionalExecutor;
import reyga.starter.foundation.core.component.TransactionalExecutor;
import reyga.starter.foundation.core.config.properties.ConfigProperties;
import reyga.starter.foundation.core.exception.handler.DefaultAppValidationExceptionHandler;
import reyga.starter.foundation.core.exception.handler.DefaultExceptionHandler;
import reyga.starter.foundation.core.exception.handler.DefaultValidationExceptionHandler;
import reyga.starter.foundation.core.service.DefaultResilienceService;
import reyga.starter.foundation.core.service.foundation.ResilienceService;

@Configuration
@EnableConfigurationProperties(ConfigProperties.class)
public class CoreConfig {

    private static final Logger log = LoggerFactory.getLogger(CoreConfig.class);

    @Bean
    @ConditionalOnProperty(name = "reyga.config.default-bean.exception-handler", havingValue = "true")
    public DefaultExceptionHandler defaultExceptionHandler() {
        return new DefaultExceptionHandler();
    }

    @Bean
    public DefaultAppValidationExceptionHandler defaultAppValidationExceptionHandler() {
        return new DefaultAppValidationExceptionHandler();
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.default-bean.validation-handler", havingValue = "true")
    public DefaultValidationExceptionHandler defaultValidationExceptionHandler() {
        return new DefaultValidationExceptionHandler();
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.default-bean.utilities", havingValue = "true")
    public ResilienceService resilienceService() {
        return new DefaultResilienceService();
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.default-bean.utilities", havingValue = "true")
    public TransactionalExecutor transactionalExecutor(PlatformTransactionManager transactionManager) {
        return new DefaultTransactionalExecutor(transactionManager);
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.aspect.around", havingValue = "true")
    public AspectAround aspectAround(ConfigProperties configProperties, ListableBeanFactory beanFactory) {
        Object behavior = resolveAspectBehavior(
                beanFactory, configProperties.aspect().behavior(),
                "reyga.config.aspect.behavior", "reyga.config.aspect.around",
                BaseAspectAround.class
        );
        return new AspectAround((AspectProcessor) behavior);
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.aspect.before", havingValue = "true")
    public AspectBefore aspectBefore(ConfigProperties configProperties, ListableBeanFactory beanFactory) {
        Object behavior = resolveAspectBehavior(
                beanFactory, configProperties.aspect().beforeBehavior(),
                "reyga.config.aspect.before-behavior", "reyga.config.aspect.before",
                BaseAspectBefore.class
        );
        return new AspectBefore((BaseAspectBefore) behavior);
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.aspect.after", havingValue = "true")
    public AspectAfter aspectAfter(ConfigProperties configProperties, ListableBeanFactory beanFactory) {
        Object behavior = resolveAspectBehavior(
                beanFactory, configProperties.aspect().afterBehavior(),
                "reyga.config.aspect.after-behavior", "reyga.config.aspect.after",
                BaseAspectAfter.class
        );
        return new AspectAfter((BaseAspectAfter) behavior);
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.aspect.after-returning", havingValue = "true")
    public AspectAfterReturning aspectAfterReturning(ConfigProperties configProperties, ListableBeanFactory beanFactory) {
        Object behavior = resolveAspectBehavior(
                beanFactory, configProperties.aspect().afterReturningBehavior(),
                "reyga.config.aspect.after-returning-behavior", "reyga.config.aspect.after-returning",
                BaseAspectAfterReturning.class
        );
        return new AspectAfterReturning((BaseAspectAfterReturning) behavior);
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.aspect.after-throwing", havingValue = "true")
    public AspectAfterThrowing aspectAfterThrowing(ConfigProperties configProperties, ListableBeanFactory beanFactory) {
        Object behavior = resolveAspectBehavior(
                beanFactory, configProperties.aspect().afterThrowingBehavior(),
                "reyga.config.aspect.after-throwing-behavior", "reyga.config.aspect.after-throwing",
                BaseAspectAfterThrowing.class
        );
        return new AspectAfterThrowing((BaseAspectAfterThrowing) behavior);
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.rate-limiter.required", havingValue = "true")
    public RateLimiterConfig defaultRateLimiterConfig(ConfigProperties properties) {
        ConfigProperties.RateLimiter rateLimiterProperty = properties.rateLimiter();
        return RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(rateLimiterProperty.refreshPeriodSeconds()))
                .limitForPeriod(rateLimiterProperty.maxRequest())
                .timeoutDuration(Duration.ofMillis(rateLimiterProperty.timeoutMilis()))
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.rate-limiter.required", havingValue = "true")
    public RateLimiterRegistry rateLimiterRegistry(RateLimiterConfig defaultConfig) {
        return RateLimiterRegistry.of(defaultConfig);
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.circuit-breaker.required", havingValue = "true")
    public CircuitBreakerConfig defaultCircuitBreakerConfig(ConfigProperties properties) {
        ConfigProperties.CircuitBreaker circuitBreakerProperty = properties.circuitBreaker();
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(circuitBreakerProperty.failureRateThreshold())
                .slowCallRateThreshold(circuitBreakerProperty.slowCallRateThreshold())
                .slowCallDurationThreshold(Duration.ofSeconds(circuitBreakerProperty.slowCallDurationThresholdSeconds()))
                .minimumNumberOfCalls(circuitBreakerProperty.minimumNumberOfCalls())
                .slidingWindowSize(circuitBreakerProperty.slidingWindowSize())
                .permittedNumberOfCallsInHalfOpenState(circuitBreakerProperty.permittedNumberOfCallsInHalfOpenState())
                .waitDurationInOpenState(Duration.ofSeconds(circuitBreakerProperty.slowCallDurationThresholdSeconds()))
                .automaticTransitionFromOpenToHalfOpenEnabled(circuitBreakerProperty.automaticTransitionFromOpenToHalfOpenEnabled())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.circuit-breaker.required", havingValue = "true")
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig defaultConfig) {
        return CircuitBreakerRegistry.of(defaultConfig);
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.retry.required", havingValue = "true")
    public RetryConfig defaultRetryConfig(ConfigProperties properties) {
        return RetryConfig.custom()
                .maxAttempts(properties.retry().maxAttempts())
                .waitDuration(Duration.ofMillis(properties.retry().waitDurationMillis()))
                .failAfterMaxAttempts(properties.retry().failAfterMaxAttempts())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.retry.required", havingValue = "true")
    public RetryRegistry retryRegistry(RetryConfig retryConfig) {
        RetryRegistry registry = RetryRegistry.of(retryConfig);
        registry.getEventPublisher()
                .onEntryAdded(event -> {
                    Retry addedRetry = event.getAddedEntry();
                    addedRetry.getEventPublisher().onRetry(retryEvent ->
                            log.warn(
                                    "-> Retry triggered [{}] attempt#{} because: {}",
                                    retryEvent.getName(),
                                    retryEvent.getNumberOfRetryAttempts(),
                                    retryEvent.getLastThrowable() != null
                                            ? retryEvent.getLastThrowable().getMessage()
                                            : "unknown error"
                            )
                    );
                });
        return registry;
    }

    private Object resolveAspectBehavior(ListableBeanFactory beanFactory, String configuredBehaviorName,
                                         String behaviorProperty, String enabledProperty, Class<?> requiredType)
    {
        if (!StringUtils.hasText(configuredBehaviorName)) {
            throw new IllegalStateException(
                    behaviorProperty + " must contain the bean name of a "
                            + requiredType.getSimpleName() + " implementation when "
                            + enabledProperty + "=true"
            );
        }
        String behaviorName = configuredBehaviorName.trim();

        if (!beanFactory.containsBean(behaviorName)) {
            throw new IllegalStateException(
                    "No bean named '" + behaviorName + "' was found for " + behaviorProperty + ". "
                            + "Register a @Component that extends " + requiredType.getSimpleName()
            );
        }

        Object behavior = beanFactory.getBean(behaviorName);
        Class<?> behaviorType = AopUtils.getTargetClass(behavior);
        if (!requiredType.isAssignableFrom(behaviorType)) {
            throw new IllegalStateException(
                    "Bean named '" + behaviorName + "' configured by " + behaviorProperty
                            + " must extend " + requiredType.getSimpleName()
                            + " but was " + behaviorType.getName()
            );
        }

        return behavior;
    }

}
