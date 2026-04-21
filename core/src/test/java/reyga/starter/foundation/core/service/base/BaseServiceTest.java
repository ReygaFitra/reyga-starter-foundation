package reyga.starter.foundation.core.service.base;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reyga.starter.foundation.common.model.dto.request.BaseRequest;
import reyga.starter.foundation.core.exception.AppFaultException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseServiceTest {

    @Mock
    private HttpServletRequest servletRequest;
    @Mock
    private HttpServletResponse servletResponse;

    private TestableService service;

    @BeforeEach
    void setUp() {
        service = new TestableService();
    }

    @Test
    void execute_whenHttpParamsNotRequired_runsSuccessfully() {
        service.setUseHttpParams(false);
        DummyRequest request = new DummyRequest();

        String result = service.execute(request);

        assertEquals("orchestrated", result);
        assertTrue(service.wasOrchestrateCalled);
        assertTrue(service.wasValidateCalled);
    }

    @Test
    void execute_whenHttpParamsRequiredAndProvided_runsSuccessfully() {
        service.setUseHttpParams(true);
        DummyRequest request = new DummyRequest();
        request.setServletRequest(servletRequest);
        request.setServletResponse(servletResponse);

        String result = service.execute(request);

        assertEquals("orchestrated", result);
    }

    @Test
    void execute_whenHttpParamsRequiredButMissing_throwsAppFaultException() {
        service.setUseHttpParams(true);
        DummyRequest request = new DummyRequest(); // Missing servlet request/response

        assertThrows(AppFaultException.class, () -> service.execute(request));
        assertFalse(service.wasOrchestrateCalled);
    }

    @Test
    void execute_callsLogInformation() {
        // This test is simple, ensuring the method is called.
        // The logger itself is mocked via @InjectLogger, so we don't test its output here.
        TestableService spyService = spy(new TestableService());
        spyService.setUseHttpParams(false);
        
        spyService.execute(new DummyRequest());
        
        verify(spyService, times(1)).logInformation(any(DummyRequest.class));
    }


    // --- Helper classes for testing ---

    private static class DummyRequest extends BaseRequest {}

    private static class TestableService extends BaseService<DummyRequest, String> {
        private boolean useHttpParams = false;
        boolean wasOrchestrateCalled = false;
        boolean wasValidateCalled = false;

        void setUseHttpParams(boolean use) {
            this.useHttpParams = use;
        }

        @Override
        protected String orchestrate(DummyRequest request) {
            wasOrchestrateCalled = true;
            return "orchestrated";
        }

        @Override
        protected void validateRequest(DummyRequest request) {
            wasValidateCalled = true;
        }

        @Override
        protected boolean useHttpServletParameter() {
            return useHttpParams;
        }
    }
}
