package reyga.starter.foundation.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;
import reyga.starter.foundation.common.model.dto.request.RequestLogging;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpHeaderBuilderTest {

    @Test
    void constructRequestBodyAndRequestMultiPart_setsBodyAndMultipart() throws Exception {
        Method method = Dummy.class.getDeclaredMethod("handle", String.class, MultipartFile.class);
        RequestLogging logging = new RequestLogging();
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("file.txt");

        HttpHeaderBuilder.constructRequestBodyAndRequestMultiPart(
                new Object[]{"body", file}, method, logging
        );

        assertEquals("body", logging.getRequestBody());
        assertNotNull(logging.getRequestMultiPart());
        assertEquals("file.txt", logging.getRequestMultiPart().get("file"));
    }

    @Test
    void extractRequestParam_setsParamsAndPathVariables() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterNames()).thenReturn(new Enumeration<>() {
            private final java.util.Iterator<String> it = Map.of("a", "1").keySet().iterator();
            @Override public boolean hasMoreElements() { return it.hasNext(); }
            @Override public String nextElement() { return it.next(); }
        });
        when(request.getParameter("a")).thenReturn("1");
        Map<String, Object> pathVars = Map.of("id", "10");
        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathVars);

        RequestLogging logging = new RequestLogging();
        HttpHeaderBuilder.extractRequestParam(request, logging);

        assertEquals("1", logging.getRequestParams().get("a"));
        assertEquals("10", logging.getRequestPathVariable().get("id"));
    }

    @Test
    void getUrl_trimsPathVariables() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(80);
        when(request.getRequestURI()).thenReturn("/api/items/1");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/items/1"));

        RequestLogging logging = new RequestLogging();
        logging.setRequestPathVariable(new HashMap<>(Map.of("id", "1")));

        String url = HttpHeaderBuilder.getUrl(logging, request);
        assertEquals("/api/items", url);
    }

    @Test
    void buildHeadersMap_populatesMapAndMdc() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaderNames()).thenReturn(new Enumeration<>() {
            private final java.util.Iterator<String> it = Map.of("x-test", "1").keySet().iterator();
            @Override public boolean hasMoreElements() { return it.hasNext(); }
            @Override public String nextElement() { return it.next(); }
        });
        when(request.getHeader("x-test")).thenReturn("1");

        Map<String, String> map = HttpHeaderBuilder.buildHeadersMap(request);
        assertEquals("1", map.get("x-test"));
    }

    private static class Dummy {
        public void handle(@RequestBody String body, @RequestPart("file") MultipartFile file) {
        }
    }
}
