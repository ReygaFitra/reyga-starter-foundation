package reyga.starter.foundation.common.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter @Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseError extends BaseResponse {

    private ResponseErrorDetail details;

    public ResponseError() {
    }

    public ResponseError(String status, String code, String message) {
        super(status, code, message);
    }

    public ResponseError(ResponseErrorDetail details) {
        this.details = details;
    }

    public ResponseError(String status, String code, String message, ResponseErrorDetail details) {
        super(status, code, message);
        this.details = details;
    }

}
