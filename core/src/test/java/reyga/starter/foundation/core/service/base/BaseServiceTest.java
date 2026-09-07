package reyga.starter.foundation.core.service.base;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.model.dto.request.BaseRequest;
import reyga.starter.foundation.core.exception.AppFaultException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseServiceTest {

    @Mock private HttpServletRequest servletRequest;
    @Mock private HttpServletResponse servletResponse;
    @Mock private CommonLogger logger;

    private TestableService service;

    @BeforeEach
    void setUp() {
        service = spy(new TestableService());
        service.setLogger(logger);
    }

    @Test
    void should_ReturnOrchestratedResult_When_ServletParametersAreNotRequired() {
        // Given
        DummyRequest request = new DummyRequest();
        service.setUseHttpParams(false);

        // When
        String result = service.execute(request);

        // Then
        assertEquals("orchestrated", result);
        InOrder executionOrder = inOrder(service);
        executionOrder.verify(service).useHttpServletParameter();
        executionOrder.verify(service).logInformation(request);
        executionOrder.verify(service).validateRequest(request);
        executionOrder.verify(service).buildProcess(request);
        verify(logger).info("Executing Service...");
        verify(logger).info("Request : ", String.valueOf(request));
        verifyNoMoreInteractions(logger);
    }

    @Test
    void should_ReturnOrchestratedResult_When_RequiredServletParametersAreProvided() {
        // Given
        DummyRequest request = new DummyRequest();
        request.setServletRequest(servletRequest);
        request.setServletResponse(servletResponse);
        service.setUseHttpParams(true);

        // When
        String result = service.execute(request);

        // Then
        assertEquals("orchestrated", result);
        verify(service).validateRequest(request);
        verify(service).buildProcess(request);
        assertSame(servletRequest, request.getServletRequest());
        assertSame(servletResponse, request.getServletResponse());
    }

    @Test
    void should_ThrowAppFaultException_When_ServletRequestIsMissing() {
        // Given
        DummyRequest request = new DummyRequest();
        request.setServletResponse(servletResponse);
        service.setUseHttpParams(true);

        // When
        AppFaultException exception = assertThrows(AppFaultException.class, () -> service.execute(request));

        // Then
        assertEquals(ServiceCodeEnum.SERVLET_CONTEXT_NOT_FOUND.getCode(), exception.getErrorCode());
        assertEquals(ServiceCodeEnum.SERVLET_CONTEXT_NOT_FOUND.getMessage(), exception.getErrorMessage());
        assertNull(exception.getFaultInfo());
        verify(service, never()).logInformation(any());
        verify(service, never()).validateRequest(any());
        verify(service, never()).buildProcess(any());
        verifyNoInteractions(logger);
    }

    @Test
    void should_ThrowAppFaultException_When_ServletResponseIsMissing() {
        // Given
        DummyRequest request = new DummyRequest();
        request.setServletRequest(servletRequest);
        service.setUseHttpParams(true);

        // When
        AppFaultException exception = assertThrows(AppFaultException.class, () -> service.execute(request));

        // Then
        assertEquals(ServiceCodeEnum.SERVLET_CONTEXT_NOT_FOUND.getCode(), exception.getErrorCode());
        verify(service, never()).logInformation(any());
        verify(service, never()).validateRequest(any());
        verify(service, never()).buildProcess(any());
        verifyNoInteractions(logger);
    }

    @Test
    void should_PropagateValidationException_When_RequestIsInvalid() {
        // Given
        DummyRequest request = new DummyRequest();
        IllegalArgumentException validationFailure = new IllegalArgumentException("invalid request");
        service.setUseHttpParams(false);
        doThrow(validationFailure).when(service).validateRequest(request);

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.execute(request));

        // Then
        assertSame(validationFailure, exception);
        verify(service).logInformation(request);
        verify(service).validateRequest(request);
        verify(service, never()).buildProcess(any());
    }

    @Test
    void should_ExecuteWithoutLogging_When_LoggerIsUnavailable() {
        // Given
        DummyRequest request = new DummyRequest();
        service.setLogger(null);
        service.setUseHttpParams(false);

        // When
        String result = service.execute(request);

        // Then
        assertEquals("orchestrated", result);
        verify(service).logInformation(request);
        verify(service).validateRequest(request);
        verify(service).buildProcess(request);
        verifyNoInteractions(logger);
    }

    private static class DummyRequest extends BaseRequest {
    }

    private static class TestableService extends BaseService<DummyRequest, String> {
        private boolean useHttpParams;

        void setUseHttpParams(boolean useHttpParams) {
            this.useHttpParams = useHttpParams;
        }

        void setLogger(CommonLogger logger) {
            this.logger = logger;
        }

        @Override
        protected String buildProcess(DummyRequest request) {
            return "orchestrated";
        }

        @Override
        protected void validateRequest(DummyRequest request) {
        }

        @Override
        protected boolean useHttpServletParameter() {
            return useHttpParams;
        }
    }
}
