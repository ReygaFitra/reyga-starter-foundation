package reyga.starter.foundation.common_http.config;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import reyga.starter.foundation.common_http.enums.HttpContentType;

/**
 * HTTP defaults bound from {@code reyga.config.http-client}; lists and maps are immutable snapshots.
 * @param connectTimeout connection timeout, default 10 seconds
 * @param readTimeout read timeout, default 10 seconds
 * @param writeTimeout write timeout, default 10 seconds
 * @param callTimeout overall timeout, default zero (unlimited)
 * @param followRedirects follows redirects, default true
 * @param retryOnConnectionFailure OkHttp transport recovery, default true
 * @param registerApi API definitions, default empty
 */
@ConfigurationProperties("reyga.config.http-client")
public record HttpClientProperties(Duration connectTimeout, Duration readTimeout,
                                   Duration writeTimeout, Duration callTimeout,
                                   Boolean followRedirects, Boolean retryOnConnectionFailure,
                                   List<Api> registerApi) {
    /**
     * Applies defaults and rejects negative, submillisecond, or oversized timeouts.
     * @param connectTimeout optional connect timeout
     * @param readTimeout optional read timeout
     * @param writeTimeout optional write timeout
     * @param callTimeout optional call timeout
     * @param followRedirects optional redirect policy
     * @param retryOnConnectionFailure optional transport recovery policy
     * @param registerApi optional API list
     */
    public HttpClientProperties {
        connectTimeout = timeout(connectTimeout, Duration.ofSeconds(10));
        readTimeout = timeout(readTimeout, Duration.ofSeconds(10));
        writeTimeout = timeout(writeTimeout, Duration.ofSeconds(10));
        callTimeout = timeout(callTimeout, Duration.ZERO);
        followRedirects = followRedirects == null ? true : followRedirects;
        retryOnConnectionFailure = retryOnConnectionFailure == null ? true : retryOnConnectionFailure;
        registerApi = registerApi == null ? List.of() : List.copyOf(registerApi);
    }

    private static Duration timeout(Duration value, Duration fallback) {
        Duration result = value == null ? fallback : value;
        if (result.isNegative() || result.compareTo(Duration.ofMillis(Integer.MAX_VALUE)) > 0
                || (!result.isZero() && result.toMillis() == 0)) {
            throw new IllegalArgumentException("HTTP timeout must be zero or between 1ms and 2147483647ms");
        }
        return result;
    }

    /**
     * Registered endpoint. DTO type names describe models rather than singleton response beans.
     * @param id unique nonblank id
     * @param baseUrl absolute HTTP(S) URL
     * @param path relative path, default empty
     * @param method HTTP method, default GET
     * @param headers repeated header list
     * @param queryParameters repeated query parameter list
     * @param pathVariables default placeholder values
     * @param contentType request MIME type or {@link HttpContentType} name, default application/json
     * @param accept response MIME type or {@link HttpContentType} name, defaults to contentType
     * @param requestBodyRequired optional body requirement; defaults to true for POST, PUT, PATCH, PROPPATCH and REPORT
     * @param responseType optional fully qualified response class
     * @param callTimeout optional overall timeout override
     */
    public record Api(String id, String baseUrl, String path, String method,
                      List<NameValue> headers, List<NameValue> queryParameters,
                      Map<String, String> pathVariables, String contentType, String accept,
                      Boolean requestBodyRequired, String responseType, Duration callTimeout) {
        /**
         * Copies collections and applies optional defaults.
         * @param id API id
         * @param baseUrl endpoint URL
         * @param path endpoint path
         * @param method HTTP method
         * @param headers headers
         * @param queryParameters query parameters
         * @param pathVariables placeholder values
         * @param contentType request media type
         * @param accept response media type
         * @param requestBodyRequired optional per-request payload requirement; no body is stored in configuration
         * @param responseType target class name
         * @param callTimeout timeout override
         */
        public Api {
            path = path == null ? "" : path;
            method = method == null ? "GET" : method;
            headers = headers == null ? List.of() : List.copyOf(headers);
            queryParameters = queryParameters == null ? List.of() : List.copyOf(queryParameters);
            pathVariables = pathVariables == null ? Map.of() : Map.copyOf(pathVariables);
            contentType = contentType == null ? "application/json" : HttpContentType.resolve(contentType);
            accept = accept == null ? contentType : HttpContentType.resolve(accept);
            callTimeout = callTimeout == null ? null : timeout(callTimeout, Duration.ZERO);
        }
    }

    /**
     * One header or query parameter; repeated names are supported.
     * @param name header/parameter name
     * @param value header/parameter value
     */
    public record NameValue(String name, String value) {
    }
}
