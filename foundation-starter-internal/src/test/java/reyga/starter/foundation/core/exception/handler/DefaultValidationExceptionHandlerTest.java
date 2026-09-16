package reyga.starter.foundation.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import reyga.starter.foundation.common.enumeration.StarterHeaderEnum;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
import reyga.starter.foundation.common.enumeration.ServiceStatusResponseEnum;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.model.dto.response.FieldErrorDetail;
import reyga.starter.foundation.common.model.dto.response.ResponseError;
import reyga.starter.foundation.core.exception.AppFaultContent;
import reyga.starter.foundation.core.exception.ValidationFaultException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class DefaultValidationExceptionHandlerTest {

    private DefaultValidationExceptionHandler handler;
    private CommonLogger logger;
    private HttpServletRequest servletRequest;

    @BeforeEach
    void setUp() {
        handler = new DefaultValidationExceptionHandler();
        logger = mock(CommonLogger.class);
        servletRequest = mock(HttpServletRequest.class);
        handler.logger = logger;
    }

    @Test
    void should_ReturnFieldErrorDetailsAndStoreException_When_RequestValidationFails() {
        // given
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "name", "name is required"));
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        // when
        ResponseEntity<ResponseError> result =
                handler.handleMethodArgumentNotValidException(exception, servletRequest);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        ResponseError body = result.getBody();
        assertNotNull(body);
        assertEquals(ServiceStatusResponseEnum.FAILED.getLabel(), body.getStatus());
        assertEquals(ServiceCodeEnum.VALIDATION_ERROR.getCode(), body.getCode());
        assertEquals(ServiceCodeEnum.VALIDATION_ERROR.getMessage(), body.getMessage());
        assertNotNull(body.getDetails());
        assertNotNull(body.getDetails().getRequestFieldDetails());
        assertEquals(1, body.getDetails().getRequestFieldDetails().size());
        assertEquals("name", body.getDetails().getRequestFieldDetails().getFirst().getField());
        assertEquals(
                "name is required",
                body.getDetails().getRequestFieldDetails().getFirst().getMessage()
        );
        assertNotNull(body.getDetails().getRequestFieldDetails().getFirst().getTimestamp());
        verify(servletRequest).setAttribute(StarterHeaderEnum.EXCEPTION.getValue(), exception);
        verifyNoMoreInteractions(servletRequest);
    }

    @Test
    void should_ReturnFaultResponseAndLogException_When_ValidationFaultExceptionOccurs() {
        // given
        FieldErrorDetail fieldError = FieldErrorDetail.builder().field("id").message("ID is mandatory").build();
        ValidationFaultException exception = new ValidationFaultException(
                AppFaultContent.buildAppFaultContent(
                        "fault", ServiceCodeEnum.VALIDATION_ERROR.getCode(), ServiceCodeEnum.VALIDATION_ERROR.getMessage(), "Value is Mandatory", HttpStatus.BAD_REQUEST
                ), "notification", "Invalid notification identifier", java.util.List.of(fieldError)
        );

        // when
        ResponseEntity<ResponseError> result =
                handler.handleValidationFaultException(exception, servletRequest);

        // then
        assertError(result, HttpStatus.BAD_REQUEST, ServiceCodeEnum.VALIDATION_ERROR.getCode(), ServiceCodeEnum.VALIDATION_ERROR.getMessage());
        assertNotNull(result.getBody().getDetails());
        assertEquals("notification", result.getBody().getDetails().getBusiness());
        assertEquals("Invalid notification identifier", result.getBody().getDetails().getAdditionalInfo());
        assertNotNull(result.getBody().getDetails().getTimestamp());
        assertEquals(java.util.List.of(fieldError), result.getBody().getDetails().getRequestFieldDetails());
        assertNull(result.getBody().getDetails().getFileDetails());
        verify(servletRequest).setAttribute(StarterHeaderEnum.EXCEPTION.getValue(), exception);
        verify(logger).exception("VALIDATIONFAULTEXCEPTION", "Value is Mandatory", exception);
        verifyNoMoreInteractions(servletRequest, logger);
    }

    private void assertError(
            ResponseEntity<ResponseError> response,
            HttpStatus status,
            String code,
            String message
    ) {
        assertEquals(status, response.getStatusCode());
        ResponseError body = response.getBody();
        assertNotNull(body);
        assertEquals(ServiceStatusResponseEnum.FAILED.getLabel(), body.getStatus());
        assertEquals(code, body.getCode());
        assertEquals(message, body.getMessage());
    }
}
