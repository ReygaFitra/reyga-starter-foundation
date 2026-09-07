package reyga.starter.foundation.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reyga.starter.foundation.common.enumeration.HeaderEnum;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
import reyga.starter.foundation.common.enumeration.ServiceStatusResponseEnum;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.model.dto.response.FileErrorDetail;
import reyga.starter.foundation.common.model.dto.response.ResponseError;
import reyga.starter.foundation.common_io.enumeration.IOOperation;
import reyga.starter.foundation.common_io.exception.IOFaultException;
import reyga.starter.foundation.common_io.exception.IOFaultMetadata;
import reyga.starter.foundation.core.exception.AppFaultContent;
import reyga.starter.foundation.core.exception.AppFaultException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class DefaultAppValidationExceptionHandlerTest {

    private DefaultAppValidationExceptionHandler handler;
    private CommonLogger logger;
    private HttpServletRequest servletRequest;

    @BeforeEach
    void setUp() {
        handler = new DefaultAppValidationExceptionHandler();
        logger = mock(CommonLogger.class);
        servletRequest = mock(HttpServletRequest.class);
        handler.logger = logger;
    }

    @Test
    void should_ReturnFaultResponseAndLogException_When_AppFaultExceptionOccurs() {
        // given
        AppFaultException exception = new AppFaultException(
                AppFaultContent.buildAppFaultContent(
                        "fault", "409001", "Conflict", "duplicate", HttpStatus.CONFLICT
                )
        );

        // when
        ResponseEntity<ResponseError> result =
                handler.handleAppFaultException(exception, servletRequest);

        // then
        assertError(result, HttpStatus.CONFLICT, "409001", "Conflict");
        verify(servletRequest).setAttribute(HeaderEnum.EXCEPTION.getValue(), exception);
        verify(logger).exception("APPFAULTEXCEPTION", "duplicate", exception);
        verifyNoMoreInteractions(servletRequest, logger);
    }

    @Test
    void should_ReturnMaskedFileDetailsAndLogException_When_IoFaultContainsMetadata() {
        // given
        IOFaultMetadata metadata = IOFaultMetadata.builder()
                .fileName("supersecret.txt")
                .operation(IOOperation.READ)
                .mimeType(MediaType.TEXT_PLAIN)
                .charset("UTF-8")
                .directory(false)
                .sizeBytes(10L)
                .lastModifiedEpochMillis(100L)
                .offset(2L)
                .length(8L)
                .build();
        IOFaultException exception = new IOFaultException("I/O failure", metadata);

        // when
        ResponseEntity<ResponseError> result =
                handler.handleIOFaultException(exception, servletRequest);

        // then
        assertError(result, HttpStatus.BAD_REQUEST, ServiceCodeEnum.FILE_ERROR.getCode(), "I/O failure");
        ResponseError body = result.getBody();
        assertNotNull(body);
        assertNotNull(body.getDetails());
        List<FileErrorDetail> details = body.getDetails().getFileDetails();
        assertNotNull(details);
        assertEquals(1, details.size());
        FileErrorDetail detail = details.getFirst();
        assertEquals("sup*****ret.txt", detail.getFileName());
        assertEquals(IOOperation.READ.toString(), detail.getOperation());
        assertEquals(MediaType.TEXT_PLAIN.toString(), detail.getMimeType());
        assertEquals("UTF-8", detail.getCharset());
        assertEquals(10L, detail.getSizeBytes());
        assertEquals(100L, detail.getLastModifiedEpochMillis());
        assertEquals(2L, detail.getOffset());
        assertEquals(8L, detail.getLength());
        verify(servletRequest).setAttribute(HeaderEnum.EXCEPTION.getValue(), exception);
        verify(logger).exception("IOFAULTEXCEPTION", details, exception);
        verifyNoMoreInteractions(servletRequest, logger);
    }

    @Test
    void should_ReturnDefaultMessageWithoutDetails_When_IoFaultMetadataAndMessageAreNull() {
        // given
        IOFaultException exception = new IOFaultException(null, (Throwable) null);

        // when
        ResponseEntity<ResponseError> result =
                handler.handleIOFaultException(exception, servletRequest);

        // then
        assertError(
                result,
                HttpStatus.BAD_REQUEST,
                ServiceCodeEnum.FILE_ERROR.getCode(),
                ServiceCodeEnum.FILE_ERROR.getMessage()
        );
        ResponseError body = result.getBody();
        assertNotNull(body);
        assertNotNull(body.getDetails());
        assertNull(body.getDetails().getFileDetails());
        verify(servletRequest).setAttribute(HeaderEnum.EXCEPTION.getValue(), exception);
        verify(logger).exception("IOFAULTEXCEPTION", List.of(), exception);
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
