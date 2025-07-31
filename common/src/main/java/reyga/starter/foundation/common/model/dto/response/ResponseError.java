package reyga.starter.foundation.common.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import reyga.starter.foundation.core.dto.response.BaseResponse;

@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseError extends BaseResponse {
    @JsonProperty("errors")
    private ResponseErrorDetail details;

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

    private ResponseError(Builder builder) {
        setStatus(builder.status);
        setCode(builder.code);
        setMessage(builder.message);
        setDetails(builder.details);
    }

    public ResponseErrorDetail getDetails() {
        return details;
    }

    public void setDetails(ResponseErrorDetail details) {
        this.details = details;
    }

    public static final class Builder {
        private String status;
        private String code;
        private String message;
        private ResponseErrorDetail details;

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder status(String val) {
            status = val;
            return this;
        }

        public Builder code(String val) {
            code = val;
            return this;
        }

        public Builder message(String val) {
            message = val;
            return this;
        }

        public Builder details(ResponseErrorDetail val) {
            details = val;
            return this;
        }

        public ResponseError build() {
            return new ResponseError(this);
        }
    }
}
