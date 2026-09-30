package reyga.starter.foundation.common.logging;

/**
 * Describes the observable result of one HTTP service execution.
 *
 * <p>The summary is immutable, but its textual request, response, and error values may
 * still contain sensitive application data. Callers must mask those values before
 * constructing this record.</p>
 *
 * @param handler fully qualified handler method that processed the request
 * @param requestId request correlation identifier
 * @param username authenticated username
 * @param httpMethod HTTP request method
 * @param statusCode HTTP response status code
 * @param endpoint requested endpoint
 * @param clientAddress originating client address
 * @param userAgent caller user-agent value
 * @param duration total processing duration
 * @param request sanitized request metadata summary
 * @param response sanitized service response summary, or {@code null} when the
 *                 active interceptor does not capture response bodies
 * @param error sanitized failure summary, or {@code null} after successful execution
 */
public record ServiceExecutionSummary(
        String handler,
        String requestId,
        String username,
        String httpMethod,
        int statusCode,
        String endpoint,
        String clientAddress,
        String userAgent,
        String duration,
        String request,
        String response,
        String error
) {
}
