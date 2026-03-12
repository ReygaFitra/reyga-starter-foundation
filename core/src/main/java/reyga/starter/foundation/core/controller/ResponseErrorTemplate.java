package reyga.starter.foundation.core.controller;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reyga.starter.foundation.common.enumeration.ServiceStatusResponseEnum;
import reyga.starter.foundation.common.model.dto.response.ResponseError;

@NoArgsConstructor
public class ResponseErrorTemplate extends ResponseBuilder {

    public static ResponseEntity<ResponseError> createErrorResponse(
            HttpStatus status, String code, String message
    ) {
       ResponseError error = buildResponseError(ServiceStatusResponseEnum.FAILED.getValue(), code, message);
       return new ResponseEntity<>(error, status);
    }

    public static ResponseEntity<ResponseError> createErrorResponse(
            HttpStatus status, String code, String message, String business, String additionalInfo
    ) {
        ResponseError error = buildResponseError(ServiceStatusResponseEnum.FAILED.getValue(), code, message, business, additionalInfo);
        return new ResponseEntity<>(error, status);
    }

}
