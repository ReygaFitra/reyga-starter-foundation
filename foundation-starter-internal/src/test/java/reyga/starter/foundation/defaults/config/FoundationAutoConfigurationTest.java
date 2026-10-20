package reyga.starter.foundation.defaults.config;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.List;
import java.util.Set;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import reyga.starter.foundation.common.config.CommonConfig;
import reyga.starter.foundation.common.logging.LoggerInjector;
import reyga.starter.foundation.common_database.config.CommonDatabaseConfig;
import reyga.starter.foundation.common_database.processor.QueryProcessor;
import reyga.starter.foundation.common_http.client.StarterHttpClient;
import reyga.starter.foundation.common_io.config.CommonIOConfig;
import reyga.starter.foundation.common_io.operations.FileInspector;
import reyga.starter.foundation.common_io.operations.FileSanitizer;
import reyga.starter.foundation.core.component.TransactionalExecutor;
import reyga.starter.foundation.core.config.CoreConfig;
import reyga.starter.foundation.core.config.ResilienceAutoConfiguration;
import reyga.starter.foundation.core.exception.handler.DefaultAppValidationExceptionHandler;
import reyga.starter.foundation.core.exception.handler.DefaultExceptionHandler;
import reyga.starter.foundation.core.exception.handler.DefaultValidationExceptionHandler;
import reyga.starter.foundation.core.service.foundation.ResilienceService;
import reyga.starter.foundation.core.validation.ValidationUtility;
import reyga.starter.foundation.logging.config.AsyncLoggingConfig;
import reyga.starter.foundation.logging.config.LoggingConfig;
import reyga.starter.foundation.logging.service.LoggingService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class FoundationAutoConfigurationTest {
    private ApplicationContextRunner runner(String... exclusions) {
        List<String> excluded = new ArrayList<>();
        for (String candidate : ImportCandidates.load(AutoConfiguration.class, getClass().getClassLoader())) {
            if (!candidate.startsWith("reyga.starter.foundation.")) {
                excluded.add(candidate);
            }
        }
        excluded.addAll(List.of(exclusions));
        return new ApplicationContextRunner().withUserConfiguration(BootSelection.class)
                .withPropertyValues("spring.autoconfigure.exclude=" + String.join(",", excluded));
    }

    @Test
    void should_DiscoverDefaultBeansAndKeepFeaturesDisabled_When_NoFlagsAreSet() {
        runner().withBean(JdbcTemplate.class, () -> mock(JdbcTemplate.class)).run(context -> {
            assertNull(context.getStartupFailure());
            assertNotNull(context.getBean(QueryProcessor.class));
            assertNotNull(context.getBean(FileInspector.class));
            assertNotNull(context.getBean(FileSanitizer.class));
            assertNotNull(context.getBean(LoggerInjector.class));
            assertNotNull(context.getBean("asyncExecutor", Executor.class));
            assertNotNull(context.getBean(DefaultAppValidationExceptionHandler.class));
            assertTrue(context.getBeansOfType(ResilienceService.class).isEmpty());
            assertTrue(context.getBeansOfType(ValidationUtility.class).isEmpty());
            assertTrue(context.getBeansOfType(StarterHttpClient.class).isEmpty());
            assertTrue(context.getBeansOfType(LoggingService.class).isEmpty());
            assertTrue(context.getBeansOfType(DefaultExceptionHandler.class).isEmpty());
        });
    }

    @Test
    void should_PreserveLegacyExclusionAndIndependentFeatures_When_DefaultGroupIsExcluded() {
        runner(FoundationDefaultAutoConfiguration.class.getName())
                .withPropertyValues("reyga.config.default-bean.validation-handler=true",
                        "reyga.config.default-bean.http-client=true")
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    assertTrue(context.getBeansOfType(QueryProcessor.class).isEmpty());
                    assertTrue(context.getBeansOfType(FileInspector.class).isEmpty());
                    assertTrue(context.getBeansOfType(LoggerInjector.class).isEmpty());
                    assertTrue(context.getBeansOfType(DefaultAppValidationExceptionHandler.class).isEmpty());
                    assertTrue(context.getBeansOfType(DefaultValidationExceptionHandler.class).isEmpty());
                    assertFalse(context.containsBean("asyncExecutor"));
                    assertNotNull(context.getBean(ValidationUtility.class));
                    assertNotNull(context.getBean(StarterHttpClient.class));
                });
    }

    @Test
    void should_MatchLegacyBeanNamesAndTypes_When_ExistingInfrastructureFlagsAreEnabled() {
        String[] flags = {"reyga.config.default-bean.exception-handler=true",
                "reyga.config.default-bean.validation-handler=true",
                "reyga.config.default-bean.utilities=true",
                "reyga.config.default-bean.logging-handler=true",
                "reyga.config.rate-limiter.required=true",
                "reyga.config.retry.required=true",
                "reyga.config.circuit-breaker.required=true",
                "reyga.config.circuit-breaker.failure-rate-threshold=50",
                "reyga.config.circuit-breaker.slow-call-rate-threshold=80",
                "reyga.config.circuit-breaker.slow-call-duration-threshold-seconds=4",
                "reyga.config.circuit-breaker.minimum-number-of-calls=5",
                "reyga.config.circuit-breaker.sliding-window-size=10",
                "reyga.config.circuit-breaker.permitted-number-of-calls-in-half-open-state=2",
                "reyga.config.circuit-breaker.wait-duration-in-open-state-seconds=12"};
        ApplicationContextRunner legacy = new ApplicationContextRunner()
                .withUserConfiguration(CommonConfig.class, CommonDatabaseConfig.class, CommonIOConfig.class,
                        CoreConfig.class, AsyncLoggingConfig.class, LoggingConfig.class)
                .withPropertyValues(flags).withBean(JdbcTemplate.class, () -> mock(JdbcTemplate.class))
                .withBean(PlatformTransactionManager.class, () -> mock(PlatformTransactionManager.class));
        Set<String> beanNames = Set.of("loggerInjector", "logger", "queryProcessor", "fileInspector", "fileSanitizer",
                "asyncExecutor", "loggingService", "loggingWebMvcConfigurer", "defaultExceptionHandler",
                "defaultAppValidationExceptionHandler", "defaultValidationExceptionHandler", "resilienceService",
                "transactionalExecutor", "defaultRateLimiterConfig", "rateLimiterRegistry", "defaultRetryConfig", "retryRegistry", "defaultCircuitBreakerConfig", "circuitBreakerRegistry");
        legacy.run(oldContext -> {
            assertNull(oldContext.getStartupFailure());
            runner().withPropertyValues(flags).withBean(JdbcTemplate.class, () -> mock(JdbcTemplate.class))
                    .withBean(PlatformTransactionManager.class, () -> mock(PlatformTransactionManager.class))
                    .run(context -> {
                        assertNull(context.getStartupFailure());
                        for (String name : beanNames) {
                            assertEquals(oldContext.getBean(name).getClass(), context.getBean(name).getClass(), name);
                        }
                        assertNotNull(context.getBean(TransactionalExecutor.class));
                        assertEquals(3, context.getBean(RateLimiterRegistry.class).getDefaultConfig().getLimitForPeriod());
                        assertEquals(1, context.getBeansOfType(ResilienceService.class).size());
                        var circuitConfig = context.getBean(CircuitBreakerRegistry.class).getDefaultConfig();
                        assertEquals(50, circuitConfig.getFailureRateThreshold());
                        assertEquals(10, circuitConfig.getSlidingWindowSize());
                        // Preserve the existing mapping, even when a different wait-duration property is supplied.
                        assertEquals(4000L, circuitConfig.getWaitIntervalFunctionInOpenState().apply(1));
                    });
        });
    }

    @Test
    void should_AvoidDuplicateCoreBeans_When_LegacyConfigurationIsExplicitlyImported() {
        runner().withUserConfiguration(CoreConfig.class)
                .withBean(JdbcTemplate.class, () -> mock(JdbcTemplate.class))
                .withPropertyValues("reyga.config.default-bean.exception-handler=true")
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    assertEquals(1, context.getBeansOfType(DefaultExceptionHandler.class).size());
                    assertSame(context.getBean(DefaultExceptionHandler.class), context.getBean(CoreConfig.class).defaultExceptionHandler());
                    assertTrue(context.getBeansOfType(ResilienceAutoConfiguration.class).isEmpty());
                });
    }

    @Test
    void should_FailStartup_When_RequiredJdbcTemplateIsMissing() {
        runner().run(context -> {
            assertNotNull(context.getStartupFailure());
            assertTrue(context.getStartupFailure().toString().contains("queryProcessor"));
        });
    }

    @Test
    void should_FailStartup_When_EnabledUtilitiesHaveNoTransactionManager() {
        runner().withBean(JdbcTemplate.class, () -> mock(JdbcTemplate.class))
                .withPropertyValues("reyga.config.default-bean.utilities=true")
                .run(context -> {
                    assertNotNull(context.getStartupFailure());
                    assertTrue(context.getStartupFailure().toString().contains("transactionalExecutor"));
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    static class BootSelection {
    }
}
