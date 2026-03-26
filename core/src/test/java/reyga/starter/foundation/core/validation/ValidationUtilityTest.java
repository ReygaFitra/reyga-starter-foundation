package reyga.starter.foundation.core.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidationUtilityTest {

    @Test
    void chain_throwsWhenNotRegistered() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, ValidationUtility::chain);
        assertTrue(ex.getMessage().contains("validation-handler=true"));
    }

    @Test
    void chain_returnsRegisteredInstance_andDelegatesValidation() {
        BaseValidationProcessor processor = mock(BaseValidationProcessor.class);
        ValidationUtility.registerDefault(processor);

        ValidationUtility utility = ValidationUtility.chain();
        utility.validateRequest("request");

        verify(processor).validateRequest("request", true);
    }
}
