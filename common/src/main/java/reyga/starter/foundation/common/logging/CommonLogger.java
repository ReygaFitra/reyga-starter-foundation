package reyga.starter.foundation.common.logging;

import java.util.Arrays;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the foundation logging format on top of SLF4J.
 *
 * <p>Instances are thread-safe when the underlying SLF4J {@link Logger} is thread-safe,
 * as required by the SLF4J contract. Message values are formatted only when their log
 * level is enabled. Applications should not pass credentials, authorization headers,
 * tokens, or unmasked personal data to this logger.</p>
 */
public class CommonLogger {

    private static final String SPACE = " ";
    private static final String DELIMITER = "|";

    private final Logger logger;

    /**
     * Creates a logger named after the supplied source class.
     *
     * @param sourceClass class that owns the logger; must not be {@code null}
     * @throws NullPointerException if {@code sourceClass} is {@code null}
     */
    public CommonLogger(Class<?> sourceClass) {
        this(LoggerFactory.getLogger(Objects.requireNonNull(
                sourceClass,
                "sourceClass must not be null"
        )));
    }

    CommonLogger(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger must not be null");
    }

    /**
     * Writes an informational message using the foundation message format.
     *
     * @param message message value; {@code null} is rendered as {@code null}
     */
    public void info(Object message) {
        if (logger.isInfoEnabled()) {
            logger.info(formatMessage(message));
        }
    }

    /**
     * Writes informational structured data under a key.
     *
     * @param key descriptive key; must not be {@code null}
     * @param value associated values; a {@code null} array is rendered as an empty value
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public void info(String key, Object... value) {
        if (logger.isInfoEnabled()) {
            logger.info(formatKeyValue(key, value));
        }
    }

    /**
     * Writes a debug message using the foundation message format.
     *
     * @param message message value; {@code null} is rendered as {@code null}
     */
    public void debug(Object message) {
        if (logger.isDebugEnabled()) {
            logger.debug(formatMessage(message));
        }
    }

    /**
     * Writes debug structured data under a key.
     *
     * @param key descriptive key; must not be {@code null}
     * @param value associated values; a {@code null} array is rendered as an empty value
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public void debug(String key, Object... value) {
        if (logger.isDebugEnabled()) {
            logger.debug(formatKeyValue(key, value));
        }
    }

    /**
     * Writes a warning message using the foundation message format.
     *
     * @param message message value; {@code null} is rendered as {@code null}
     */
    public void warn(Object message) {
        if (logger.isWarnEnabled()) {
            logger.warn(formatMessage(message));
        }
    }

    /**
     * Writes warning structured data under a key.
     *
     * @param key descriptive key; must not be {@code null}
     * @param value associated values; a {@code null} array is rendered as an empty value
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public void warn(String key, Object... value) {
        if (logger.isWarnEnabled()) {
            logger.warn(formatKeyValue(key, value));
        }
    }

    /**
     * Writes an error message using the foundation message format.
     *
     * @param message message value; {@code null} is rendered as {@code null}
     */
    public void error(Object message) {
        if (logger.isErrorEnabled()) {
            logger.error(formatMessage(message));
        }
    }

    /**
     * Writes error structured data under a key.
     *
     * @param key descriptive key; must not be {@code null}
     * @param value associated values; a {@code null} array is rendered as an empty value
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public void error(String key, Object... value) {
        if (logger.isErrorEnabled()) {
            logger.error(formatKeyValue(key, value));
        }
    }

    /**
     * Writes an error message together with its original throwable.
     *
     * @param message error description; must not be {@code null}
     * @param throwable original failure; must not be {@code null}
     * @throws NullPointerException if an argument is {@code null}
     */
    public void errorWithCause(String message, Throwable throwable) {
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(throwable, "throwable must not be null");
        if (logger.isErrorEnabled()) {
            logger.error(formatMessage(message), throwable);
        }
    }

    /**
     * Writes one error event for a failed process and preserves the complete stack trace.
     *
     * @param process process or operation name; must not be {@code null}
     * @param infoFault additional diagnostic context; must not contain sensitive data
     * @param exception original exception; must not be {@code null}
     * @throws NullPointerException if {@code process} or {@code exception} is {@code null}
     */
    public void exception(String process, Object infoFault, Exception exception) {
        Objects.requireNonNull(process, "process must not be null");
        Objects.requireNonNull(exception, "exception must not be null");
        if (logger.isErrorEnabled()) {
            logger.error(
                    "Process [{}] failed | additionalInfo=[{}]",
                    process,
                    infoFault,
                    exception
            );
        }
    }

    /**
     * Writes a service-start lifecycle marker.
     *
     * @param serviceName service or method name; must not be {@code null}
     * @throws NullPointerException if {@code serviceName} is {@code null}
     */
    public void infoServiceStart(String serviceName) {
        Objects.requireNonNull(serviceName, "serviceName must not be null");
        if (logger.isInfoEnabled()) {
            logger.info(serviceLifecycleLog(serviceName, "Start"));
        }
    }

    /**
     * Writes a service-end lifecycle marker.
     *
     * @param serviceName service or method name; must not be {@code null}
     * @throws NullPointerException if {@code serviceName} is {@code null}
     */
    public void infoServiceEnd(String serviceName) {
        Objects.requireNonNull(serviceName, "serviceName must not be null");
        if (logger.isInfoEnabled()) {
            logger.info(serviceLifecycleLog(serviceName, "End"));
        }
    }

    /**
     * Writes one structured event that summarizes a completed HTTP service execution.
     *
     * <p>Carriage returns and line feeds are escaped to keep the summary on one log
     * line. Values supplied by the caller must already be sanitized.</p>
     *
     * @param summary completed execution details; must not be {@code null}
     * @throws NullPointerException if {@code summary} is {@code null}
     */
    public void infoServiceSummary(ServiceExecutionSummary summary) {
        Objects.requireNonNull(summary, "summary must not be null");
        if (logger.isInfoEnabled()) {
            logger.info(serviceSummaryFormat(summary));
        }
    }

    /**
     * Writes the legacy HTTP summary using the current service-execution format.
     *
     * <p>The access token is deliberately omitted. New integrations should use
     * {@link #infoServiceSummary(ServiceExecutionSummary)}. Callers remain responsible
     * for sanitizing request, response, error, username, and network values.</p>
     *
     * @param reqId request correlation identifier
     * @param token access token, which is never written verbatim
     * @param userName authenticated username
     * @param method HTTP method
     * @param status HTTP response status
     * @param reqEndpoint request endpoint
     * @param ipAddr client address
     * @param pkg package or handler information
     * @param error error summary
     * @param request sanitized request summary
     * @param response sanitized response summary
     * @param userAgent user-agent summary
     * @param respTime response duration
     */
    public void infoAspectLog(
            String reqId,
            String token,
            String userName,
            String method,
            Integer status,
            String reqEndpoint,
            String ipAddr,
            String pkg,
            String error,
            String request,
            String response,
            String userAgent,
            String respTime
    ) {
        infoServiceSummary(new ServiceExecutionSummary(
                pkg,
                reqId,
                userName,
                method,
                status != null ? status : 0,
                reqEndpoint,
                ipAddr,
                userAgent,
                respTime,
                request,
                response,
                error
        ));
    }

    private static String serviceLifecycleLog(String serviceName, String phase) {
        return "==================== " + serviceName + SPACE + phase + " ====================";
    }

    private static String serviceSummaryFormat(ServiceExecutionSummary summary) {
        String outcome = summary.statusCode() >= 400 || hasText(summary.error())
                ? "FAILURE"
                : "SUCCESS";
        return "SERVICE_EXECUTION"
                + " | outcome=" + outcome
                + " | handler=" + singleLine(summary.handler())
                + " | requestId=" + singleLine(summary.requestId())
                + " | http={method=" + singleLine(summary.httpMethod())
                + ", endpoint=" + singleLine(summary.endpoint())
                + ", status=" + summary.statusCode()
                + ", duration=" + singleLine(summary.duration()) + "}"
                + " | caller={username=" + singleLine(summary.username())
                + ", clientAddress=" + singleLine(summary.clientAddress())
                + ", userAgent=" + singleLine(summary.userAgent()) + "}"
                + " | request=" + singleLine(summary.request())
                + " | response=" + singleLine(summary.response())
                + " | error=" + singleLine(summary.error());
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String singleLine(String value) {
        return Objects.toString(value, "")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private static String formatMessage(Object message) {
        return DELIMITER + SPACE + Objects.toString(message) + SPACE + DELIMITER;
    }

    private static String formatKeyValue(String key, Object... values) {
        Objects.requireNonNull(key, "key must not be null");
        return DELIMITER + SPACE + "{" + key + "=" + formatValues(values) + "}"
                + SPACE + DELIMITER;
    }

    private static String formatValues(Object... values) {
        if (values == null || values.length == 0) {
            return "";
        }
        if (values.length == 1) {
            return Objects.toString(values[0]);
        }
        return Arrays.deepToString(values);
    }
}
