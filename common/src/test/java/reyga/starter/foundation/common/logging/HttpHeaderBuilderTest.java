package reyga.starter.foundation.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;
import reyga.starter.foundation.common.model.dto.request.RequestLogging;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpHeaderBuilderTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void should_SetBodyAndMultipartValues_When_AnnotatedArgumentsAreProvided() throws Exception {
        // Given
        Method method = Controller.class.getDeclaredMethod("handle", String.class, MultipartFile.class, String.class);
        RequestLogging logging = new RequestLogging();
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("file.txt");

        // When
        HttpHeaderBuilder.constructRequestBodyAndRequestMultiPart(new Object[]{"body", file, "metadata"}, method, logging);

        // Then
        assertEquals("body", logging.getRequestBody());
        assertEquals(Map.of("file", "file.txt", "metadata", "metadata"), logging.getRequestMultiPart());
        verify(file).getOriginalFilename();
        verifyNoMoreInteractions(file);
    }

    @Test
    void should_LeaveBodyAndMultipartNull_When_ArgumentsHaveNoRelevantAnnotations() throws Exception {
        // Given
        Method method = Controller.class.getDeclaredMethod("plain", String.class);
        RequestLogging logging = new RequestLogging();

        // When
        HttpHeaderBuilder.constructRequestBodyAndRequestMultiPart(new Object[]{"value"}, method, logging);

        // Then
        assertNull(logging.getRequestBody());
        assertNull(logging.getRequestMultiPart());
    }

    @Test
    void should_SetParamsAndPathVariables_When_RequestContainsValues() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterNames()).thenReturn(Collections.enumeration(Map.of("query", "value").keySet()));
        when(request.getParameter("query")).thenReturn("value");
        Map<String, Object> paths = Map.of("id", "10");
        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(paths);
        RequestLogging logging = new RequestLogging();

        // When
        HttpHeaderBuilder.extractRequestParam(request, logging);

        // Then
        assertEquals(Map.of("query", "value"), logging.getRequestParams());
        assertSame(paths, logging.getRequestPathVariable());
        verify(request).getParameterNames();
        verify(request).getParameter("query");
        verify(request).getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        verifyNoMoreInteractions(request);
    }

    @Test
    void should_LeaveParamsAndPathVariablesNull_When_RequestIsNullOrEmpty() {
        // Given
        RequestLogging nullRequestLogging = new RequestLogging();
        RequestLogging emptyRequestLogging = new RequestLogging();
        HttpServletRequest emptyRequest = mock(HttpServletRequest.class);
        when(emptyRequest.getParameterNames()).thenReturn(Collections.emptyEnumeration());

        // When
        HttpHeaderBuilder.extractRequestParam(null, nullRequestLogging);
        HttpHeaderBuilder.extractRequestParam(emptyRequest, emptyRequestLogging);

        // Then
        assertNull(nullRequestLogging.getRequestParams());
        assertNull(nullRequestLogging.getRequestPathVariable());
        assertNull(emptyRequestLogging.getRequestParams());
        assertNull(emptyRequestLogging.getRequestPathVariable());
        verify(emptyRequest).getParameterNames();
        verify(emptyRequest).getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        verifyNoMoreInteractions(emptyRequest);
    }

    @Test
    void should_RemovePathVariableSegments_When_UrlContainsPathVariables() {
        // Given
        HttpServletRequest request = requestFor("/api/items/10/details/20/");
        RequestLogging logging = new RequestLogging();
        logging.setRequestPathVariable(new HashMap<>(Map.of("itemId", "10", "detailId", "20")));

        // When
        String result = HttpHeaderBuilder.getUrl(logging, request);

        // Then
        assertEquals("/api/items/10", result);
    }

    @Test
    void should_ReturnOriginalUrl_When_PathVariablesAreUnavailable() {
        // Given
        HttpServletRequest request = requestFor("/api/items");

        // When
        String nullDtoResult = HttpHeaderBuilder.getUrl(null, request);
        String emptyDtoResult = HttpHeaderBuilder.getUrl(new RequestLogging(), request);

        // Then
        assertEquals("/api/items", nullDtoResult);
        assertEquals("/api/items", emptyDtoResult);
    }

    @Test
    void should_ReturnHeaderMapAndPopulateMissingMdcValues_When_RequestHasHeaders() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Map.of("x-one", "1", "x-two", "2").keySet()));
        when(request.getHeader("x-one")).thenReturn("1");
        when(request.getHeader("x-two")).thenReturn("2");
        MDC.put("x-two", "existing");

        // When
        Map<String, String> result = HttpHeaderBuilder.buildHeadersMap(request);

        // Then
        assertEquals(Map.of("x-one", "1", "x-two", "2"), result);
        assertEquals("1", MDC.get("x-one"));
        assertEquals("existing", MDC.get("x-two"));
        verify(request).getHeaderNames();
        verify(request).getHeader("x-one");
        verify(request).getHeader("x-two");
        verifyNoMoreInteractions(request);
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
        void handle(@RequestBody String body, @RequestPart("file") MultipartFile file,
                    @RequestPart("metadata") String metadata) {}
        void plain(String value) {}
    }
}
