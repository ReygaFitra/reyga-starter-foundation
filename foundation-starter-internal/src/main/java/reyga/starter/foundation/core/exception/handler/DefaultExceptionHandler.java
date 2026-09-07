package reyga.starter.foundation.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
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
   private static final String ILLEGAL_ARGUMENT_EXCEPTION = "IllegalArgumentException";
   private static final String GENERAL_ERROR = "GENERAL ERROR";
   private static final String GLOBAL_ERROR = "GLOBAL ERROR";

    @Override
    protected ResponseEntity<ResponseError> processGlobalErrorHandler(Exception exception, HttpServletRequest servletRequest) {
        FaultContent faultContent;
        ObjectMapper mapper = new ObjectMapper();
        final Map<String, Object> errors = new HashMap<>();

        if (exception instanceof IllegalArgumentException) {
            faultContent = new FaultContent(
                    ServiceCodeEnum.GLOBAL_ERROR.getCode(),
                    ILLEGAL_ARGUMENT_EXCEPTION,
                    GENERAL_ERROR,
                    INTERNAL_SERVER_ERROR
            );
            errors.put("illegalArgumentException", exception.getStackTrace()[0].toString());

            try {
                logger.warn("ILLEGAL ARGUMENT EXCEPTION ERROR :", mapper.writeValueAsString(errors));
            } catch (JacksonException e1) {
                logger.error(JACKSON_WARN, e1.getMessage());
            }

        } else {
            faultContent = new FaultContent(
                    ServiceCodeEnum.GLOBAL_ERROR.getCode(),
                    GLOBAL_ERROR,
                    ServiceCodeEnum.GLOBAL_ERROR.getMessage(),
                    INTERNAL_SERVER_ERROR
            );
            errors.put("Global Error", exception.getStackTrace()[0].toString());
            try {
                logger.warn("GLOBAL ERROR :", mapper.writeValueAsString(errors));
            } catch (JacksonException e1) {
                logger.error(JACKSON_WARN, e1.getMessage());
            }

        }

        logger.exception(faultContent.exceptionType().toUpperCase(), null, exception);
        return ResponseErrorBuilder.createErrorResponse(faultContent.httpStatus(), faultContent.code(), faultContent.message());
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
        return ResponseErrorBuilder.createErrorResponse(INTERNAL_SERVER_ERROR, ServiceCodeEnum.DATABASE_ERROR.getCode(), ServiceCodeEnum.DATABASE_ERROR.getMessage());
    }

    protected record FaultContent(String code, String exceptionType, String message, HttpStatus httpStatus) {}

}
