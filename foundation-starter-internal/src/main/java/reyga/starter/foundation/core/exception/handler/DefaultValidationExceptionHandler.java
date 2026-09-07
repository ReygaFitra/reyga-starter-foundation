package reyga.starter.foundation.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reyga.starter.foundation.common.enumeration.HeaderEnum;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.logging.InjectLogger;
import reyga.starter.foundation.common.model.dto.response.FieldErrorDetail;
import reyga.starter.foundation.common.model.dto.response.ResponseError;
import reyga.starter.foundation.common.util.DateUtility;
import reyga.starter.foundation.core.controller.ResponseErrorBuilder;

import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice
public class DefaultValidationExceptionHandler {

    @InjectLogger
    private CommonLogger logger;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseError> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        request.setAttribute(HeaderEnum.EXCEPTION.getValue(), ex);

        List<FieldErrorDetail> fieldErrorDetails = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> FieldErrorDetail.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .timestamp(DateUtility.getTimestamp(LocalDateTime.now()))
                        .build())
                .toList();

        if (logger != null) {
            logger.exception("MethodArgumentNotValidException".toUpperCase(), fieldErrorDetails, ex);
        }

        return ResponseErrorBuilder.createErrorResponse(
                HttpStatus.BAD_REQUEST, ServiceCodeEnum.VALIDATION_ERROR.getCode(), ServiceCodeEnum.VALIDATION_ERROR.getMessage(),
                null, null, fieldErrorDetails
        );
    }

}
