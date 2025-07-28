package reyga.starter.foundation.common.exception;

import org.springframework.http.HttpStatus;

/**
 * The type App fault exception.
 */
public class AppFaultException extends Exception {
    /**
     * The Fault info.
     */
    private Object faultInfo;
    /**
     * The Error code.
     */
    private String errorCode;
    /**
     * The Error message.
     */
    private String errorMessage;
    /**
     * The Http Status Code.
     */
    private HttpStatus statusCode;

    /**
     * Instantiates a new App fault exception.
     *
     * @param errorCode    the error code
     * @param errorMessage the error message
     * @param faultInfo    the fault info
     * @param statusCode   the http status code
     */
    public AppFaultException(String errorCode, String errorMessage, Object faultInfo, HttpStatus statusCode) {
        super(errorMessage);
        this.faultInfo = faultInfo;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
    }

    /**
     * Gets fault info.
     *
     * @return the fault info
     */
    public Object getFaultInfo() {
        return faultInfo;
    }

    /**
     * Sets fault info.
     *
     * @param faultInfo the fault info
     */
    public void setFaultInfo(Object faultInfo) {
        this.faultInfo = faultInfo;
    }

    /**
     * Gets error code.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Sets error code.
     *
     * @param errorCode the error code
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Gets error message.
     *
     * @return the error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets error message.
     *
     * @param errorMessage the error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Gets status code.
     *
     * @return the status code
     */
    public HttpStatus getStatusCode() {
        return statusCode;
    }

    /**
     * Sets status code.
     *
     * @param statusCode the status code
     */
    public void setStatusCode(HttpStatus statusCode) {
        this.statusCode = statusCode;
    }
}
