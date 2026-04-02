package reyga.starter.foundation.common.enumeration;

import lombok.Getter;

@Getter
public enum ServiceCodeEnum {
    SAMPLE1("00", "Service Process Successfully"),
    SAMPLE2("01", "Service Process Failure"),
    RATE_LIMIT_EXCEEDED("400429", "Service Process Limit Exceeded"),
    VALIDATION_ERROR("400441", "Invalid Request"),
    FILE_ERROR("400442", "File Error"),
    GLOBAL_ERROR("500599", "Internal Server Error"),
    DATABASE_ERROR("500598", "Database Error"),
    DATABASE_QUERY_FAULT("500597", "Query Database Error"),
    DATABASE_CONSTRAINT_FAULT("500596", "Database Constraint Error");

    private final String code;
    private final String message;

    ServiceCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
