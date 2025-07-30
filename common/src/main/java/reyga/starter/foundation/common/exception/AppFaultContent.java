package reyga.starter.foundation.common.exception;

import lombok.*;
import org.springframework.http.HttpStatus;
import reyga.starter.foundation.common.model.dto.content.BaseContent;

import java.io.Serializable;

@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor @AllArgsConstructor
public class AppFaultContent extends BaseContent {
    private String message;
    private String errorCode;
    private String errorMessage;
    private Object faultInfo;
    private HttpStatus statusCode;
}
