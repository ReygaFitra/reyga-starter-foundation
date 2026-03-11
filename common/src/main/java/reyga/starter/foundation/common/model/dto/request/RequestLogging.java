package reyga.starter.foundation.common.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RequestLogging implements Serializable {
    private transient Map<String, Object> requestParams;
    private transient Map<String, Object> requestMultiPart;
    private transient Map<String, Object> requestPathVariable;
    private transient Object requestBody;

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
