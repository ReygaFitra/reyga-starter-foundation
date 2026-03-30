package reyga.starter.foundation.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
import reyga.starter.foundation.common.enumeration.ServiceStatusResponseEnum;
import reyga.starter.foundation.common.model.dto.response.FieldErrorDetail;
import reyga.starter.foundation.common.model.dto.response.ResponseError;
import reyga.starter.foundation.core.controller.ResponseErrorTemplate;
import reyga.starter.foundation.core.exception.AppFaultException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ControllerAdvice
public class DefaultExceptionHandler extends BaseExceptionHandler<ResponseError> {

   private static final String JACKSON_WARN = "write_log_error";

    @Override
    protected ResponseEntity<ResponseError> processGlobalErrorHandler(Exception exception, HttpServletRequest servletRequest) {
        String code;
        String exceptionType;
        HttpStatus httpStatus;
        String message;
        ObjectMapper mapper = new ObjectMapper();
        final Map<String, Object> errors = new HashMap<>();
        if (exception instanceof IllegalArgumentException) {
            code = "91";
            exceptionType = "IllegalArgumentException";
            httpStatus = HttpStatus.BAD_REQUEST;
            message = "GENERAL ERROR";
            errors.put("illegalArgumentException", exception.getStackTrace()[0].toString());
            try {
                log.warn("ILLEGAL ARGUMENT EXCEPTION ERROR :", mapper.writeValueAsString(errors));
            } catch (JacksonException e1) {
                log.error(JACKSON_WARN, e1.getMessage());
            }
        } else {
            code = "99";
            exceptionType = "Global Error";
            httpStatus = INTERNAL_SERVER_ERROR;
            message = "INTERNAL SERVER ERROR";
            errors.put("Global Error", exception.getStackTrace()[0].toString());
            try {
                log.warn("GLOBAL ERROR :", mapper.writeValueAsString(errors));
            } catch (JacksonException e1) {
                log.error(JACKSON_WARN, e1.getMessage());
            }
        }
        log.exception(exceptionType.toUpperCase(), null, exception);
        return ResponseErrorTemplate.createErrorResponse(httpStatus, code, message);
    }

    @Override
    protected ResponseEntity<ResponseError> processDatabaseErrorHandler(Exception exception, HttpServletRequest servletRequest) {
        String exceptionType = "";
        ObjectMapper mapper = new ObjectMapper();
        final Map<String, Object> errors = new HashMap<>();
        if (exception instanceof JpaSystemException) {
            exceptionType = "JpaSystemException";
            errors.put("JPA-SYSTEM-ERROR", exception.getStackTrace()[0].toString());
            try {
                log.warn("JPA ERROR :", mapper.writeValueAsString(errors));
            } catch (JacksonException e1) {
                log.error(JACKSON_WARN, e1.getMessage());
            }
        }
        if (exception instanceof DataAccessException) {
            exceptionType = "DataAccessException";
            errors.put("JDBC-ERROR", exception.getStackTrace()[0].toString());
            try {
                log.warn("JDBC ERROR :", mapper.writeValueAsString(errors));
            } catch (JacksonException e1) {
                log.error(JACKSON_WARN, e1.getMessage());
            }
        }
        log.exception(exceptionType.toUpperCase(), null, exception);
        return ResponseErrorTemplate.createErrorResponse(INTERNAL_SERVER_ERROR, "99", "DATABASE ERROR");
    }

    @Override
    protected ResponseEntity<ResponseError> processAppFaultErrorHandler(AppFaultException appFaultException, HttpServletRequest servletRequest) {
        log.exception("AppFaultException".toUpperCase(), appFaultException.getFaultInfo(),appFaultException);
        return ResponseErrorTemplate.createErrorResponse(
                appFaultException.getStatusCode(), appFaultException.getErrorCode(), appFaultException.getErrorMessage()
        );
    }

    @Override
    protected ResponseEntity<ResponseError> processMethodArgumentNotValidException(MethodArgumentNotValidException methodArgumentNotValidException, HttpServletRequest servletRequest, List<FieldErrorDetail> fieldErrorDetails) {
        log.exception("MethodArgumentNotValidException".toUpperCase(), fieldErrorDetails, methodArgumentNotValidException);
        return ResponseErrorTemplate.createErrorResponse(
                HttpStatus.BAD_REQUEST, ServiceCodeEnum.VALIDATION_ERROR.getCode(), ServiceCodeEnum.VALIDATION_ERROR.getMessage(),
                null, null, fieldErrorDetails
        );
    }
}
