package reyga.starter.foundation.common.exception.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.hibernate.JDBCException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reyga.starter.foundation.common.enumeration.HeaderEnum;
import reyga.starter.foundation.common.exception.AppFaultException;
import reyga.starter.foundation.common.logging.CustomLogger;
import reyga.starter.foundation.common.model.dto.response.ResponseError;
import reyga.starter.foundation.common.model.dto.response.ResponseTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final CustomLogger logger;

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ResponseError> handleGlobalErrorException(Exception e, HttpServletRequest request) {
        request.setAttribute(HeaderEnum.EXCEPTION.getValue(), e);
        String code;
        String exceptionType;
        HttpStatus httpStatus;
        String message;
        ObjectMapper mapper = new ObjectMapper();
        final Map<String, Object> errors = new HashMap<>();
        if (e instanceof IllegalArgumentException) {
            code = "91";
            exceptionType = "IllegalArgumentException";
            httpStatus = HttpStatus.BAD_REQUEST;
            message = "GENERAL ERROR";
            errors.put("illegalArgumentException", e.getStackTrace()[0].toString());
            try {
                this.logger.warn("ILLEGAL ARGUMENT EXCEPTION ERROR :", mapper.writeValueAsString(errors));
            } catch (JsonProcessingException e1) {
                this.logger.error("write_log_error", e1.getMessage());
            }
        } else {
            code = "99";
            exceptionType = "Global Error";
            httpStatus = INTERNAL_SERVER_ERROR;
            message = "INTERNAL SERVER ERROR";
            errors.put("Global Error", e.getStackTrace()[0].toString());
            try {
                this.logger.warn("GLOBAL ERROR :", mapper.writeValueAsString(errors));
            } catch (JsonProcessingException e1) {
                this.logger.error("write_log_error", e1.getMessage());
            }
        }
        this.logger.exception(exceptionType.toUpperCase(), null,e);
        return ResponseTemplate.createErrorResponse(httpStatus, code, message);
    }

    @ExceptionHandler({JpaSystemException.class, JDBCException.class})
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ResponseEntity<ResponseError> handleDatabaseErrorException(Exception e, HttpServletRequest request) {
        request.setAttribute(HeaderEnum.EXCEPTION.getValue(), e);
        String exceptionType = "";
        ObjectMapper mapper = new ObjectMapper();
        final Map<String, Object> errors = new HashMap<>();
        if (e instanceof JpaSystemException) {
            exceptionType = "JpaSystemException";
            errors.put("JPA-SYSTEM-ERROR", e.getStackTrace()[0].toString());
            try {
                this.logger.warn("JPA ERROR :", mapper.writeValueAsString(errors));
            } catch (JsonProcessingException e1) {
                this.logger.error("write_log_error", e1.getMessage());
            }
        }
        if (e instanceof JDBCException) {
            exceptionType = "JDBCException";
            errors.put("JDBC-ERROR", e.getStackTrace()[0].toString());
            try {
                this.logger.warn("JDBC ERROR :", mapper.writeValueAsString(errors));
            } catch (JsonProcessingException e1) {
                this.logger.error("write_log_error", e1.getMessage());
            }
        }
        this.logger.exception(exceptionType.toUpperCase(), null,e);
        return ResponseTemplate.createErrorResponse(INTERNAL_SERVER_ERROR, "99", "DATABASE ERROR");
    }

    @ExceptionHandler(AppFaultException.class)
    public ResponseEntity<ResponseError> handleAppFaultException(AppFaultException appFaultException, HttpServletRequest request) {
        request.setAttribute(HeaderEnum.EXCEPTION.getValue(), appFaultException);
        this.logger.exception("AppFaultException".toUpperCase(), appFaultException.getFaultInfo(),appFaultException);
        return ResponseTemplate.createErrorResponse(
                appFaultException.getStatusCode(), appFaultException.getErrorCode(), appFaultException.getErrorMessage()
        );
    }

}
