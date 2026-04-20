package reyga.starter.foundation.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.logging.InjectLogger;
import reyga.starter.foundation.common.model.dto.response.ResponseError;
import reyga.starter.foundation.core.controller.ResponseErrorBuilder;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ControllerAdvice
public class DefaultExceptionHandler extends BaseExceptionHandler<ResponseError> {

    @InjectLogger
    protected CommonLogger logger;

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
                logger.warn("ILLEGAL ARGUMENT EXCEPTION ERROR :", mapper.writeValueAsString(errors));
            } catch (JacksonException e1) {
                logger.error(JACKSON_WARN, e1.getMessage());
            }
        } else {
            code = "99";
            exceptionType = "Global Error";
            httpStatus = INTERNAL_SERVER_ERROR;
            message = "INTERNAL SERVER ERROR";
            errors.put("Global Error", exception.getStackTrace()[0].toString());
            try {
                logger.warn("GLOBAL ERROR :", mapper.writeValueAsString(errors));
            } catch (JacksonException e1) {
                logger.error(JACKSON_WARN, e1.getMessage());
            }
        }
        logger.exception(exceptionType.toUpperCase(), null, exception);
        return ResponseErrorBuilder.createErrorResponse(httpStatus, code, message);
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
                logger.warn("JPA ERROR :", mapper.writeValueAsString(errors));
            } catch (JacksonException e1) {
                logger.error(JACKSON_WARN, e1.getMessage());
            }
        }
        if (exception instanceof DataAccessException) {
            exceptionType = "DataAccessException";
            errors.put("JDBC-ERROR", exception.getStackTrace()[0].toString());
            try {
                logger.warn("JDBC ERROR :", mapper.writeValueAsString(errors));
            } catch (JacksonException e1) {
                logger.error(JACKSON_WARN, e1.getMessage());
            }
        }
        logger.exception(exceptionType.toUpperCase(), null, exception);
        return ResponseErrorBuilder.createErrorResponse(INTERNAL_SERVER_ERROR, "99", "DATABASE ERROR");
    }

}
