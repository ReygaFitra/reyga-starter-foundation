package reyga.starter.foundation.common.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Defines the standard HTTP header, servlet request attribute, and MDC keys used
 * by the foundation logging components.
 *
 * <p>Use getValue() when an application needs to read or propagate the
 * same key as the library.</p>
 */
@Getter
@AllArgsConstructor
public enum StarterHeaderEnum {

    /** Access token supplied by the caller. */
    ACCESS_TOKEN("x-starter-access-token"),

    /** Username associated with the request. */
    USERNAME("x-starter-user-name"),

    /** Correlation identifier for the request. */
    REQUEST_ID("x-starter-request-id"),

    /** HTTP method associated with the request. */
    METHOD("x-starter-request-method"),

    /** HTTP response status code. */
    STATUS_CODE("x-starter-status-code"),

    /** URI of the requested resource. */
    REQUEST_ENDPOINT("x-starter-request-uri"),

    /** Originating client or forwarded address. */
    FORWARDED_FOR("x-starter-forwarded-for"),

    /** Application package or execution context name. */
    PACKAGE_INFO("x-starter-package-name"),

    /** Exception captured while processing the request. */
    EXCEPTION("x-starter-request-exception"),

    /** Request payload captured for logging. */
    REQUEST("x-starter-request"),

    /** Response payload captured for logging. */
    RESPONSE("x-starter-response"),

    /** User-Agent value supplied by the caller. */
    USER_AGENT("x-starter-user-agent"),

    /** Total request processing duration. */
    RESPONSE_TIME("x-starter-response-time"),

    /** Internal MDC marker for routing a completed request to the summary log. */
    SUMMARY_LOG("x-starter-summary-log");

    private final String value;
}
