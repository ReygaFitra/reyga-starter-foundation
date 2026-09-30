package reyga.starter.foundation.common.logging;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;
import reyga.starter.foundation.common.model.dto.request.RequestLogging;

/**
 * Builds sanitized HTTP request context for foundation logging.
 *
 * <p>Sensitive header values are replaced before they enter the returned map or
 * SLF4J MDC. Request bodies and parameters are not sanitized by this class and must
 * be reviewed by the caller before logging.</p>
 */
public class HttpHeaderBuilder {

    private static final String REDACTED_VALUE = "[REDACTED]";
    private static final Set<String> SENSITIVE_HEADER_NAMES = Set.of(
            "authorization",
            "proxy-authorization",
            "cookie",
            "set-cookie",
            "x-api-key",
            "x-starter-access-token"
    );

    /**
     * Creates an HTTP logging context helper.
     */
    public HttpHeaderBuilder() {
    }

    /**
     * Extracts request body and multipart values from annotated method arguments.
     *
     * @param args invocation arguments
     * @param method invoked controller method
     * @param requestDto target logging DTO
     */
    public static void constructRequestBodyAndRequestMultiPart(Object[] args, Method method, RequestLogging requestDto) {
        Map<String, Object> requestMultiPart = new HashMap<>();

        for(int argIndex = 0; argIndex < args.length; ++argIndex) {
            for(Annotation annotation : method.getParameterAnnotations()[argIndex]) {
                if (annotation instanceof RequestBody) {
                    requestDto.setRequestBody(args[argIndex]);
                } else if (annotation instanceof RequestPart requestParam) {
                    if (args[argIndex] instanceof MultipartFile multipartFile) {
                        requestMultiPart.put(requestParam.value(), multipartFile.getOriginalFilename());
                    } else {
                        requestMultiPart.put(requestParam.value(), args[argIndex]);
                    }
                }
            }
        }

        if (!CollectionUtils.isEmpty(requestMultiPart)) {
            requestDto.setRequestMultiPart(requestMultiPart);
        }

    }

    /**
     * Extracts query parameters and URI template variables from a request.
     *
     * @param request current request; may be {@code null}
     * @param requestDto target logging DTO
     */
    public static void extractRequestParam(HttpServletRequest request, RequestLogging requestDto) {
        if (request != null) {
            Iterator<String> paramsItr = request.getParameterNames().asIterator();
            Map<String, Object> map = new HashMap<>();

            while(paramsItr.hasNext()) {
                String paramName = paramsItr.next();
                map.put(paramName, request.getParameter(paramName));
            }

            if (!CollectionUtils.isEmpty(map)) {
                requestDto.setRequestParams(map);
            }

            Object pathVariableAttribute = request.getAttribute(
                    HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE
            );
            if (pathVariableAttribute instanceof Map<?, ?> pathVariables
                    && !pathVariables.isEmpty()) {
                Map<String, Object> stringKeyedPathVariables = new HashMap<>();
                pathVariables.forEach((key, value) -> {
                    if (key instanceof String stringKey) {
                        stringKeyedPathVariables.put(stringKey, value);
                    }
                });
                requestDto.setRequestPathVariable(stringKeyedPathVariables);
            }
        }

    }

    /**
     * Removes trailing URI segments that correspond to logged path variables.
     *
     * @param requestDto request logging context; may be {@code null}
     * @param request current HTTP request
     * @return normalized request URL
     */
    public static String getUrl(RequestLogging requestDto, HttpServletRequest request) {
        String url = HttpBuilder.getUriPath(request);
        if (requestDto != null && requestDto.getRequestPathVariable() != null) {
            for(int i = 0; i < requestDto.getRequestPathVariable().size(); ++i) {
                url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
                url = url.substring(0, url.lastIndexOf("/"));
            }
        }

        return url;
    }

    /**
     * Returns request headers and adds them to MDC while masking sensitive values.
     *
     * <p>Authorization, proxy authorization, cookie, API key, and foundation access
     * token headers are replaced with {@code [REDACTED]}. Header matching is
     * case-insensitive.</p>
     *
     * @param request current HTTP request
     * @return mutable header map containing only sanitized values
     */
    public static Map<String, String> buildHeadersMap(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();

        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames == null) {
            return map;
        }

        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            boolean sensitive = isSensitiveHeader(key);
            String safeValue = sensitive ? REDACTED_VALUE : value;
            if (sensitive || MDC.get(key) == null || MDC.get(key).isEmpty()) {
                if (safeValue != null) {
                    MDC.put(key, safeValue);
                }
            }
            map.put(key, safeValue);
        }

        return map;
    }

    private static boolean isSensitiveHeader(String headerName) {
        return headerName != null
                && SENSITIVE_HEADER_NAMES.contains(headerName.toLowerCase(Locale.ROOT));
    }
}
