package reyga.starter.foundation.core.dto.response;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import reyga.starter.foundation.common.enumeration.HeaderEnum;
import reyga.starter.foundation.common.enumeration.ServiceStatusResponseEnum;
import reyga.starter.foundation.common.logging.BaseLogging;
import reyga.starter.foundation.common.util.DateUtil;
import reyga.starter.foundation.common.util.MapperUtil;

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
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request;

        if (requestAttributes instanceof ServletRequestAttributes) {
            request = ((ServletRequestAttributes) requestAttributes).getRequest();
            if (data instanceof ResponseData<?>) {
                request.setAttribute(HeaderEnum.RESPONSE.getValue(), data.toString());
            } else {
              request.setAttribute(HeaderEnum.RESPONSE.getValue(), MapperUtil.convertDtoToJsonString(data, true));
            }
            MDC.put(HeaderEnum.RESPONSE.getValue(), data.toString());
        }
    }

}
