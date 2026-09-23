package reyga.starter.foundation.core.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import reyga.starter.foundation.common.model.dto.content.BaseContent;
import reyga.starter.foundation.core.annotation.AroundExecution;
import reyga.starter.foundation.core.aspect.AspectAround;
import reyga.starter.foundation.core.aspect.BaseAspectAround;
import reyga.starter.foundation.core.exception.handler.DefaultValidationExceptionHandler;

import static org.junit.jupiter.api.Assertions.*;

class CoreConfigTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(CoreConfig.class);

    @Test
    void should_ReturnValidationExceptionHandler_When_BeanIsCreated() {
        // given
        CoreConfig config = new CoreConfig();
        // when
        DefaultValidationExceptionHandler handler = config.defaultValidationExceptionHandler();
        // then
        assertNotNull(handler);
        assertEquals(DefaultValidationExceptionHandler.class, handler.getClass());
    }

    @Test
    void should_NotCreateAroundAspect_When_FeatureIsDisabled() {
        runner.withPropertyValues("reyga.config.aspect.around=false")
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    assertTrue(context.getBeansOfType(AspectAround.class).isEmpty());
                });
    }

    @Test
    void should_UseConfiguredBehavior_When_AnnotatedMethodIsInvokedThroughSpringProxy() {
        runner.withUserConfiguration(AspectBehaviorConfig.class)
                .withPropertyValues(
                        "reyga.config.aspect.around=true",
                        "reyga.config.aspect.behavior=auditAspectBehavior"
                )
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    assertNotNull(context.getBean(AspectAround.class));
                    TestAspectBehavior selected = context.getBean("auditAspectBehavior", TestAspectBehavior.class);
                    TestAspectBehavior ignored = context.getBean("otherAspectBehavior", TestAspectBehavior.class);
                    AnnotatedService service = context.getBean(AnnotatedService.class);

                    assertEquals("processed", service.process());
                    assertEquals(1, selected.preHandleCount);
                    assertEquals(1, selected.postHandleCount);
                    assertEquals(0, ignored.preHandleCount);
                    assertEquals(0, ignored.postHandleCount);

                    assertEquals("plain", service.plain());
                    assertEquals(1, selected.preHandleCount);
                    assertEquals(1, selected.postHandleCount);
                });
    }

    @Test
    void should_FailStartup_When_BehaviorPropertyIsMissing() {
        runner.withPropertyValues("reyga.config.aspect.around=true")
                .run(context -> assertStartupFailureContains(
                        context.getStartupFailure(),
                        "reyga.config.aspect.behavior must contain the bean name"
                ));
    }

    @Test
    void should_FailStartup_When_ConfiguredBehaviorBeanDoesNotExist() {
        runner.withPropertyValues(
                        "reyga.config.aspect.around=true",
                        "reyga.config.aspect.behavior=missingAspectBehavior"
                )
                .run(context -> assertStartupFailureContains(
                        context.getStartupFailure(),
                        "No bean named 'missingAspectBehavior' was found"
                ));
    }

    @Test
    void should_FailStartup_When_ConfiguredBehaviorDoesNotExtendBaseAspectAround() {
        runner.withUserConfiguration(InvalidAspectBehaviorConfig.class)
                .withPropertyValues(
                        "reyga.config.aspect.around=true",
                        "reyga.config.aspect.behavior=invalidAspectBehavior"
                )
                .run(context -> assertStartupFailureContains(
                        context.getStartupFailure(),
                        "must extend BaseAspectAround"
                ));
    }

    private static void assertStartupFailureContains(Throwable failure, String expectedMessage) {
        assertNotNull(failure);
        Throwable rootCause = failure;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        assertTrue(rootCause.getMessage().contains(expectedMessage), rootCause.getMessage());
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAspectJAutoProxy
    static class AspectBehaviorConfig {
        @Bean("auditAspectBehavior")
        TestAspectBehavior auditAspectBehavior() {
            return new TestAspectBehavior();
        }

        @Bean("otherAspectBehavior")
        TestAspectBehavior otherAspectBehavior() {
            return new TestAspectBehavior();
        }

        @Bean
        AnnotatedService annotatedService() {
            return new AnnotatedService();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class InvalidAspectBehaviorConfig {
        @Bean("invalidAspectBehavior")
        String invalidAspectBehavior() {
            return "invalid";
        }
    }

    static final class TestContent extends BaseContent {
    }

    static final class TestAspectBehavior extends BaseAspectAround<TestContent> {
        private int preHandleCount;
        private int postHandleCount;

        @Override
        protected TestContent preHandle() {
            preHandleCount++;
            return new TestContent();
        }

        @Override
        protected void postHandle(TestContent content) {
            postHandleCount++;
        }

        @Override
        protected boolean shouldHandleException() {
            return false;
        }
    }

    static class AnnotatedService {
        @AroundExecution
        public String process() {
            return "processed";
        }

        public String plain() {
            return "plain";
        }
    }
}
