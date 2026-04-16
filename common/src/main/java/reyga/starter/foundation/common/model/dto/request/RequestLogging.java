package reyga.starter.foundation.common.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RequestLogging {
    private Map<String, Object> requestParams;
    private Map<String, Object> requestMultiPart;
    private Map<String, Object> requestPathVariable;
    private Object requestBody;

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
