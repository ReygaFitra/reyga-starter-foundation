package reyga.starter.foundation.common.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HeaderEnum {
    ACCESS_TOKEN("x-access-token"),
    USERNAME("x-user-name"),
    REQUEST_ID("x-request-id"),
    METHOD("x-request-method"),
    STATUS_CODE("x-status-code"),
    REQUEST_ENDPOINT("x-request-uri"),
    FORWARDED_FOR("x-forwarded-for"),
    PACKAGE_INFO("x-package-name"),
    EXCEPTION("x-request-exception"),
    REQUEST("x-request"),
    RESPONSE("x-response"),
    USER_AGENT("user-agent"),
    RESPONSE_TIME("x-response-time");

    private final String value;
}
