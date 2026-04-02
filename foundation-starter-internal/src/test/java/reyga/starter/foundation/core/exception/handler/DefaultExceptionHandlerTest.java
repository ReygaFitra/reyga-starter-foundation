package reyga.starter.foundation.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
import reyga.starter.foundation.common.model.dto.response.ResponseErrorDetail;
import reyga.starter.foundation.common.model.dto.response.ResponseError;
import reyga.starter.foundation.common_io.enumeration.IOOperation;
import reyga.starter.foundation.common_io.exception.IOFaultException;
import reyga.starter.foundation.common_io.exception.IOFaultMetadata;
import reyga.starter.foundation.core.exception.AppFaultContent;
import reyga.starter.foundation.core.exception.AppFaultException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultExceptionHandlerTest {

    @Test
    void processGlobalErrorHandler_handlesIllegalArgument() {
        DefaultExceptionHandler handler = new DefaultExceptionHandler();
        HttpServletRequest request = mockRequest();

        ResponseEntity<ResponseError> response = handler.handleGlobalErrorException(
                new IllegalArgumentException("bad"), request
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("91", response.getBody().getCode());
        assertEquals("GENERAL ERROR", response.getBody().getMessage());
    }

    @Test
    void processGlobalErrorHandler_handlesGenericException() {
        DefaultExceptionHandler handler = new DefaultExceptionHandler();
        HttpServletRequest request = mockRequest();

        ResponseEntity<ResponseError> response = handler.handleGlobalErrorException(
                new RuntimeException("fail"), request
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("99", response.getBody().getCode());
        assertEquals("INTERNAL SERVER ERROR", response.getBody().getMessage());
    }

    @Test
    void processDatabaseErrorHandler_handlesJpaSystemException() {
        DefaultExceptionHandler handler = new DefaultExceptionHandler();
        HttpServletRequest request = mockRequest();

        ResponseEntity<ResponseError> response = handler.handleDatabaseErrorException(
                new JpaSystemException(new RuntimeException("db")), request
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("99", response.getBody().getCode());
        assertEquals("DATABASE ERROR", response.getBody().getMessage());
    }

    @Test
    void processDatabaseErrorHandler_handlesDataAccessException() {
        DefaultExceptionHandler handler = new DefaultExceptionHandler();
        HttpServletRequest request = mockRequest();

        ResponseEntity<ResponseError> response = handler.handleDatabaseErrorException(
                new DataAccessException("db") {}, request
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("99", response.getBody().getCode());
        assertEquals("DATABASE ERROR", response.getBody().getMessage());
    }

    @Test
    void processAppFaultErrorHandler_returnsAppFaultResponse() {
        DefaultAppValidationExceptionHandler handler = new DefaultAppValidationExceptionHandler();
        HttpServletRequest request = mockRequest();
        AppFaultException ex = new AppFaultException(
                AppFaultContent.buildAppFaultContent("msg", "01", "err", "fault", HttpStatus.CONFLICT)
        );

        ResponseEntity<ResponseError> response = handler.handleAppFaultException(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("01", response.getBody().getCode());
        assertEquals("err", response.getBody().getMessage());
    }

    @Test
    void handleMethodArgumentNotValidException_returnsFieldErrors() {
        DefaultAppValidationExceptionHandler handler = new DefaultAppValidationExceptionHandler();
        HttpServletRequest request = mockRequest();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new BeanWrapperImpl(), "payload");
        bindingResult.addError(new FieldError("payload", "name", "required"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ResponseError> response = handler.handleMethodArgumentNotValidException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ServiceCodeEnum.VALIDATION_ERROR.getCode(), response.getBody().getCode());
        ResponseErrorDetail details = response.getBody().getDetails();
        assertNotNull(details);
        assertNotNull(details.getRequestFieldDetails());
        assertEquals(1, details.getRequestFieldDetails().size());
        assertEquals("name", details.getRequestFieldDetails().get(0).getField());
        assertSame(ex, request.getAttribute(reyga.starter.foundation.common.enumeration.HeaderEnum.EXCEPTION.getValue()));
    }

    @Test
    void handleIOFaultException_returnsFileDetails() {
        DefaultAppValidationExceptionHandler handler = new DefaultAppValidationExceptionHandler();
        HttpServletRequest request = mockRequest();
        IOFaultMetadata metadata = IOFaultMetadata.builder()
                .fileName("supersecret.txt")
                .operation(IOOperation.READ)
                .mimeType(MediaType.TEXT_PLAIN)
                .charset("UTF-8")
                .directory(false)
                .sizeBytes(10L)
                .lastModifiedEpochMillis(100L)
                .build();
        IOFaultException fex = new IOFaultException("io error", metadata);

        ResponseEntity<ResponseError> response = handler.handleIOFaultException(fex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ServiceCodeEnum.FILE_ERROR.getCode(), response.getBody().getCode());
        ResponseErrorDetail details = response.getBody().getDetails();
        assertNotNull(details);
        assertNotNull(details.getFileDetails());
        assertEquals(1, details.getFileDetails().size());
        assertEquals("sup*****ret.txt", details.getFileDetails().get(0).getFileName());
        assertEquals(IOOperation.READ.toString(), details.getFileDetails().get(0).getOperation());
        assertEquals(MediaType.TEXT_PLAIN.toString(), details.getFileDetails().get(0).getMimeType());
        assertEquals("UTF-8", details.getFileDetails().get(0).getCharset());
        assertFalse(details.getFileDetails().get(0).isDirectory());
        assertEquals(10L, details.getFileDetails().get(0).getSizeBytes());
        assertEquals(100L, details.getFileDetails().get(0).getLastModifiedEpochMillis());
        assertEquals(0L, details.getFileDetails().get(0).getOffset());
        assertEquals(0L, details.getFileDetails().get(0).getLength());
        assertSame(fex, request.getAttribute(reyga.starter.foundation.common.enumeration.HeaderEnum.EXCEPTION.getValue()));
    }

    private HttpServletRequest mockRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Map<String, Object> attributes = new HashMap<>();
        doAnswer(invocation -> {
            attributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(request).setAttribute(anyString(), any());
        when(request.getAttribute(anyString())).thenAnswer(invocation -> attributes.get(invocation.getArgument(0)));
        return request;
    }
}
