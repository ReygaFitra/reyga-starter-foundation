package reyga.starter.foundation.core.validation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidationUtilityTest {

    @AfterEach
    void tearDown() throws Exception {
        Field defaultInstance = ValidationUtility.class.getDeclaredField("defaultInstance");
        defaultInstance.setAccessible(true);
        defaultInstance.set(null, null);
    }

    @Test
    void should_ThrowIllegalStateException_When_DefaultProcessorIsNotRegistered() throws Exception {
        // Given
        tearDown();

        // When
        IllegalStateException result = assertThrows(IllegalStateException.class, ValidationUtility::chain);

        // Then
        assertTrue(result.getMessage().contains("validation-handler=true"));
    }

    @Test
    void should_ReturnChainAndValidateWithMapPattern_When_DefaultProcessorIsRegistered() {
        // Given
        BaseValidationProcessor processor = mock(BaseValidationProcessor.class);
        ValidationUtility.registerDefault(processor);

        // When
        ValidationUtility utility = ValidationUtility.chain().validateRequest("request");

        // Then
        assertSame(ValidationUtility.chain(), utility);
        verify(processor).validateRequest("request", true);
        verifyNoMoreInteractions(processor);
    }

    @Test
    void should_ReturnChainAndValidateGroups_When_ValidationGroupsAreProvided() {
        // Given
        BaseValidationProcessor processor = mock(BaseValidationProcessor.class);
        List<Class<Object>> groups = List.of(Object.class);
        ValidationUtility.registerDefault(processor);

        // When
        ValidationUtility result = ValidationUtility.chain().validateRequest("request", groups);

        // Then
        assertSame(ValidationUtility.chain(), result);
        verify(processor).validateRequest("request", true, groups);
        verifyNoMoreInteractions(processor);
    }

    @Test
    void should_DelegateValidation_When_MapPatternIsSpecified() {
        // Given
        BaseValidationProcessor processor = mock(BaseValidationProcessor.class);
        ValidationUtility.registerDefault(processor);

        // When
        ValidationUtility.chain().validateRequest("request", false);

        // Then
        verify(processor).validateRequest("request", false);
        verifyNoMoreInteractions(processor);
    }

    @Test
    void should_DelegateGroupValidation_When_MapPatternAndGroupsAreSpecified() {
        // Given
        BaseValidationProcessor processor = mock(BaseValidationProcessor.class);
        List<Class<Object>> groups = List.of(Object.class);
        ValidationUtility.registerDefault(processor);

        // When
        ValidationUtility.chain().validateRequest("request", false, groups);

        // Then
        verify(processor).validateRequest("request", false, groups);
        verifyNoMoreInteractions(processor);
    }
}
