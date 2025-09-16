package reyga.starter.foundation.common.enumeration;

import lombok.Getter;

@Getter
public enum ServiceCodeEnum {
    SAMPLE1("00", "Service Process Successfully"), SAMPLE2("01", "Service Process Failure"),
    RATE_LIMIT_EXCEEDED("429", "Service Process Limit Exceeded"), GLOBAL_ERROR("99", "Internal Server Error"),
    DATABASE_ERROR("98", "System Error"), DATABASE_QUERY_FAULT("97", "System Error"), DATABASE_CONSTRAINT_FAULT("96", "System Error");

    private final String code;
    private final String message;

    ServiceCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
