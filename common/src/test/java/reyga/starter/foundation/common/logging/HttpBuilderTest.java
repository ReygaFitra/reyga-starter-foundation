package reyga.starter.foundation.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpBuilderTest {

    @Test
    void getHttpMethod_readsRequestMapping() throws Exception {
        Method method = Dummy.class.getDeclaredMethod("handle");
        assertEquals("POST", HttpBuilder.getHttpMethod(method));
    }

    @Test
    void getClientAddress_prefersForwardedFor() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("x-forwarded-for")).thenReturn("1.1.1.1,2.2.2.2");
        when(request.getRemoteAddr()).thenReturn("3.3.3.3");

        assertEquals("1.1.1.1", HttpBuilder.getClientAddress(request));
    }

    @Test
    void getClientAddress_usesRemoteAddrWhenNoForwardedFor() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("x-forwarded-for")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("3.3.3.3");

        assertEquals("3.3.3.3", HttpBuilder.getClientAddress(request));
    }

    @Test
    void extractRequestHeader_lowercasesKeys() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaderNames()).thenReturn(new Enumeration<>() {
            private final java.util.Iterator<String> it = Map.of("X-Test", "1").keySet().iterator();
            @Override public boolean hasMoreElements() { return it.hasNext(); }
            @Override public String nextElement() { return it.next(); }
        });
        when(request.getHeader("X-Test")).thenReturn("1");

        Map<String, Object> map = HttpBuilder.extractRequestHeader(request);

        assertEquals("1", map.get("x-test"));
    }

    @Test
    void getCurrentHttpServletRequest_returnsSetRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertSame(request, HttpBuilder.getCurrentHttpServletRequest());
        RequestContextHolder.resetRequestAttributes();
    }

    private static class Dummy {
        @RequestMapping(method = RequestMethod.POST)
        public void handle() {
        }
    }
}
