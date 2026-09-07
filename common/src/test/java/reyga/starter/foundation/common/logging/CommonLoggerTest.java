package reyga.starter.foundation.common.logging;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommonLoggerTest {

    @Test
    void should_DelegateFormattedMessages_When_AllLogLevelsAreCalled() throws Exception {
        // Given
        Logger dependency = mock(Logger.class);
        CommonLogger logger = loggerUsing(dependency);

        // When
        logger.info("info");
        logger.debug("debug");
        logger.warn("warn");
        logger.error("error");

        // Then
        verify(dependency).info("| info |");
        verify(dependency).debug("| debug |");
        verify(dependency).warn("| warn |");
        verify(dependency).error("| error |");
        verifyNoMoreInteractions(dependency);
    }

    @Test
    void should_DelegateKeyValueMessages_When_AllLogLevelsAreCalled() throws Exception {
        // Given
        Logger dependency = mock(Logger.class);
        CommonLogger logger = loggerUsing(dependency);

        // When
        logger.info("key", "value");
        logger.debug("key", "value");
        logger.warn("key", "value");
        logger.error("key", (Object[]) null);

        // Then
        ArgumentCaptor<String> info = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> debug = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> warn = ArgumentCaptor.forClass(String.class);
        verify(dependency).info(info.capture());
        verify(dependency).debug(debug.capture());
        verify(dependency).warn(warn.capture());
        verify(dependency).error("| {key=} |");
        assertTrue(info.getValue().startsWith("| {key=[Ljava.lang.Object;@"));
        assertTrue(debug.getValue().startsWith("| {key=[Ljava.lang.Object;@"));
        assertTrue(warn.getValue().startsWith("| {key=[Ljava.lang.Object;@"));
        verifyNoMoreInteractions(dependency);
    }

    @Test
    void should_DelegateServiceLifecycleMessages_When_ServiceNameIsProvided() throws Exception {
        // Given
        Logger dependency = mock(Logger.class);
        CommonLogger logger = loggerUsing(dependency);

        // When
        logger.infoServiceStart("PaymentService");
        logger.infoServiceEnd("PaymentService");

        // Then
        verify(dependency).info("==================== PaymentService Start ====================");
        verify(dependency).info("==================== PaymentService End ====================");
        verifyNoMoreInteractions(dependency);
    }

    @Test
    void should_DelegateAspectSummary_When_AllAspectValuesAreProvided() throws Exception {
        // Given
        Logger dependency = mock(Logger.class);
        CommonLogger logger = loggerUsing(dependency);

        // When
        logger.infoAspectLog("req", "token", "user", "POST", 201, "/items", "127.0.0.1",
                "pkg", "none", "request", "response", "agent", "10ms");

        // Then
        verify(dependency).info("[req | token | user | POST | 201 | /items | 127.0.0.1 | pkg | none | request | response | agent | 10ms]");
        verifyNoMoreInteractions(dependency);
    }

    @Test
    void should_DelegateTraceInformation_When_ExceptionIsProvided() throws Exception {
        // Given
        Logger dependency = mock(Logger.class);
        CommonLogger logger = loggerUsing(dependency);
        IllegalStateException failure = new IllegalStateException("failed");

        // When
        logger.exception("process", "context", failure);

        // Then
        verify(dependency).error("::::::::::>>>>>>>>>> TRACING ERROR process START <<<<<<<<<<::::::::::");
        verify(dependency).error(eq("Error :{}"), contains("java.lang.IllegalStateException: failed at "));
        verify(dependency).error("Exception Message :{}", "| failed |");
        verify(dependency).error(eq("Additional Info Fault :{}"),
                (Object) argThat(value -> value.toString().equals("| [context]")));
        verify(dependency).error("::::::::::>>>>>>>>>> TRACING ERROR END <<<<<<<<<<::::::::::");
        verify(dependency).debug("Stacktrace", failure);
        verifyNoMoreInteractions(dependency);
    }

    @Test
    void should_CreateLogger_When_SourceClassIsProvided() {
        // Given
        Class<?> sourceClass = CommonLoggerTest.class;

        // When
        CommonLogger result = CommonLoggerFactory.getLogger(sourceClass);

        // Then
        assertNotNull(result);
    }

    private CommonLogger loggerUsing(Logger dependency) throws Exception {
        CommonLogger logger = new CommonLogger(CommonLoggerTest.class);
        Field field = CommonLogger.class.getDeclaredField("logger");
        field.setAccessible(true);
        field.set(logger, dependency);
        return logger;
    }
}
