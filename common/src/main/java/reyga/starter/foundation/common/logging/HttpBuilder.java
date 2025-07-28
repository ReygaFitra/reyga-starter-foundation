package reyga.starter.foundation.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

@Component
@NoArgsConstructor
public class HttpBuilder {

    public static String getHttpMethod(Method method) {
        return ((RequestMethod) Arrays.stream(((RequestMapping)Arrays.stream((RequestMapping[])((Annotation)Arrays.stream(method.getDeclaredAnnotations()).filter((annotation) -> annotation.annotationType().isAnnotationPresent(RequestMapping.class)).findFirst().get()).annotationType().getAnnotationsByType(RequestMapping.class)).findFirst().get()).method()).findFirst().get()).name();
    }

    public static String getUriPath(HttpServletRequest request) {
        return request != null ? ServletUriComponentsBuilder.fromRequest(request).buildAndExpand(new Object[0]).getPath() : null;
    }

    public static String getClientAddress(HttpServletRequest request) {
        String remoteAddr = null;
        if (request != null) {
            remoteAddr = request.getHeader("x-forwarded-for");
            if (remoteAddr != null && !"".equals(remoteAddr)) {
                StringTokenizer tokenizer = new StringTokenizer(remoteAddr, ",");
                if (tokenizer.hasMoreTokens()) {
                    remoteAddr = tokenizer.nextToken();
                }
            } else {
                remoteAddr = request.getRemoteAddr();
            }
        }

        return remoteAddr;
    }

    public static Map<String, Object> extractRequestHeader(HttpServletRequest request) {
        Map<String, Object> map = new HashMap();
        Iterator<String> headerItr = request.getHeaderNames().asIterator();

        while(headerItr.hasNext()) {
            String headerName = (String)headerItr.next();
            map.put(headerName.toLowerCase(), request.getHeader(headerName));
        }

        return map;
    }

    public static String getCurrentServiceMethodName() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

}
