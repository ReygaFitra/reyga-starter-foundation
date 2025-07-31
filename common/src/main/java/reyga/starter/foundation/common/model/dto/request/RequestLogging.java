package reyga.starter.foundation.common.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import reyga.starter.foundation.core.dto.request.BaseRequest;

import java.util.Map;

@NoArgsConstructor @AllArgsConstructor
public class RequestLogging extends BaseRequest {
    private Map<String, Object> requestParams;
    private Map<String, Object> requestMultiPart;
    private Map<String, Object> requestPathVariable;
    private Object requestBody;

    public Map<String, Object> getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(Map<String, Object> requestParams) {
        this.requestParams = requestParams;
    }

    public Map<String, Object> getRequestMultiPart() {
        return requestMultiPart;
    }

    public void setRequestMultiPart(Map<String, Object> requestMultiPart) {
        this.requestMultiPart = requestMultiPart;
    }

    public Map<String, Object> getRequestPathVariable() {
        return requestPathVariable;
    }

    public void setRequestPathVariable(Map<String, Object> requestPathVariable) {
        this.requestPathVariable = requestPathVariable;
    }

    public Object getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(Object requestBody) {
        this.requestBody = requestBody;
    }

    @Override
    public String toString() {
        return "{" +
                "requestParams=" + requestParams +
                ", requestMultiPart=" + requestMultiPart +
                ", requestPathVariable=" + requestPathVariable +
                ", requestBody=" + requestBody +
                '}';
    }
}
