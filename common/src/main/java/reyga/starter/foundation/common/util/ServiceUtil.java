package reyga.starter.foundation.common.util;

import org.springframework.http.HttpStatus;
import reyga.starter.foundation.common.exception.AppFaultContent;

public class ServiceUtil {

    public static AppFaultContent buildAppFaultContent(
            String message, String errorCode, String errorMessage,
            Object faultInfo, HttpStatus statusCode
    ) {
        return AppFaultContent.builder()
                .message(message)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .faultInfo(faultInfo)
                .statusCode(statusCode)
                .build();
    }

}
