package reyga.starter.foundation.core.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import reyga.starter.foundation.common.model.dto.response.FieldErrorDetail;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidationFaultExceptionTest {

    @Test
    void should_ReturnImmutableFieldDetails_When_ResponseMetadataIsProvided() {
        // Given
        FieldErrorDetail fieldError = FieldErrorDetail.builder().field("email").message("invalid").build();
        List<FieldErrorDetail> source = new ArrayList<>(List.of(fieldError));

        // When
        ValidationFaultException result = new ValidationFaultException(
                "01", "Invalid Request", "diagnostic", HttpStatus.BAD_REQUEST,
                "customer", "Invalid customer input", source
        );
        source.clear();

        // Then
        assertEquals("01", result.getFaultContent().getErrorCode());
        assertEquals("Invalid Request", result.getFaultContent().getErrorMessage());
        assertEquals(HttpStatus.BAD_REQUEST, result.getFaultContent().getStatusCode());
        assertEquals("diagnostic", result.getFaultContent().getFaultInfo());
        assertEquals("customer", result.getBusiness());
        assertEquals("Invalid customer input", result.getAdditionalInfo());
        assertEquals(List.of(fieldError), result.getFieldErrorList());
        assertThrows(UnsupportedOperationException.class, () -> result.getFieldErrorList().add(fieldError));
        assertFalse(AppFaultException.class.isAssignableFrom(ValidationFaultException.class));
    }

    @Test
    void should_ReturnNullResponseDetails_When_LegacyConstructorIsUsed() {
        // Given
        AppFaultContent content = AppFaultContent.buildAppFaultContent(
                "validation", "01", "Invalid Request", "diagnostic", HttpStatus.BAD_REQUEST
        );

        // When
        ValidationFaultException result = new ValidationFaultException(content);

        // Then
        assertEquals("01", result.getFaultContent().getErrorCode());
        assertEquals("Invalid Request", result.getFaultContent().getErrorMessage());
        assertEquals("diagnostic", result.getFaultContent().getFaultInfo());
        assertNull(result.getBusiness());
        assertNull(result.getAdditionalInfo());
        assertNull(result.getFieldErrorList());
    }

    @Test
    void should_ThrowNullPointerException_When_FaultContentIsNull() {
        // Given
        AppFaultContent content = null;

        // When
        NullPointerException result = assertThrows(
                NullPointerException.class,
                () -> new ValidationFaultException(content)
        );

        // Then
        assertEquals("faultContent must not be null", result.getMessage());
    }
}
