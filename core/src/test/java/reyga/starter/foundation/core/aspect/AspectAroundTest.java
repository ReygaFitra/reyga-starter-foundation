package reyga.starter.foundation.core.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AspectAroundTest {

    @Test
    void should_ReturnJoinPointResult_When_AspectIsDisabled() throws Throwable {
        // Given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        AspectProcessor processor = mock(AspectProcessor.class);
        when(joinPoint.proceed()).thenReturn("direct-result");
        AspectAround aspect = new AspectAround(false, processor);

        // When
        Object result = aspect.processRequestIntercept(joinPoint);

        // Then
        assertEquals("direct-result", result);
        verify(joinPoint).proceed();
        verifyNoInteractions(processor);
        verifyNoMoreInteractions(joinPoint);
    }

    @Test
    void should_ReturnProcessorResult_When_AspectIsEnabled() throws Throwable {
        // Given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        AspectProcessor processor = mock(AspectProcessor.class);
        when(processor.process(joinPoint)).thenReturn("processed-result");
        AspectAround aspect = new AspectAround(true, processor);

        // When
        Object result = aspect.processRequestIntercept(joinPoint);

        // Then
        assertEquals("processed-result", result);
        verify(processor).process(joinPoint);
        verifyNoInteractions(joinPoint);
        verifyNoMoreInteractions(processor);
    }

    @Test
    void should_PropagateException_When_EnabledProcessorFails() throws Throwable {
        // Given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        AspectProcessor processor = mock(AspectProcessor.class);
        IllegalStateException failure = new IllegalStateException("aspect failure");
        when(processor.process(joinPoint)).thenThrow(failure);
        AspectAround aspect = new AspectAround(true, processor);

        // When
        IllegalStateException result = assertThrows(
                IllegalStateException.class,
                () -> aspect.processRequestIntercept(joinPoint)
        );

        // Then
        assertSame(failure, result);
        verify(processor).process(joinPoint);
        verifyNoInteractions(joinPoint);
        verifyNoMoreInteractions(processor);
    }
}
