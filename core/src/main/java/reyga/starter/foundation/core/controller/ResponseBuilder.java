package reyga.starter.foundation.core.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import reyga.starter.foundation.common.enumeration.HeaderEnum;
import reyga.starter.foundation.common.logging.BaseLogging;
import reyga.starter.foundation.common.util.DateUtil;
import reyga.starter.foundation.common.util.MapperUtil;
import reyga.starter.foundation.core.dto.response.ResponseData;
import reyga.starter.foundation.core.dto.response.ResponseError;
import reyga.starter.foundation.core.dto.response.ResponseErrorDetail;

import java.time.LocalDateTime;

@NoArgsConstructor
public class ResponseBuilder extends BaseLogging {

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

    protected static <T> ResponseData<T> buildResponseData(T data, String status, String code, String message) {
        return ResponseData.<T>builder()
                .status(status)
                .code(code)
                .message(message)
                .data(data)
                .build();
    }

    protected static ResponseError buildResponseError(String status, String code, String message) {
        return ResponseError.Builder.newBuilder()
                .status(status)
                .code(code)
                .message(message)
                .build();
    }

    protected static ResponseError buildResponseError(String status, String code, String message, String business, String additionalInfo) {
        return ResponseError.Builder.newBuilder()
                .status(status)
                .code(code)
                .message(message)
                .details(ResponseErrorDetail.Builder.newBuilder()
                        .business(business)
                        .additionalInfo(additionalInfo)
                        .timestamp(DateUtil.getTimestamp(LocalDateTime.now()))
                        .build())
                .build();
    }
}
