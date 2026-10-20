package reyga.starter.foundation.common_http.client;

import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import reyga.starter.foundation.common_http.config.HttpClientProperties;

/**
 * Immutable, startup-validated endpoint registry. Request payloads are supplied only by builders.
 */
public class HttpOperationRegistry {
    private final Map<String, HttpClientProperties.Api> apis;
    private final Map<String, Class<?>> responseTypes;
    private final HttpBodyCodec codec;

    /**
     * Validates registrations without sending requests or creating payloads.
     * @param properties bound configuration
     * @param codec body codec
     * @throws IllegalArgumentException for invalid or duplicate registrations
     */
    public HttpOperationRegistry(HttpClientProperties properties, HttpBodyCodec codec) {
        this.codec = java.util.Objects.requireNonNull(codec, "codec must not be null");
        Map<String, HttpClientProperties.Api> registrations = new LinkedHashMap<>();
        Map<String, Class<?>> types = new LinkedHashMap<>();
        for (HttpClientProperties.Api api : properties.registerApi()) {
            requireText(api.id(), "register-api.id");
            if (registrations.putIfAbsent(api.id(), api) != null) {
                throw new IllegalArgumentException("Duplicate HTTP register-api.id: " + api.id());
            }
            validate(api);
            if (api.responseType() != null) {
                requireText(api.responseType(), "response-type");
                try {
                    types.put(api.id(), ClassUtils.forName(api.responseType(), ClassUtils.getDefaultClassLoader()));
                } catch (ClassNotFoundException | LinkageError exception) {
                    throw new IllegalArgumentException("HTTP response-type must name a loadable class for " + api.id(), exception);
                }
            }
        }
        apis = Map.copyOf(registrations);
        responseTypes = Map.copyOf(types);
    }

    /**
     * Creates a builder with isolated mutable state and shared immutable defaults.
     * @param <T> request payload
     * @param <R> response payload
     * @return new isolated builder
     */
    public <T, R> HttpOperations.Builder<T, R> builder() {
        return new DefaultHttpOperationsBuilder<>(this, codec);
    }

    HttpClientProperties.Api api(String id) {
        HttpClientProperties.Api api = apis.get(id);
        if (api == null) {
            throw new IllegalArgumentException("Unknown HTTP API id: " + id);
        }
        return api;
    }

    Class<?> responseType(String id) {
        return responseTypes.get(id);
    }

    static void requireText(String value, String key) {
        if (!StringUtils.hasText(value) || value.contains("${")) {
            throw new IllegalArgumentException("HTTP " + key + " must be nonblank and resolved");
        }
    }

    static void validate(HttpClientProperties.Api api) {
        requireText(api.baseUrl(), "base-url");
        HttpUrl url = HttpUrl.parse(api.baseUrl());
        if (url == null || !url.username().isEmpty() || !url.password().isEmpty() || url.fragment() != null) {
            throw new IllegalArgumentException("HTTP base-url must be an absolute HTTP(S) URL without userinfo or fragment");
        }
        requireText(api.method(), "method");
        String method = api.method().toUpperCase(java.util.Locale.ROOT);
        if (!method.matches("[A-Z]+")) {
            throw new IllegalArgumentException("HTTP method must contain letters only");
        }
        if ((method.equals("GET") || method.equals("HEAD")) && Boolean.TRUE.equals(api.requestBodyRequired())) {
            throw new IllegalArgumentException("HTTP GET/HEAD cannot set request-body-required=true");
        }
        if (api.path().contains(":") || api.path().startsWith("//") || api.path().contains("?")
                || api.path().contains("#") || api.path().contains("\\") || api.path().contains("${")) {
            throw new IllegalArgumentException("HTTP path must be a relative path; use query-parameters for queries");
        }
        for (String segment : api.path().split("/")) {
            if (segment.equals(".") || segment.equals("..")) {
                throw new IllegalArgumentException("HTTP path cannot contain traversal segments");
            }
        }
        if (MediaType.parse(api.contentType()) == null || MediaType.parse(api.accept()) == null) {
            throw new IllegalArgumentException("HTTP content-type and accept must be valid media types");
        }
        okhttp3.Headers.Builder headers = new okhttp3.Headers.Builder();
        for (HttpClientProperties.NameValue header : api.headers()) {
            requireText(header.name(), "headers.name");
            if (header.value() == null || header.value().contains("${")) {
                throw new IllegalArgumentException("HTTP headers.value must be non-null and resolved");
            }
            headers.add(header.name(), header.value());
        }
        for (HttpClientProperties.NameValue parameter : api.queryParameters()) {
            requireText(parameter.name(), "query-parameters.name");
            if (parameter.value() == null || parameter.value().contains("${")) {
                throw new IllegalArgumentException("HTTP query-parameters.value must be non-null and resolved");
            }
        }
        for (Map.Entry<String, String> variable : api.pathVariables().entrySet()) {
            requireText(variable.getKey(), "path-variables name");
            requireText(variable.getValue(), "path-variables value");
            if (variable.getValue().equals(".") || variable.getValue().equals("..")) {
                throw new IllegalArgumentException("HTTP path-variables cannot contain traversal segments");
            }
        }
    }
}
