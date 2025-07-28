package reyga.starter.foundation.common.model.dto.response;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reyga.starter.foundation.common.enumeration.ServiceStatusResponseEnum;
import reyga.starter.foundation.common.util.DateUtil;

import java.time.LocalDateTime;

@NoArgsConstructor
public class ResponseTemplate {

    public static <T> ResponseEntity<T> createResponse(T data, HttpStatus status) {
        return new ResponseEntity<>(data, status);
    }

    public static <T> ResponseEntity<ResponseData<T>> createResponse(
            T data, HttpStatus status, String code, String message
    ) {
        ResponseData<T> responseData = new ResponseData<>();
        responseData.setStatus(ServiceStatusResponseEnum.SUCCESS.getValue());
        responseData.setCode(code);
        responseData.setMessage(message);
        responseData.setData(data);
        return new ResponseEntity<>(responseData, status);
    }

    public static ResponseEntity<ResponseError> createErrorResponse(
            HttpStatus status, String code, String message
    ) {
       ResponseError error = ResponseError.Builder.newBuilder()
               .status(ServiceStatusResponseEnum.FAILED.getValue())
               .code(code)
               .message(message)
               .build();;
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
        return new ResponseEntity<>(error, status);
    }

}
