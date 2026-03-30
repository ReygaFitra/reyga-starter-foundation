package reyga.starter.foundation.core.controller;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reyga.starter.foundation.common.enumeration.ServiceStatusResponseEnum;
import reyga.starter.foundation.common.model.dto.response.FieldErrorDetail;
import reyga.starter.foundation.common.model.dto.response.ResponseError;

import java.util.List;

@NoArgsConstructor
public class ResponseErrorTemplate extends ResponseBuilder {

    public static ResponseEntity<ResponseError> createErrorResponse(
            HttpStatus httpStatus, String code, String message
    ) {
       ResponseError error = buildResponseError(ServiceStatusResponseEnum.FAILED.getLabel(), code, message);
       return new ResponseEntity<>(error, httpStatus);
    }

    public static ResponseEntity<ResponseError> createErrorResponse(
            HttpStatus httpStatus, String code, String message, String business, String additionalInfo
    ) {
        ResponseError error = buildResponseError(ServiceStatusResponseEnum.FAILED.getLabel(), code, message, business, additionalInfo);
        return new ResponseEntity<>(error, httpStatus);
    }

    public static ResponseEntity<ResponseError> createErrorResponse(
            HttpStatus httpStatus, String code, String message, String business, String additionalInfo, List<FieldErrorDetail> fieldErrorList
    ) {
        ResponseError error = buildResponseError(ServiceStatusResponseEnum.FAILED.getLabel(), code, message, business, additionalInfo, fieldErrorList);
        return new ResponseEntity<>(error, httpStatus);
    }

}
