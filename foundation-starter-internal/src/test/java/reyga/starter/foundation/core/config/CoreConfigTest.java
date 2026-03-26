package reyga.starter.foundation.core.config;

import org.junit.jupiter.api.Test;
import reyga.starter.foundation.core.validation.ValidationProcessor;
import reyga.starter.foundation.core.validation.ValidationUtility;

import static org.junit.jupiter.api.Assertions.*;

class CoreConfigTest {

    @Test
    void validationProcessor_registersDefaultUtility() {
        CoreConfig config = new CoreConfig();
        ValidationProcessor processor = config.validationProcessor();

        assertNotNull(processor);
        assertDoesNotThrow(ValidationUtility::chain);
    }
}
