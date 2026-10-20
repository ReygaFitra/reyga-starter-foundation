package reyga.starter.foundation.common_http.client;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Objects;

import okhttp3.Request;
import okhttp3.Response;
import reyga.starter.foundation.common_http.enums.HttpContentType;
import reyga.starter.foundation.common_http.enums.HttpHeader;
import reyga.starter.foundation.common_http.enums.HttpMethod;

/**
 * Prepared request and optional decoding contract. Reusable with buffered bodies;
 * response ownership always belongs to the caller. Builders are not thread-safe.
 * @param <T> request payload type
 * @param <R> response payload type
 */
public class HttpOperations<T, R> {
    private final Request request;
    private final Type responseType;
    private final HttpBodyCodec codec;
    private final String accept;
    private final Duration callTimeout;

    /**
     * Creates an empty compatibility instance; execution requires a builder-created instance.
     */
    public HttpOperations() {
        this.request = null;
        this.responseType = null;
        this.codec = null;
        this.accept = null;
        this.callTimeout = null;
    }

    /**
     * Creates a prepared operation.
     * @param request non-null request; custom streaming bodies retain their own replay restrictions
     * @param responseType optional target type
     * @param codec non-null serialization strategy
     * @param accept fallback response media type
     * @param callTimeout optional per-call timeout; zero disables it
     */
    public HttpOperations(Request request, Type responseType, HttpBodyCodec codec,
                          String accept, Duration callTimeout) {
        this.request = Objects.requireNonNull(request, "request must not be null");
        this.responseType = responseType;
        this.codec = Objects.requireNonNull(codec, "codec must not be null");
        this.accept = Objects.requireNonNull(accept, "accept must not be null");
        if (callTimeout != null && (callTimeout.isNegative()
                || callTimeout.compareTo(Duration.ofMillis(Integer.MAX_VALUE)) > 0
                || (!callTimeout.isZero() && callTimeout.toMillis() == 0))) {
            throw new IllegalArgumentException("callTimeout must be zero or between 1ms and 2147483647ms");
        }
        this.callTimeout = callTimeout;
    }

    /**
     * Returns the prepared request without executing it.
     * @return prepared request
     * @throws IllegalStateException for an empty compatibility instance
     */
    public Request request() {
        if (request == null) {
            throw new IllegalStateException("Create HttpOperations using StarterHttpClient.operationBuilder()");
        }
        return request;
    }

    /**
     * Returns the overall timeout override; zero disables the timeout.
     * @return optional call timeout override, or null to use the client timeout
     */
    public Duration callTimeout() {
        return callTimeout;
    }

    /**
     * Reads the body once without closing the response or interpreting its status.
     * @param response response owned by caller
     * @return decoded payload; empty bodies return null
     * @throws IOException for malformed or unsupported content
     * @throws IllegalStateException if no response type was configured
     */
    @SuppressWarnings("unchecked")
    public R readResponse(Response response) throws IOException {
        Objects.requireNonNull(response, "response must not be null");
        if (responseType == null) {
            throw new IllegalStateException("Configure response-type or responseType before decoding");
        }
        byte[] bytes = response.body().bytes();
        if (bytes.length == 0) {
            return null;
        }
        return (R) codec.decode(bytes, response.header("Content-Type", accept), responseType);
    }

    /**
     * Builds a request by applying explicit values over registered YAML defaults.
     * Without an id, baseUrl is required and only bodyless requests are supported
     * by the default builder; GET and JSON are defaults. Body requests require a
     * registered id and a per-request payload. Required-body methods reject null payloads.
     * @param <T> request payload type
     * @param <R> response payload type
     */
    public interface Builder<T, R> {
        /**
         * Selects registered defaults; explicit overrides apply regardless of setter order.
         * @param id registered API id; unknown ids fail at build
         * @return this builder
         */
        Builder<T, R> id(String id);
        /**
         * Overrides the registered base URL.
         * @param baseUrl absolute HTTP(S) URL
         * @return this builder
         */
        Builder<T, R> baseUrl(String baseUrl);
        /**
         * Appends a relative path to the base URL path.
         * @param path relative path with optional {name} placeholders
         * @return this builder
         */
        Builder<T, R> path(String path);
        /**
         * Overrides the HTTP method.
         * @param method HTTP method, normalized to uppercase
         * @return this builder
         */
        Builder<T, R> method(String method);

        /**
         * Selects a common HTTP method while preserving the String extension point.
         * @param method non-null method
         * @return this builder
         * @throws NullPointerException if method is null
         */
        default Builder<T, R> method(HttpMethod method) {
            return method(Objects.requireNonNull(method, "method must not be null").name());
        }
        /**
         * Replaces all values for a header, comparing names without case.
         * @param name header name
         * @param value value replacing configured values
         * @return this builder
         */
        Builder<T, R> header(String name, String value);

        /**
         * Replaces all values for a common header.
         * @param name non-null header name
         * @param value non-null header value
         * @return this builder
         * @throws NullPointerException if name or value is null
         */
        default Builder<T, R> header(HttpHeader name, String value) {
            return header(Objects.requireNonNull(name, "header must not be null").value(), value);
        }
        /**
         * Appends another header value.
         * @param name header name
         * @param value additional value
         * @return this builder
         */
        Builder<T, R> addHeader(String name, String value);

        /**
         * Appends a value for a common header.
         * @param name non-null header name
         * @param value non-null header value
         * @return this builder
         * @throws NullPointerException if name or value is null
         */
        default Builder<T, R> addHeader(HttpHeader name, String value) {
            return addHeader(Objects.requireNonNull(name, "header must not be null").value(), value);
        }
        /**
         * Replaces all values for a query parameter.
         * @param name query parameter
         * @param value value replacing configured values
         * @return this builder
         */
        Builder<T, R> queryParameter(String name, String value);
        /**
         * Appends another query parameter value.
         * @param name query parameter
         * @param value additional value
         * @return this builder
         */
        Builder<T, R> addQueryParameter(String name, String value);
        /**
         * Supplies one encoded path placeholder value.
         * @param name placeholder name
         * @param value value encoded as a path segment
         * @return this builder
         */
        Builder<T, R> pathVariable(String name, String value);
        /**
         * Selects the request representation; structured payloads use UTF-8.
         * @param contentType media type including optional charset; the default builder also resolves HttpContentType names
         * @return this builder
         */
        Builder<T, R> contentType(String contentType);

        /**
         * Selects a common request media type without charset/boundary parameters.
         * @param contentType non-null media type
         * @return this builder
         * @throws NullPointerException if contentType is null
         */
        default Builder<T, R> contentType(HttpContentType contentType) {
            return contentType(Objects.requireNonNull(contentType, "contentType must not be null").value());
        }
        /**
         * Selects the expected response representation.
         * @param accept expected response media type; the default builder also resolves HttpContentType names
         * @return this builder
         */
        Builder<T, R> accept(String accept);

        /**
         * Selects a common expected response media type.
         * @param accept non-null media type
         * @return this builder
         * @throws NullPointerException if accept is null
         */
        default Builder<T, R> accept(HttpContentType accept) {
            return accept(Objects.requireNonNull(accept, "accept must not be null").value());
        }
        /**
         * Supplies the per-request payload; serialization occurs during build.
         * The default builder requires a registered id when a payload is supplied.
         * @param body structured payload, raw String/bytes, or OkHttp RequestBody; null clears the current payload
         * @return this builder
         */
        Builder<T, R> requestBody(T body);
        /**
         * Selects the optional response decoding target.
         * @param responseType target class or parameterized type; null disables decoding
         * @return this builder
         */
        Builder<T, R> responseType(Type responseType);
        /**
         * Selects or returns the overall timeout override; zero disables the timeout.
         * @param timeout nonnegative timeout; zero disables it
         * @return this builder
         */
        Builder<T, R> callTimeout(Duration timeout);
        /**
         * Validates and serializes a request snapshot without sending it.
         * @return prepared operation
         * @throws IllegalArgumentException for invalid configuration or payload (preserves serialization cause)
         */
        HttpOperations<T, R> build();
    }
}
