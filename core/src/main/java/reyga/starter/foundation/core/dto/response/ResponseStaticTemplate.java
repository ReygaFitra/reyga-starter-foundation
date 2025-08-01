package reyga.starter.foundation.core.dto.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reyga.starter.foundation.common.enumeration.HeaderEnum;
import reyga.starter.foundation.common.enumeration.ServiceStatusResponseEnum;
import reyga.starter.foundation.common.logging.BaseLogging;
import reyga.starter.foundation.common.util.DateUtil;

import java.time.LocalDateTime;

@NoArgsConstructor
public class ResponseStaticTemplate extends BaseLogging {

    public static ResponseEntity<ResponseError> createErrorResponse(
            HttpStatus status, String code, String message
    ) {
       ResponseError error = ResponseError.Builder.newBuilder()
               .status(ServiceStatusResponseEnum.FAILED.getValue())
               .code(code)
               .message(message)
               .build();
       setMDCResponse(error);
       return new ResponseEntity<>(error, status);
    }

    public static ResponseEntity<ResponseError> createErrorResponse(
            HttpStatus status, String code, String message, String business, String additionalInfo
    ) {
        ResponseError error = ResponseError.Builder.newBuilder()
                .status(ServiceStatusResponseEnum.FAILED.getValue())
                .code(code)
                .message(message)
                .details(ResponseErrorDetail.Builder.newBuilder()
                        .business(business)
                        .additionalInfo(additionalInfo)
                        .timestamp(DateUtil.getTimestamp(LocalDateTime.now()))
                        .build())
                .build();
        setMDCResponse(error);
        return new ResponseEntity<>(error, status);
    }


    protected static <T> void setMDCResponse(T data) {
        MDC.put(HeaderEnum.RESPONSE.getValue(), data.toString());
    }

}
