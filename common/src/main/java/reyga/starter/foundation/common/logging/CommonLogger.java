package reyga.starter.foundation.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CommonLogger {

    private final Logger logger;

    private static final String DEFAULT_SPACE_VAR = " ";
    private static final String DEFAULT_DELIMETER_VAR = "|";
    private static final String DEFAULT_PREFIX_VAR = "[";
    private static final String DEFAULT_SUFFIX_VAR = "]";

    public CommonLogger(Class<?> sourceClass) {
        this.logger = LoggerFactory.getLogger(sourceClass);
    }

    public void info(Object message) {
        this.logger.info(getFormattedMessage(message));
    }

    public void info(String key, Object... value) {
        this.logger.info(getFormattedMessage(key, value));
    }

    public void debug(Object message) {
        this.logger.debug(getFormattedMessage(message));
    }

    public void debug(String key, Object... value) {
        this.logger.debug(getFormattedMessage(key, value));
    }

    public void warn(Object message) {
        this.logger.warn(getFormattedMessage(message));
    }

    public void warn(String key, Object... value) {
        this.logger.warn(getFormattedMessage(key, value));
    }

    public void error(Object message) {
        this.logger.error(getFormattedMessage(message));
    }

    public void error(String key, Object... value) {
        this.logger.error(getFormattedMessage(key, value));
    }

    public void exception(String process, Object infoFault, Exception e) {
        StringBuilder additionalInfoFault = new StringBuilder()
                .append(DEFAULT_DELIMETER_VAR)
                .append(DEFAULT_SPACE_VAR)
                .append(DEFAULT_PREFIX_VAR)
                .append(infoFault)
                .append(DEFAULT_SUFFIX_VAR);
        this.logger.error("::::::::::>>>>>>>>>> TRACING ERROR " + process + " START <<<<<<<<<<::::::::::");
        this.logger.error("Error :{}", e + DEFAULT_SPACE_VAR + "at" + DEFAULT_SPACE_VAR + e.getStackTrace()[0]);
        this.logger.error("Exception Message :{}", getFormattedMessage(e.getMessage()));
        this.logger.error("Additional Info Fault :{}", additionalInfoFault);
        this.logger.error("::::::::::>>>>>>>>>> TRACING ERROR END <<<<<<<<<<::::::::::");
        this.logger.debug("Stacktrace", e);
    }

    public void infoServiceStart(String serviceName) {
        this.logger.info(serviceStartLog(serviceName));
    }

    public void infoServiceEnd(String serviceName) {
        this.logger.info(serviceEndLog(serviceName));
    }

    public void infoAspectLog(String reqId, String token, String userName, String method, Integer status, String reqEndpoint,
                              String ipAddr, String pkg, String error, String request, String response, String userAgent, String respTime) {
        this.logger.info(aspectLogFormat(reqId, token, userName, method, status, reqEndpoint, ipAddr, pkg, error, request, response, userAgent, respTime));
    }

    private String serviceStartLog(String object) {
        return String.format("==================== %s Start ====================", object);
    }

    private String serviceEndLog(String object) {
        return String.format("==================== %s End ====================", object);
    }

    private String aspectLogFormat(String reqId, String token, String userName, String method, Integer status, String reqEndpoint,
                                   String ipAddr, String pkg, String error, String request, String response, String userAgent, String respTime) {
        return String.format(
                DEFAULT_PREFIX_VAR + String.join(
                        DEFAULT_SPACE_VAR + DEFAULT_DELIMETER_VAR + DEFAULT_SPACE_VAR,
                        reqId, token, userName, method, status.toString(), reqEndpoint, ipAddr, pkg, error, request, response, userAgent, respTime
                ) + DEFAULT_SUFFIX_VAR);
    }

    private static String getFormattedMessage(Object message) {
        return DEFAULT_DELIMETER_VAR + DEFAULT_SPACE_VAR + message + DEFAULT_SPACE_VAR + DEFAULT_DELIMETER_VAR;
    }

    private static String getFormattedMessage(String key, Object... value) {
        Map<String, Object> message = new HashMap<>();
        message.put(key, value == null ? "" : value);
        return DEFAULT_DELIMETER_VAR + DEFAULT_SPACE_VAR + message + DEFAULT_SPACE_VAR + DEFAULT_DELIMETER_VAR;
    }
}
