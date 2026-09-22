package reyga.starter.foundation.core.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AspectAroundTest {

    @Test
    void should_ReturnProcessorResult_When_MethodIsIntercepted() throws Throwable {
        // Given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        AspectProcessor processor = mock(AspectProcessor.class);
        when(processor.process(joinPoint)).thenReturn("processed-result");
        AspectAround aspect = new AspectAround(processor);

        // When
        Object result = aspect.processRequestIntercept(joinPoint);

        // Then
        assertEquals("processed-result", result);
        verify(processor).process(joinPoint);
        verifyNoInteractions(joinPoint);
        verifyNoMoreInteractions(processor);
    }

    @Test
    void should_PropagateException_When_ProcessorFails() throws Throwable {
        // Given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        AspectProcessor processor = mock(AspectProcessor.class);
        IllegalStateException failure = new IllegalStateException("aspect failure");
        when(processor.process(joinPoint)).thenThrow(failure);
        AspectAround aspect = new AspectAround(processor);

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
