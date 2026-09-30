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
import reyga.starter.foundation.common.model.dto.request.BaseRequest;
import reyga.starter.foundation.core.exception.AppFaultException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BaseServiceTest {

    @Mock private HttpServletRequest servletRequest;
    @Mock private HttpServletResponse servletResponse;

    private TestableService service;

    @BeforeEach
    void setUp() {
        service = spy(new TestableService());
    }

    @Test
    void should_ReturnOrchestratedResult_When_ServletParametersAreNotRequired() {
        // Given
        DummyRequest request = new DummyRequest();
        request.setName("customer-01");
        service.setUseHttpParams(false);

        // When
        String result = service.execute(request);

        // Then
        assertEquals("orchestrated", result);
        InOrder executionOrder = inOrder(service);
        executionOrder.verify(service).useHttpServletParameter();
        executionOrder.verify(service).validateRequest(request);
        executionOrder.verify(service).buildProcess(request);
    }

    @Test
    void should_ReturnOrchestratedResult_When_RequiredServletParametersAreProvided() {
        // Given
        DummyRequest request = new DummyRequest();
        request.setName("customer-01");
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
        verify(service, never()).validateRequest(any());
        verify(service, never()).buildProcess(any());
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
        verify(service, never()).validateRequest(any());
        verify(service, never()).buildProcess(any());
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
        verify(service).validateRequest(request);
        verify(service, never()).buildProcess(any());
    }

    private static class DummyRequest extends BaseRequest {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private static class TestableService extends BaseService<DummyRequest, String> {
        private boolean useHttpParams;

        void setUseHttpParams(boolean useHttpParams) {
            this.useHttpParams = useHttpParams;
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
