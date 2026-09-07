package reyga.starter.foundation.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpBuilderTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void should_ReturnHttpMethod_When_MappedMethodIsProvided() throws Exception {
        // Given
        Method composedMapping = Controller.class.getDeclaredMethod("composedMapping");

        // When
        String composedMappingResult = HttpBuilder.getHttpMethod(composedMapping);

        // Then
        assertEquals("POST", composedMappingResult);
    }

    @Test
    void should_ThrowNoSuchElementException_When_MethodHasNoHttpMethodMapping() throws Exception {
        // Given
        Method unmapped = Controller.class.getDeclaredMethod("unmapped");
        Method directMapping = Controller.class.getDeclaredMethod("requestMapping");
        Method mappingWithoutMethod = Controller.class.getDeclaredMethod("mappingWithoutMethod");

        // When
        NoSuchElementException unmappedException = assertThrows(NoSuchElementException.class,
                () -> HttpBuilder.getHttpMethod(unmapped));
        NoSuchElementException directMappingException = assertThrows(NoSuchElementException.class,
                () -> HttpBuilder.getHttpMethod(directMapping));
        NoSuchElementException emptyMethodException = assertThrows(NoSuchElementException.class,
                () -> HttpBuilder.getHttpMethod(mappingWithoutMethod));

        // Then
        assertNotNull(unmappedException);
        assertNotNull(directMappingException);
        assertNotNull(emptyMethodException);
    }

    @Test
    void should_ReturnUriPath_When_RequestIsProvided() {
        // Given
        HttpServletRequest request = requestFor("/api/items/1");

        // When
        String result = HttpBuilder.getUriPath(request);

        // Then
        assertEquals("/api/items/1", result);
        verify(request).getScheme();
        verify(request).getServerName();
        verify(request).getServerPort();
        verify(request).getRequestURI();
        verify(request).getQueryString();
    }

    @Test
    void should_ReturnNullUriAndAddress_When_RequestIsNull() {
        // Given
        HttpServletRequest request = null;

        // When
        String uri = HttpBuilder.getUriPath(request);
        String address = HttpBuilder.getClientAddress(request);

        // Then
        assertNull(uri);
        assertNull(address);
    }

    @Test
    void should_ReturnFirstForwardedAddress_When_ForwardedHeaderContainsMultipleAddresses() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("x-forwarded-for")).thenReturn("1.1.1.1,2.2.2.2");

        // When
        String result = HttpBuilder.getClientAddress(request);

        // Then
        assertEquals("1.1.1.1", result);
        verify(request).getHeader("x-forwarded-for");
        verify(request, never()).getRemoteAddr();
        verifyNoMoreInteractions(request);
    }

    @Test
    void should_ReturnRemoteAddress_When_ForwardedHeaderIsUnavailable() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("x-forwarded-for")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("3.3.3.3");

        // When
        String result = HttpBuilder.getClientAddress(request);

        // Then
        assertEquals("3.3.3.3", result);
        verify(request).getHeader("x-forwarded-for");
        verify(request).getRemoteAddr();
        verifyNoMoreInteractions(request);
    }

    @Test
    void should_ReturnLowercaseHeaderMap_When_RequestHasHeaders() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Map.of("X-Test", "one", "accept", "json").keySet()));
        when(request.getHeader("X-Test")).thenReturn("one");
        when(request.getHeader("accept")).thenReturn("json");

        // When
        Map<String, Object> result = HttpBuilder.extractRequestHeader(request);

        // Then
        assertEquals(Map.of("x-test", "one", "accept", "json"), result);
        verify(request).getHeaderNames();
        verify(request).getHeader("X-Test");
        verify(request).getHeader("accept");
        verifyNoMoreInteractions(request);
    }

    @Test
    void should_ReturnCurrentRequest_When_RequestAttributesAreBound() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // When
        HttpServletRequest result = HttpBuilder.getCurrentHttpServletRequest();

        // Then
        assertSame(request, result);
    }

    @Test
    void should_ReturnNullRequest_When_RequestAttributesAreNotBound() {
        // Given
        RequestContextHolder.resetRequestAttributes();

        // When
        HttpServletRequest result = HttpBuilder.getCurrentHttpServletRequest();

        // Then
        assertNull(result);
    }

    @Test
    void should_ReturnCallerMethodName_When_ServiceMethodNameIsRequested() {
        // Given
        String expected = "should_ReturnCallerMethodName_When_ServiceMethodNameIsRequested";

        // When
        String result = HttpBuilder.getCurrentServiceMethodName();

        // Then
        assertEquals(expected, result);
    }

    private HttpServletRequest requestFor(String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(80);
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost" + uri));
        return request;
    }

    private static class Controller {
        @RequestMapping(method = RequestMethod.PUT)
        void requestMapping() {}
        @PostMapping
        void composedMapping() {}
        @RequestMapping
        void mappingWithoutMethod() {}
        void unmapped() {}
    }
}
