package reyga.starter.foundation.common_http.client;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import reyga.starter.foundation.common_http.enums.HttpContentType;
import reyga.starter.foundation.common_http.config.HttpClientProperties.Api;
import reyga.starter.foundation.common_http.config.HttpClientProperties.NameValue;

final class DefaultHttpOperationsBuilder<T, R> implements HttpOperations.Builder<T, R> {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([^{}]+)}");
    private final HttpOperationRegistry registry;
    private final HttpBodyCodec codec;
    private String id;
    private String baseUrl;
    private String path;
    private String method;
    private String contentType;
    private String accept;
    private Object body;
    private Type responseType;
    private boolean responseTypeSet;
    private Duration timeout;
    private final Map<String, String> variables = new LinkedHashMap<>();
    private final List<OverrideValue> headers = new ArrayList<>();
    private final List<OverrideValue> queries = new ArrayList<>();

    DefaultHttpOperationsBuilder(HttpOperationRegistry registry, HttpBodyCodec codec) {
        this.registry = registry;
        this.codec = codec;
    }

    @Override
    public HttpOperations.Builder<T, R> id(String value) {
        id = required(value, "id");
        return this;
    }

    @Override
    public HttpOperations.Builder<T, R> baseUrl(String value) {
        baseUrl = required(value, "base-url");
        return this;
    }

    @Override
    public HttpOperations.Builder<T, R> path(String value) {
        path = Objects.requireNonNull(value);
        return this;
    }

    @Override
    public HttpOperations.Builder<T, R> method(String value) {
        method = required(value, "method");
        return this;
    }

    @Override
    public HttpOperations.Builder<T, R> contentType(String value) {
        contentType = HttpContentType.resolve(required(value, "content-type"));
        return this;
    }

    @Override
    public HttpOperations.Builder<T, R> accept(String value) {
        accept = HttpContentType.resolve(required(value, "accept"));
        return this;
    }

    @Override
    public HttpOperations.Builder<T, R> header(String name, String value) {
        headers.add(new OverrideValue(name, value, false));
        return this;
    }

    @Override
    public HttpOperations.Builder<T, R> addHeader(String name, String value) {
        headers.add(new OverrideValue(name, value, true));
        return this;
    }

    @Override
    public HttpOperations.Builder<T, R> queryParameter(String name, String value) {
        queries.add(new OverrideValue(name, value, false));
        return this;
    }

    @Override
    public HttpOperations.Builder<T, R> addQueryParameter(String name, String value) {
        queries.add(new OverrideValue(name, value, true));
        return this;
    }

    @Override
    public HttpOperations.Builder<T, R> pathVariable(String name, String value) {
        variables.put(required(name, "path variable"), Objects.requireNonNull(value));
        return this;
    }

    @Override
    public HttpOperations.Builder<T, R> requestBody(T value) {
        body = value;
        return this;
    }

    @Override
    public HttpOperations.Builder<T, R> responseType(Type value) {
        responseType = value;
        responseTypeSet = true;
        return this;
    }

    @Override
    public HttpOperations.Builder<T, R> callTimeout(Duration value) {
        timeout = Objects.requireNonNull(value);
        return this;
    }


    @Override
    public HttpOperations<T, R> build() {
        Api defaults = id == null ? new Api(null, baseUrl, null, null, null, null, null, null, null, null, null, null)
                : registry.api(id);
        Api resolved = new Api(id, choose(baseUrl, defaults.baseUrl()), choose(path, defaults.path()),
                choose(method, defaults.method()), defaults.headers(), defaults.queryParameters(), defaults.pathVariables(),
                choose(contentType, defaults.contentType()), choose(accept,
                        contentType != null && defaults.accept().equals(defaults.contentType()) ? contentType : defaults.accept()),
                defaults.requestBodyRequired(), null, timeout == null ? defaults.callTimeout() : timeout);
        HttpOperationRegistry.validate(resolved);
        Headers builtHeaders = buildHeaders(resolved);
        HttpUrl url = buildUrl(resolved);
        String verb = resolved.method().toUpperCase(Locale.ROOT);
        boolean methodNeedsBody = List.of("POST", "PUT", "PATCH", "PROPPATCH", "REPORT").contains(verb);
        boolean bodyRequired = resolved.requestBodyRequired() == null ? methodNeedsBody : resolved.requestBodyRequired();
        if (body != null && id == null) {
            throw new IllegalArgumentException("HTTP requests with a body require id(...) and requestBody(...)");
        }
        if (bodyRequired && body == null) {
            throw new IllegalArgumentException("HTTP " + verb + " requires requestBody(...) for this operation; supply the per-request payload");
        }
        if (body != null && (verb.equals("GET") || verb.equals("HEAD"))) {
            throw new IllegalArgumentException("HTTP GET/HEAD cannot have a request body");
        }
        MediaType mediaType = MediaType.parse(Objects.requireNonNull(builtHeaders.get("Content-Type")));
        if (mediaType == null) {
            throw new IllegalArgumentException("Invalid HTTP Content-Type header");
        }
        RequestBody requestBody = encode(body, mediaType);
        if (requestBody == null && methodNeedsBody) {
            requestBody = RequestBody.create(new byte[0], mediaType);
        }
        Request request = new Request.Builder().url(url).headers(builtHeaders).method(verb, requestBody).build();
        Type target = responseTypeSet ? responseType : (id == null ? null : registry.responseType(id));
        return new HttpOperations<>(request, target, codec, builtHeaders.get("Accept"), resolved.callTimeout());
    }

    private Headers buildHeaders(Api resolved) {
        Headers.Builder requestHeaders = new Headers.Builder()
                .add("Content-Type", resolved.contentType()).add("Accept", resolved.accept());
        // Explicit media-type fields replace registered headers as well.
        for (NameValue value : resolved.headers()) {
            if (value.name().equalsIgnoreCase("Content-Type") || value.name().equalsIgnoreCase("Accept")) {
                requestHeaders.set(value.name(), value.value());
            } else {
                requestHeaders.add(value.name(), value.value());
            }
        }
        if (contentType != null) {
            requestHeaders.set("Content-Type", contentType);
        }
        if (accept != null) {
            requestHeaders.set("Accept", accept);
        }
        for (OverrideValue header : headers) {
            if (header.append()) {
                requestHeaders.add(header.name(), header.value());
            } else {
                requestHeaders.set(header.name(), header.value());
            }
        }
        return requestHeaders.build();
    }

    private HttpUrl buildUrl(Api resolved) {
        Map<String, String> pathValues = new LinkedHashMap<>(resolved.pathVariables());
        pathValues.putAll(variables);
        Matcher matcher = PLACEHOLDER.matcher(resolved.path());
        StringBuilder resolvedPath = new StringBuilder();
        while (matcher.find()) {
            String value = pathValues.get(matcher.group(1));
            if (value == null || value.isEmpty() || value.equals(".") || value.equals("..") || value.contains("${")) {
                throw new IllegalArgumentException("Missing or invalid HTTP path variable: " + matcher.group(1));
            }
            String encoded = new HttpUrl.Builder().scheme("https").host("placeholder.invalid")
                    .addPathSegment(value).build().encodedPath().substring(1);
            matcher.appendReplacement(resolvedPath, Matcher.quoteReplacement(encoded));
        }
        matcher.appendTail(resolvedPath);
        HttpUrl base = Objects.requireNonNull(HttpUrl.parse(resolved.baseUrl()));
        HttpUrl.Builder url = base.newBuilder();
        String suffix = resolvedPath.toString();
        if (!suffix.isEmpty()) {
            String prefix = base.encodedPath();
            url.encodedPath(prefix + (prefix.endsWith("/") ? "" : "/")
                    + (suffix.startsWith("/") ? suffix.substring(1) : suffix));
        }
        for (NameValue query : resolved.queryParameters()) {
            url.addQueryParameter(query.name(), query.value());
        }
        for (OverrideValue query : queries) {
            if (!query.append()) {
                url.removeAllQueryParameters(query.name());
            }
            url.addQueryParameter(query.name(), query.value());
        }
        return url.build();
    }

    private RequestBody encode(Object payload, MediaType mediaType) {
        if (payload == null) {
            return null;
        }
        if (payload instanceof RequestBody raw) {
            return raw;
        }
        if (payload instanceof String raw) {
            return RequestBody.create(raw, mediaType);
        }
        if (payload instanceof byte[] raw) {
            return RequestBody.create(raw.clone(), mediaType);
        }
        if (!StandardCharsets.UTF_8.equals(mediaType.charset(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("Structured HTTP payloads require UTF-8 charset");
        }
        try {
            return RequestBody.create(codec.encode(payload, mediaType.toString()), mediaType);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Cannot serialize HTTP request body", exception);
        }
    }

    private static String choose(String override, String fallback) {
        return override == null ? fallback : override;
    }

    private static String required(String value, String key) {
        HttpOperationRegistry.requireText(value, key);
        return value;
    }

    private record OverrideValue(String name, String value, boolean append) {
        private OverrideValue {
            required(name, "header/query name");
            Objects.requireNonNull(value, "header/query value must not be null");
            if (value.contains("${")) {
                throw new IllegalArgumentException("HTTP header/query value must be resolved");
            }
        }
    }
}
