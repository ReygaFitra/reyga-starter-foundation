package reyga.starter.foundation.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import reyga.starter.foundation.common.model.dto.response.FieldErrorDetail;

import java.util.List;
import java.util.Objects;

/**
 * Validation failure enriched with response-detail metadata.
 *
 * <p>{@link AppFaultContent} is the single source of HTTP status, code, message, and
 * diagnostic information. The optional detail fields are consumed by the default
 * validation exception handler to create a {@code ResponseError} with request-field
 * details. This exception deliberately does not inherit {@link AppFaultException};
 * validation failures have their own exception-handling contract.</p>
 */
@Getter
public class ValidationFaultException extends RuntimeException {
    public ValidationFaultException(String errorCode, String errorMessage, Object faultInfo, HttpStatus statusCode) {
        this(buildFaultContent(errorCode, errorMessage, faultInfo, statusCode), null, null, null);
    }

    public ValidationFaultException(String errorCode, String errorMessage, Object faultInfo, Throwable cause, HttpStatus statusCode) {
        this(buildFaultContent(errorCode, errorMessage, faultInfo, statusCode), cause, null, null, null);
    }

    public ValidationFaultException(AppFaultContent faultContent) {
        this(faultContent, null, null, null);
    }

    public ValidationFaultException(AppFaultContent faultContent, Throwable cause) {
        this(faultContent, cause, null, null, null);
    }

    /**
     * Creates a validation failure with response-detail metadata.
     *
     * @param errorCode application error code
     * @param errorMessage application error message
     * @param faultInfo diagnostic information for logs
     * @param statusCode response HTTP status
     * @param business business context shown in the response details
     * @param additionalInfo additional response-detail information
     * @param fieldErrorList immutable snapshot of invalid request fields, or null when unavailable
     */
    public ValidationFaultException(
            String errorCode, String errorMessage, Object faultInfo, HttpStatus statusCode,
            String business, String additionalInfo, List<FieldErrorDetail> fieldErrorList
    ) {
        this(buildFaultContent(errorCode, errorMessage, faultInfo, statusCode), business, additionalInfo, fieldErrorList);
    }

    /**
     * Creates a validation failure with a cause and response-detail metadata.
     *
     * @param errorCode application error code
     * @param errorMessage application error message
     * @param faultInfo diagnostic information for logs
     * @param cause failure cause
     * @param statusCode response HTTP status
     * @param business business context shown in the response details
     * @param additionalInfo additional response-detail information
     * @param fieldErrorList immutable snapshot of invalid request fields, or null when unavailable
     */
    public ValidationFaultException(
            String errorCode, String errorMessage, Object faultInfo, Throwable cause, HttpStatus statusCode,
            String business, String additionalInfo, List<FieldErrorDetail> fieldErrorList
    ) {
        this(buildFaultContent(errorCode, errorMessage, faultInfo, statusCode), cause, business, additionalInfo, fieldErrorList);
    }

    /**
     * Creates a validation failure from fault content with response-detail metadata.
     *
     * @param faultContent HTTP fault values used by the validation response
     * @param business business context shown in the response details
     * @param additionalInfo additional response-detail information
     * @param fieldErrorList immutable snapshot of invalid request fields, or null when unavailable
     */
    public ValidationFaultException(
            AppFaultContent faultContent, String business, String additionalInfo, List<FieldErrorDetail> fieldErrorList
    ) {
        super(Objects.requireNonNull(faultContent, "faultContent must not be null").getErrorMessage());
        this.faultContent = faultContent;
        this.business = business;
        this.additionalInfo = additionalInfo;
        this.fieldErrorList = copyFieldErrorList(fieldErrorList);
    }

    /**
     * Creates a validation failure from fault content and a cause with response-detail metadata.
     *
     * @param faultContent HTTP fault values used by the validation response
     * @param cause failure cause
     * @param business business context shown in the response details
     * @param additionalInfo additional response-detail information
     * @param fieldErrorList immutable snapshot of invalid request fields, or null when unavailable
     */
    public ValidationFaultException(
            AppFaultContent faultContent, Throwable cause, String business, String additionalInfo,
            List<FieldErrorDetail> fieldErrorList
    ) {
        super(Objects.requireNonNull(faultContent, "faultContent must not be null").getErrorMessage(), cause);
        this.faultContent = faultContent;
        this.business = business;
        this.additionalInfo = additionalInfo;
        this.fieldErrorList = copyFieldErrorList(fieldErrorList);
    }

    private static List<FieldErrorDetail> copyFieldErrorList(List<FieldErrorDetail> fieldErrorList) {
        return fieldErrorList == null ? null : List.copyOf(fieldErrorList);
    }

    private static AppFaultContent buildFaultContent(
            String errorCode, String errorMessage, Object faultInfo, HttpStatus statusCode
    ) {
        return AppFaultContent.buildAppFaultContent(
                "Validation Exception", errorCode, errorMessage, faultInfo, statusCode
        );
    }

    private final AppFaultContent faultContent;
    private final String business;
    private final String additionalInfo;
    private final List<FieldErrorDetail> fieldErrorList;
}
