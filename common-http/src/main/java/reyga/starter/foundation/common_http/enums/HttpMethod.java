package reyga.starter.foundation.common_http.enums;

/** Common HTTP methods for typed builders; custom method tokens remain available through String overloads. */
public enum HttpMethod {
    /** Retrieves a representation without a request body. */
    GET,
    /** Retrieves response headers without a request body. */
    HEAD,
    /** Submits a payload; registered operations require a per-request body by default. */
    POST,
    /** Replaces a resource representation. */
    PUT,
    /** Applies the upstream-defined partial update format. */
    PATCH,
    /** Deletes a resource according to the upstream contract. */
    DELETE,
    /** Queries upstream communication options. */
    OPTIONS,
    /** Requests an upstream diagnostic loopback. */
    TRACE,
    /** Requests an upstream tunnel; transport support remains governed by OkHttp. */
    CONNECT,
    /** Updates WebDAV resource properties. */
    PROPPATCH,
    /** Requests a WebDAV report. */
    REPORT
}
