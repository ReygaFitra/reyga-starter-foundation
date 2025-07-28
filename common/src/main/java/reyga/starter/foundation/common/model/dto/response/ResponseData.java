package reyga.starter.foundation.common.model.dto.response;

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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
