package reyga.starter.foundation.core.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import reyga.starter.foundation.common.model.dto.request.BaseRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class BaseHttpServletBuilderTest {

    private final TestHttpServletBuilder builder = new TestHttpServletBuilder();

    @Test
    void should_SetAndReturnServletParameters_When_ParametersAreProvided() {
        // Given
        DummyRequest request = new DummyRequest();
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);

        // When
        builder.set(request, servletRequest, servletResponse);

        // Then
        assertSame(servletRequest, builder.requestFrom(request));
        assertSame(servletResponse, builder.responseFrom(request));
        assertSame(servletRequest, request.getServletRequest());
        assertSame(servletResponse, request.getServletResponse());
    }

    @Test
    void should_ReturnNullServletParameters_When_ParametersAreUnavailable() {
        // Given
        DummyRequest request = new DummyRequest();

        // When
        HttpServletRequest servletRequest = builder.requestFrom(request);
        HttpServletResponse servletResponse = builder.responseFrom(request);

        // Then
        assertNull(servletRequest);
        assertNull(servletResponse);
        assertNull(request.getServletRequest());
        assertNull(request.getServletResponse());
    }

    @Test
    void should_ThrowNullPointerException_When_RequestIsNull() {
        // Given
        DummyRequest request = null;

        // When
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> builder.requestFrom(request)
        );

        // Then
        assertNotNull(exception);
    }

    private static final class DummyRequest extends BaseRequest {
    }

    private static final class TestHttpServletBuilder extends BaseHttpServletBuilder {
        void set(DummyRequest request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
            setHttpServletParameter(request, servletRequest, servletResponse);
        }

        HttpServletRequest requestFrom(DummyRequest request) {
            return getServletRequest(request);
        }

        HttpServletResponse responseFrom(DummyRequest request) {
            return getServletResponse(request);
        }
    }
}
