package reyga.starter.foundation.core.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter @Getter
public class AppFaultException extends RuntimeException {

    private Object faultInfo;
    private String errorCode;
    private String errorMessage;
    private HttpStatus statusCode;

    public AppFaultException(String errorCode, String errorMessage, Object faultInfo, HttpStatus statusCode) {
        super(errorMessage);
        this.faultInfo = faultInfo;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
    }

    public AppFaultException(AppFaultContent faultContent) {
        super(faultContent.getMessage());
        this.faultInfo = faultContent.getFaultInfo();
        this.errorCode = faultContent.getErrorCode();
        this.errorMessage = faultContent.getErrorMessage();
        this.statusCode = faultContent.getStatusCode();
    }

    @Override
    public String toString() {
        return "AppFaultException{" +
                "faultInfo=" + faultInfo +
                ", errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", statusCode=" + statusCode +
                '}';
    }
}
