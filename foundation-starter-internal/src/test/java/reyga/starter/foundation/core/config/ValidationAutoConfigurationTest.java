package reyga.starter.foundation.core.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
import reyga.starter.foundation.core.exception.ValidationFaultException;
import reyga.starter.foundation.core.validation.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidationAutoConfigurationTest {
    record Request(@jakarta.validation.constraints.NotBlank(message = "required") String name) {}
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class));

    @Test
    void should_NotCreateUtility_When_FeatureIsDisabled() {
        // given
        var configured = runner.withPropertyValues("reyga.config.default-bean.validation-handler=false");
        // when
        configured.run(context -> {
            // then
            assertNull(context.getStartupFailure());
            assertTrue(context.getBeansOfType(ValidationUtility.class).isEmpty());
        });
    }

    @Test
    void should_NotCreateUtility_When_FeatureIsUnspecified() {
        // given
        var configured = runner;
        // when
        configured.run(context -> {
            // then
            assertNull(context.getStartupFailure());
            assertTrue(context.getBeansOfType(ValidationUtility.class).isEmpty());
        });
    }

    @Test
    void should_CreateDefaultUtility_When_YamlEnablesValidation() {
        // given
        var configured = runner.withPropertyValues("reyga.config.default-bean.validation-handler=true");
        // when
        configured.run(context -> {
            // then
            assertNull(context.getStartupFailure());
            assertEquals(1, context.getBeansOfType(ValidationUtility.class).size());
            var utility = context.getBean(ValidationUtility.class);
            assertSame(utility, utility.validateRequest("valid"));
            assertTrue(context.getBean(ValidationProperties.class).mapDetails());
        });
    }

    @Test
    void should_BindMessageFormat_When_YamlDisablesMapDetails() {
        // given
        var configured = runner.withPropertyValues("reyga.config.default-bean.validation-handler=true",
                "reyga.custom.validation.map-details=false");
        // when
        configured.run(context -> {
            // then
            assertNull(context.getStartupFailure());
            assertEquals(1, context.getBeansOfType(ValidationUtility.class).size());
            assertFalse(context.getBean(ValidationProperties.class).mapDetails());
            var fault = assertThrows(ValidationFaultException.class,
                    () -> context.getBean(ValidationUtility.class).validateRequest(new Request("")));
            assertEquals(java.util.Set.of("required"), fault.getFaultContent().getFaultInfo());
            assertEquals(ServiceCodeEnum.VALIDATION_ERROR.getCode(), fault.getFaultContent().getErrorCode());
            assertEquals("Invalid Request", fault.getFaultContent().getErrorMessage());
            assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, fault.getFaultContent().getStatusCode());
        });
    }

    @Test
    void should_KeepManualUtility_When_YamlIsAlsoEnabled() {
        // given
        var configured = runner.withUserConfiguration(ManualConfig.class)
                .withPropertyValues("reyga.config.default-bean.validation-handler=true");
        // when
        configured.run(context -> {
            // then
            assertNull(context.getStartupFailure());
            assertEquals(1, context.getBeansOfType(ValidationUtility.class).size());
            var utility = context.getBean(ValidationUtility.class);
            assertSame(context.getBean("customValidation"), utility);
            verifyNoInteractions(utility);
        });
    }

    @Test
    void should_UseManualUtility_When_YamlIsAbsent() {
        // given
        var configured = runner.withUserConfiguration(ManualConfig.class);
        // when
        configured.run(context -> {
            // then
            assertNull(context.getStartupFailure());
            assertEquals(1, context.getBeansOfType(ValidationUtility.class).size());
            assertSame(context.getBean("customValidation"), context.getBean(ValidationUtility.class));
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class ManualConfig {
        @Bean
        ValidationUtility customValidation() { return mock(ValidationUtility.class); }
    }

    @Test
    void should_CloseUtility_When_ApplicationContextIsClosed() {
        // given
        var instance = new java.util.concurrent.atomic.AtomicReference<ValidationUtility>();
        // when
        runner.withPropertyValues("reyga.config.default-bean.validation-handler=true")
                .run(context -> instance.set(context.getBean(ValidationUtility.class)));
        var error = assertThrows(IllegalStateException.class, () -> instance.get().validateRequest("r"));
        // then
        assertEquals("ValidationUtility is closed", error.getMessage());
    }

    @Test
    void should_FailStartup_When_BooleanSettingIsInvalid() {
        // given
        var configured = runner.withPropertyValues("reyga.config.default-bean.validation-handler=true",
                "reyga.custom.validation.map-details=invalid");
        // when
        configured.run(context -> {
            // then
            assertNotNull(context.getStartupFailure());
        });
    }

    @Test
    void should_CreateManualBuilderBean_When_YamlIsNotConfigured() {
        // given
        var configured = runner.withUserConfiguration(ManualBuilderConfig.class);
        // when
        configured.run(context -> {
            // then
            assertNull(context.getStartupFailure());
            assertEquals(1, context.getBeansOfType(ValidationUtility.class).size());
            var utility = context.getBean(ValidationUtility.class);
            assertSame(utility, utility.validateRequest(new Request("valid")));
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class ManualBuilderConfig {
        @Bean
        ValidationUtility validationUtility() {
            return ValidationConfig.builder().useDefaultBehavior().build();
        }
    }
}
