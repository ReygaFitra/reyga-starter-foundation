package reyga.starter.foundation.common.logging;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommonLoggerTest {

    @Test
    void should_DelegateFormattedMessages_When_LogLevelsAreEnabled() {
        // given
        Logger dependency = enabledLogger();
        CommonLogger logger = new CommonLogger(dependency);

        // when
        logger.info("info");
        logger.debug("debug");
        logger.warn("warn");
        logger.error("error");

        // then
        verify(dependency).info("| info |");
        verify(dependency).debug("| debug |");
        verify(dependency).warn("| warn |");
        verify(dependency).error("| error |");
    }

    @Test
    void should_DelegateDeterministicKeyValueMessages_When_ValuesAreProvided() {
        // given
        Logger dependency = enabledLogger();
        CommonLogger logger = new CommonLogger(dependency);

        // when
        logger.info("single", "value");
        logger.debug("multiple", "first", 2);
        logger.warn("nullValue", (Object) null);
        logger.error("empty", (Object[]) null);

        // then
        verify(dependency).info("| {single=value} |");
        verify(dependency).debug("| {multiple=[first, 2]} |");
        verify(dependency).warn("| {nullValue=null} |");
        verify(dependency).error("| {empty=} |");
    }

    @Test
    void should_NotFormatOrWriteMessage_When_LogLevelIsDisabled() {
        // given
        Logger dependency = mock(Logger.class);
        CommonLogger logger = new CommonLogger(dependency);
        Object message = new Object() {
            @Override
            public String toString() {
                throw new AssertionError("message must not be formatted");
            }
        };

        // when
        logger.info(message);
        logger.debug(message);
        logger.warn(message);
        logger.error(message);

        // then
        verify(dependency, never()).info(anyString());
        verify(dependency, never()).debug(anyString());
        verify(dependency, never()).warn(anyString());
        verify(dependency, never()).error(anyString());
    }

    @Test
    void should_DelegateThrowableAtErrorLevel_When_ErrorIsLogged() {
        // given
        Logger dependency = enabledLogger();
        CommonLogger logger = new CommonLogger(dependency);
        IllegalStateException failure = new IllegalStateException("failed");

        // when
        logger.errorWithCause("Operation failed", failure);

        // then
        verify(dependency).error("| Operation failed |", failure);
    }

    @Test
    void should_DelegateServiceLifecycleMessages_When_ServiceNameIsProvided() {
        // given
        Logger dependency = enabledLogger();
        CommonLogger logger = new CommonLogger(dependency);

        // when
        logger.infoServiceStart("PaymentService");
        logger.infoServiceEnd("PaymentService");

        // then
        verify(dependency).info("==================== PaymentService Start ====================");
        verify(dependency).info("==================== PaymentService End ====================");
    }

    @Test
    void should_WriteCurrentSummaryFormatWithoutToken_When_LegacyAspectApiIsUsed() {
        // given
        Logger dependency = enabledLogger();
        CommonLogger logger = new CommonLogger(dependency);

        // when
        logger.infoAspectLog(
                "req",
                "secret-token",
                "user",
                "POST",
                201,
                "/items",
                "127.0.0.1",
                "pkg",
                null,
                "request",
                "response",
                "agent",
                "10ms"
        );

        // then
        verify(dependency).info(
                "SERVICE_EXECUTION | outcome=SUCCESS | handler=pkg | requestId=req"
                        + " | http={method=POST, endpoint=/items, status=201, duration=10ms}"
                        + " | caller={username=user, clientAddress=127.0.0.1, userAgent=agent}"
                        + " | request=request | response=response | error="
        );
    }

    @Test
    void should_WriteFailureSummaryOnOneLine_When_ServiceExecutionFails() {
        // given
        Logger dependency = enabledLogger();
        CommonLogger logger = new CommonLogger(dependency);
        ServiceExecutionSummary summary = new ServiceExecutionSummary(
                "NotificationController#update",
                "request-01",
                "operator",
                "PUT",
                500,
                "/notifications/1",
                "127.0.0.1",
                "test-agent",
                "12 ms",
                "{\"id\":1}",
                "{\"status\":\"FAILED\"}",
                "processing\nfailed"
        );

        // when
        logger.infoServiceSummary(summary);

        // then
        verify(dependency).info(
                "SERVICE_EXECUTION | outcome=FAILURE"
                        + " | handler=NotificationController#update | requestId=request-01"
                        + " | http={method=PUT, endpoint=/notifications/1, status=500, duration=12 ms}"
                        + " | caller={username=operator, clientAddress=127.0.0.1, userAgent=test-agent}"
                        + " | request={\"id\":1} | response={\"status\":\"FAILED\"}"
                        + " | error=processing\\nfailed"
        );
    }

    @Test
    void should_DelegateSingleErrorEvent_When_ExceptionIsProvided() {
        // given
        Logger dependency = enabledLogger();
        CommonLogger logger = new CommonLogger(dependency);
        IllegalStateException failure = new IllegalStateException("failed");

        // when
        logger.exception("process", "context", failure);

        // then
        verify(dependency).error(
                "Process [{}] failed | additionalInfo=[{}]",
                "process",
                "context",
                failure
        );
    }

    @Test
    void should_RejectNullMandatoryArguments_When_LoggerIsUsed() {
        // given
        Logger dependency = enabledLogger();
        CommonLogger logger = new CommonLogger(dependency);
        IllegalStateException failure = new IllegalStateException("failed");

        // when
        NullPointerException nullKey = assertThrows(
                NullPointerException.class,
                () -> logger.info(null, "value")
        );
        NullPointerException nullThrowable = assertThrows(
                NullPointerException.class,
                () -> logger.errorWithCause("failed", null)
        );
        NullPointerException nullProcess = assertThrows(
                NullPointerException.class,
                () -> logger.exception(null, "context", failure)
        );
        NullPointerException nullSummary = assertThrows(
                NullPointerException.class,
                () -> logger.infoServiceSummary(null)
        );

        // then
        assertEquals("key must not be null", nullKey.getMessage());
        assertEquals("throwable must not be null", nullThrowable.getMessage());
        assertEquals("process must not be null", nullProcess.getMessage());
        assertEquals("summary must not be null", nullSummary.getMessage());
    }

    @Test
    void should_CreateLogger_When_SourceClassIsProvided() {
        // given
        Class<?> sourceClass = CommonLoggerTest.class;

        // when
        CommonLogger result = CommonLoggerFactory.getLogger(sourceClass);

        // then
        assertNotNull(result);
    }

    @Test
    void should_RejectNullDependency_When_LoggerIsConstructed() {
        // when
        NullPointerException directResult = assertThrows(
                NullPointerException.class,
                () -> new CommonLogger((Logger) null)
        );
        NullPointerException factoryResult = assertThrows(
                NullPointerException.class,
                () -> CommonLoggerFactory.getLogger(null)
        );

        // then
        assertEquals("logger must not be null", directResult.getMessage());
        assertEquals("sourceClass must not be null", factoryResult.getMessage());
    }

    private Logger enabledLogger() {
        Logger logger = mock(Logger.class);
        when(logger.isInfoEnabled()).thenReturn(true);
        when(logger.isDebugEnabled()).thenReturn(true);
        when(logger.isWarnEnabled()).thenReturn(true);
        when(logger.isErrorEnabled()).thenReturn(true);
        return logger;
    }
}
