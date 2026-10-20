package reyga.starter.foundation.common_http.enums;

/** Common header names for typed builders; arbitrary header names remain supported as Strings. */
public enum HttpHeader {
    /** Representation accepted by the caller. */
    ACCEPT("Accept"),
    /** Request representation. */
    CONTENT_TYPE("Content-Type"),
    /** Credentials for the target origin. */
    AUTHORIZATION("Authorization"),
    /** Credentials for an HTTP proxy. */
    PROXY_AUTHORIZATION("Proxy-Authorization"),
    /** HTTP cache directives. */
    CACHE_CONTROL("Cache-Control"),
    /** Conditional request entity tag. */
    IF_NONE_MATCH("If-None-Match"),
    /** Conditional modification timestamp. */
    IF_MODIFIED_SINCE("If-Modified-Since"),
    /** Caller software identifier. */
    USER_AGENT("User-Agent"),
    /** Request cookies. */
    COOKIE("Cookie"),
    /** Application request correlation identifier. */
    X_REQUEST_ID("X-Request-Id");

    private final String value;

    HttpHeader(String value) {
        this.value = value;
    }

    /**
     * Returns the standard wire name for use in request headers.
     * @return immutable header name
     */
    public String value() {
        return value;
    }
}
