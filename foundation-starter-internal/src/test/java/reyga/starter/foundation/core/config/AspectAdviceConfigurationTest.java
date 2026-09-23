package reyga.starter.foundation.core.config;

import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import reyga.starter.foundation.core.annotation.AfterExecution;
import reyga.starter.foundation.core.annotation.AfterReturningExecution;
import reyga.starter.foundation.core.annotation.AfterThrowingExecution;
import reyga.starter.foundation.core.annotation.BeforeExecution;
import reyga.starter.foundation.core.aspect.AspectAfter;
import reyga.starter.foundation.core.aspect.AspectAfterReturning;
import reyga.starter.foundation.core.aspect.AspectAfterThrowing;
import reyga.starter.foundation.core.aspect.AspectBefore;
import reyga.starter.foundation.core.aspect.BaseAspectAfter;
import reyga.starter.foundation.core.aspect.BaseAspectAfterReturning;
import reyga.starter.foundation.core.aspect.BaseAspectAfterThrowing;
import reyga.starter.foundation.core.aspect.BaseAspectBefore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AspectAdviceConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(CoreConfig.class, AdviceBehaviorConfig.class);

    @Test
    void should_InvokeEachConfiguredBehavior_When_AnnotatedMethodsAreCalled() {
        runner.withPropertyValues(
                        "reyga.config.aspect.before=true",
                        "reyga.config.aspect.before-behavior=beforeBehavior",
                        "reyga.config.aspect.after=true",
                        "reyga.config.aspect.after-behavior=afterBehavior",
                        "reyga.config.aspect.after-returning=true",
                        "reyga.config.aspect.after-returning-behavior=afterReturningBehavior",
                        "reyga.config.aspect.after-throwing=true",
                        "reyga.config.aspect.after-throwing-behavior=afterThrowingBehavior"
                )
                .run(context -> {
                    assertNotNull(context.getBean(AspectBefore.class));
                    assertNotNull(context.getBean(AspectAfter.class));
                    assertNotNull(context.getBean(AspectAfterReturning.class));
                    assertNotNull(context.getBean(AspectAfterThrowing.class));

                    AdviceService service = context.getBean(AdviceService.class);
                    BeforeBehavior before = context.getBean(BeforeBehavior.class);
                    AfterBehavior after = context.getBean(AfterBehavior.class);
                    AfterReturningBehavior returning = context.getBean(AfterReturningBehavior.class);
                    AfterThrowingBehavior throwing = context.getBean(AfterThrowingBehavior.class);

                    assertEquals("before", service.before());
                    assertEquals(1, before.invocations);
                    assertTrue(before.methodName.endsWith("before"));

                    assertEquals("after-success", service.afterSuccess());
                    IllegalArgumentException afterFailure = assertThrows(
                            IllegalArgumentException.class,
                            service::afterFailure
                    );
                    assertEquals("after-failure", afterFailure.getMessage());
                    assertEquals(2, after.invocations);

                    assertEquals("returned-value", service.afterReturning());
                    assertEquals(1, returning.invocations);
                    assertEquals("returned-value", returning.result);

                    IllegalStateException expected = assertThrows(
                            IllegalStateException.class,
                            service::afterThrowing
                    );
                    assertEquals(1, throwing.invocations);
                    assertSame(expected, throwing.throwable);

                    assertEquals("plain", service.plain());
                    assertEquals(1, before.invocations);
                    assertEquals(2, after.invocations);
                    assertEquals(1, returning.invocations);
                    assertEquals(1, throwing.invocations);
                });
    }

    @Test
    void should_FailStartup_When_EnabledAdviceHasNoBehavior() {
        assertMissingBehavior(
                "reyga.config.aspect.before=true",
                "reyga.config.aspect.before-behavior"
        );
        assertMissingBehavior(
                "reyga.config.aspect.after=true",
                "reyga.config.aspect.after-behavior"
        );
        assertMissingBehavior(
                "reyga.config.aspect.after-returning=true",
                "reyga.config.aspect.after-returning-behavior"
        );
        assertMissingBehavior(
                "reyga.config.aspect.after-throwing=true",
                "reyga.config.aspect.after-throwing-behavior"
        );
    }

    private void assertMissingBehavior(String enabledProperty, String behaviorProperty) {
        new ApplicationContextRunner()
                .withUserConfiguration(CoreConfig.class)
                .withPropertyValues(enabledProperty)
                .run(context -> {
                    Throwable rootCause = context.getStartupFailure();
                    assertNotNull(rootCause);
                    while (rootCause.getCause() != null) {
                        rootCause = rootCause.getCause();
                    }
                    assertTrue(rootCause.getMessage().contains(behaviorProperty));
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class AdviceBehaviorConfig {
        @Bean("beforeBehavior")
        BeforeBehavior beforeBehavior() {
            return new BeforeBehavior();
        }

        @Bean("afterBehavior")
        AfterBehavior afterBehavior() {
            return new AfterBehavior();
        }

        @Bean("afterReturningBehavior")
        AfterReturningBehavior afterReturningBehavior() {
            return new AfterReturningBehavior();
        }

        @Bean("afterThrowingBehavior")
        AfterThrowingBehavior afterThrowingBehavior() {
            return new AfterThrowingBehavior();
        }

        @Bean
        AdviceService adviceService() {
            return new AdviceService();
        }
    }

    static final class BeforeBehavior extends BaseAspectBefore {
        private int invocations;
        private String methodName;

        @Override
        protected void beforeHandle(JoinPoint joinPoint) {
            invocations++;
            methodName = joinPoint.getSignature().getName();
        }
    }

    static final class AfterBehavior extends BaseAspectAfter {
        private int invocations;

        @Override
        protected void afterHandle(JoinPoint joinPoint) {
            invocations++;
        }
    }

    static final class AfterReturningBehavior extends BaseAspectAfterReturning {
        private int invocations;
        private Object result;

        @Override
        protected void afterReturningHandle(JoinPoint joinPoint, Object result) {
            invocations++;
            this.result = result;
        }
    }

    static final class AfterThrowingBehavior extends BaseAspectAfterThrowing {
        private int invocations;
        private Throwable throwable;

        @Override
        protected void afterThrowingHandle(JoinPoint joinPoint, Throwable throwable) {
            invocations++;
            this.throwable = throwable;
        }
    }

    static class AdviceService {
        @BeforeExecution
        public String before() {
            return "before";
        }

        @AfterExecution
        public String afterSuccess() {
            return "after-success";
        }

        @AfterExecution
        public String afterFailure() {
            throw new IllegalArgumentException("after-failure");
        }

        @AfterReturningExecution
        public String afterReturning() {
            return "returned-value";
        }

        @AfterThrowingExecution
        public String afterThrowing() {
            throw new IllegalStateException("throwing-failure");
        }

        public String plain() {
            return "plain";
        }
    }
}
