package reyga.starter.foundation.core.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter @Getter
public class AppFaultException extends RuntimeException {

    private final transient Object faultInfo;
    private final String errorCode;
    private final String errorMessage;
    private final HttpStatus statusCode;

    public AppFaultException(String errorCode, String errorMessage, Object faultInfo, HttpStatus statusCode) {
        super(errorMessage);
        this.faultInfo = faultInfo;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
    }

    public AppFaultException(String errorCode, String errorMessage, Object faultInfo, Throwable cause, HttpStatus statusCode) {
        super(errorMessage, cause);
        this.faultInfo = faultInfo;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
    }

    public AppFaultException(AppFaultContent faultContent) {
        super(faultContent.getErrorMessage());
        this.faultInfo = faultContent.getFaultInfo();
        this.errorCode = faultContent.getErrorCode();
        this.errorMessage = faultContent.getErrorMessage();
        this.statusCode = faultContent.getStatusCode();
    }

    public AppFaultException(AppFaultContent faultContent, Throwable cause) {
        super(faultContent.getErrorMessage(), cause);
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
