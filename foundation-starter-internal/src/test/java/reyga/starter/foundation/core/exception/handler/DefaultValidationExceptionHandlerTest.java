package reyga.starter.foundation.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import reyga.starter.foundation.common.enumeration.HeaderEnum;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
import reyga.starter.foundation.common.enumeration.ServiceStatusResponseEnum;
import reyga.starter.foundation.common.model.dto.response.ResponseError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class DefaultValidationExceptionHandlerTest {

    @Test
    void should_ReturnFieldErrorDetailsAndStoreException_When_RequestValidationFails() {
        // given
        DefaultValidationExceptionHandler handler = new DefaultValidationExceptionHandler();
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
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
        verify(servletRequest).setAttribute(HeaderEnum.EXCEPTION.getValue(), exception);
        verifyNoMoreInteractions(servletRequest);
    }
}
