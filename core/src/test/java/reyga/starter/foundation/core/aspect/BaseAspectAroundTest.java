package reyga.starter.foundation.core.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import reyga.starter.foundation.common.model.dto.content.BaseContent;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BaseAspectAroundTest {

    @Test
    void should_ReturnJoinPointResultAndRunLifecycle_When_ProcessingSucceeds() throws Throwable {
        // Given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn("result");
        TestAspect aspect = spy(new TestAspect(false));

        // When
        Object result = aspect.process(joinPoint);

        // Then
        assertEquals("result", result);
        verify(aspect).preHandle();
        verify(joinPoint).proceed();
        verify(aspect).postHandle(aspect.content);
        verify(aspect, never()).handleException(any(), any());
        verifyNoMoreInteractions(joinPoint);
    }

    @Test
    void should_HandleAndRethrowException_When_ExceptionHandlingIsEnabled() throws Throwable {
        // Given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        IllegalArgumentException failure = new IllegalArgumentException("failure");
        when(joinPoint.proceed()).thenThrow(failure);
        TestAspect aspect = spy(new TestAspect(true));

        // When
        IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> aspect.process(joinPoint));

        // Then
        assertSame(failure, result);
        verify(aspect).preHandle();
        verify(aspect).shouldHandleException();
        verify(aspect).handleException(failure, aspect.content);
        verify(aspect).postHandle(aspect.content);
        verify(joinPoint).proceed();
    }

    @Test
    void should_RethrowWithoutHandling_When_ExceptionHandlingIsDisabled() throws Throwable {
        // Given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        IllegalStateException failure = new IllegalStateException("failure");
        when(joinPoint.proceed()).thenThrow(failure);
        TestAspect aspect = spy(new TestAspect(false));

        // When
        IllegalStateException result = assertThrows(IllegalStateException.class, () -> aspect.process(joinPoint));

        // Then
        assertSame(failure, result);
        verify(aspect).preHandle();
        verify(aspect).shouldHandleException();
        verify(aspect, never()).handleException(any(), any());
        verify(aspect).postHandle(aspect.content);
        verify(joinPoint).proceed();
    }

    private static final class DummyContent extends BaseContent {
    }

    private static class TestAspect extends BaseAspectAround<DummyContent> {
        private final DummyContent content = new DummyContent();
        private final boolean handleExceptions;

        private TestAspect(boolean handleExceptions) {
            this.handleExceptions = handleExceptions;
        }

        @Override
        protected DummyContent preHandle() {
            return content;
        }

        @Override
        protected void postHandle(DummyContent content) {
        }

        @Override
        protected boolean shouldHandleException() {
            return handleExceptions;
        }

        @Override
        protected void handleException(Exception exception, DummyContent content) {
        }
    }
}
