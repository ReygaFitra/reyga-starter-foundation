package reyga.starter.foundation.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import reyga.starter.foundation.common.enumeration.HeaderEnum;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
import reyga.starter.foundation.common.enumeration.ServiceStatusResponseEnum;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.model.dto.response.ResponseError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class DefaultExceptionHandlerTest {

    private DefaultExceptionHandler handler;
    private CommonLogger logger;
    private HttpServletRequest servletRequest;

    @BeforeEach
    void setUp() {
        handler = new DefaultExceptionHandler();
        logger = mock(CommonLogger.class);
        servletRequest = mock(HttpServletRequest.class);
        handler.logger = logger;
    }

    @Test
    void should_ReturnGeneralErrorAndLogException_When_IllegalArgumentExceptionOccurs() {
        // given
        IllegalArgumentException exception = new IllegalArgumentException("invalid argument");

        // when
        ResponseEntity<ResponseError> result =
                handler.handleGlobalErrorException(exception, servletRequest);

        // then
        assertErrorResponse(
                result,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ServiceCodeEnum.GLOBAL_ERROR.getCode(),
                "GENERAL ERROR"
        );
        verify(servletRequest).setAttribute(HeaderEnum.EXCEPTION.getValue(), exception);
        verify(logger).warn(eq("ILLEGAL ARGUMENT EXCEPTION ERROR :"), any(Object[].class));
        verify(logger).exception("ILLEGALARGUMENTEXCEPTION", null, exception);
        verifyNoMoreInteractions(servletRequest, logger);
    }

    @Test
    void should_ReturnGlobalErrorAndLogException_When_GenericExceptionOccurs() {
        // given
        RuntimeException exception = new RuntimeException("unexpected failure");

        // when
        ResponseEntity<ResponseError> result =
                handler.handleGlobalErrorException(exception, servletRequest);

        // then
        assertErrorResponse(
                result,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ServiceCodeEnum.GLOBAL_ERROR.getCode(),
                ServiceCodeEnum.GLOBAL_ERROR.getMessage()
        );
        verify(servletRequest).setAttribute(HeaderEnum.EXCEPTION.getValue(), exception);
        verify(logger).warn(eq("GLOBAL ERROR :"), any(Object[].class));
        verify(logger).exception("GLOBAL ERROR", null, exception);
        verifyNoMoreInteractions(servletRequest, logger);
    }

    @Test
    void should_ReturnDatabaseErrorAndLogJpaFailure_When_JpaSystemExceptionOccurs() {
        // given
        JpaSystemException exception = new JpaSystemException(new RuntimeException("database failure"));

        // when
        ResponseEntity<ResponseError> result =
                handler.handleDatabaseErrorException(exception, servletRequest);

        // then
        assertErrorResponse(
                result,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ServiceCodeEnum.DATABASE_ERROR.getCode(),
                ServiceCodeEnum.DATABASE_ERROR.getMessage()
        );
        verify(servletRequest).setAttribute(HeaderEnum.EXCEPTION.getValue(), exception);
        verify(logger).warn(eq("JPA ERROR :"), any(Object[].class));
        verify(logger).warn(eq("JDBC ERROR :"), any(Object[].class));
        verify(logger).exception("DATAACCESSEXCEPTION", null, exception);
        verifyNoMoreInteractions(servletRequest, logger);
    }

    @Test
    void should_ReturnDatabaseErrorAndLogJdbcFailure_When_DataAccessExceptionOccurs() {
        // given
        DataAccessException exception = new DataAccessException("database failure") {
        };

        // when
        ResponseEntity<ResponseError> result =
                handler.handleDatabaseErrorException(exception, servletRequest);

        // then
        assertErrorResponse(
                result,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ServiceCodeEnum.DATABASE_ERROR.getCode(),
                ServiceCodeEnum.DATABASE_ERROR.getMessage()
        );
        verify(servletRequest).setAttribute(HeaderEnum.EXCEPTION.getValue(), exception);
        verify(logger).warn(eq("JDBC ERROR :"), any(Object[].class));
        verify(logger).exception("DATAACCESSEXCEPTION", null, exception);
        verifyNoMoreInteractions(servletRequest, logger);
    }

    private void assertErrorResponse(
            ResponseEntity<ResponseError> response,
            HttpStatus expectedStatus,
            String expectedCode,
            String expectedMessage
    ) {
        assertEquals(expectedStatus, response.getStatusCode());
        ResponseError body = response.getBody();
        assertNotNull(body);
        assertEquals(ServiceStatusResponseEnum.FAILED.getLabel(), body.getStatus());
        assertEquals(expectedCode, body.getCode());
        assertEquals(expectedMessage, body.getMessage());
    }
}
