package reyga.starter.foundation.core.config;

import org.junit.jupiter.api.Test;
import reyga.starter.foundation.core.validation.ValidationProcessor;
import reyga.starter.foundation.core.validation.ValidationUtility;

import static org.junit.jupiter.api.Assertions.*;

class CoreConfigTest {

    @Test
    void should_ReturnValidationProcessorAndRegisterUtility_When_BeanIsCreated() {
        // given
        CoreConfig config = new CoreConfig();

        // when
        ValidationProcessor processor = config.validationProcessor();

        // then
        assertNotNull(processor);
        assertDoesNotThrow(ValidationUtility::chain);
    }
}
