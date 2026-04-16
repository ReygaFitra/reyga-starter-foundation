package reyga.starter.foundation.common.model.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter @Getter
@SuperBuilder(toBuilder = true)
public class ResponseData<T> extends BaseResponse {
    private T data;

    public ResponseData() {
    }

    public ResponseData(String status, String code, String message) {
        super(status, code, message);
    }

    public ResponseData(T data) {
        this.data = data;
    }

    public ResponseData(String status, String code, String message, T data) {
        super(status, code, message);
        this.data = data;
    }

    @Override
    public String toString() {
        return "{" +
                "status='" + status + '\'' +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
