package reyga.starter.foundation.common.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HeaderEnum {
    ACCESS_TOKEN("x-starter-access-token"),
    USERNAME("x-starter-user-name"),
    REQUEST_ID("x-starter-request-id"),
    METHOD("x-starter-request-method"),
    STATUS_CODE("x-starter-status-code"),
    REQUEST_ENDPOINT("x-starter-request-uri"),
    FORWARDED_FOR("x-starter-forwarded-for"),
    PACKAGE_INFO("x-starter-package-name"),
    EXCEPTION("x-starter-request-exception"),
    REQUEST("x-starter-request"),
    RESPONSE("x-starter-response"),
    USER_AGENT("x-starter-user-agent"),
    RESPONSE_TIME("x-starter-response-time"),
    SUMMARY_LOG("x-starter-summary-log");

    private final String value;
}
