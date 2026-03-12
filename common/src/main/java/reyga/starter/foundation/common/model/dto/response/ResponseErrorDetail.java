package reyga.starter.foundation.common.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ResponseErrorDetail {
    private String business;
    private String additionalInfo;
    private Timestamp timestamp;

    private ResponseErrorDetail(Builder builder) {
        setBusiness(builder.business);
        setAdditionalInfo(builder.additionalInfo);
        setTimestamp(builder.timestamp);
    }

    public static final class Builder {
        private String business;
        private String additionalInfo;
        private Timestamp timestamp;

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder business(String val) {
            business = val;
            return this;
        }

        public Builder additionalInfo(String val) {
            additionalInfo = val;
            return this;
        }

        public Builder timestamp(Timestamp val) {
            timestamp = val;
            return this;
        }

        public ResponseErrorDetail build() {
            return new ResponseErrorDetail(this);
        }
    }
}
