package reyga.starter.foundation.logging.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class BaseRollingLogFilterTest {

    @ParameterizedTest
    @EnumSource(FilterReply.class)
    void should_ReturnDelegateReplyAndForwardEvent_When_FilterIsInvoked(FilterReply expectedReply) {
        // given
        ILoggingEvent event = mock(ILoggingEvent.class);
        AtomicInteger invocationCount = new AtomicInteger();
        AtomicReference<ILoggingEvent> receivedEvent = new AtomicReference<>();
        BaseRollingLogFilter filter = new BaseRollingLogFilter() {
            @Override
            protected FilterReply filter(ILoggingEvent value) {
                invocationCount.incrementAndGet();
                receivedEvent.set(value);
                return expectedReply;
            }
        };

        // when
        FilterReply result = filter.decide(event);

        // then
        assertEquals(expectedReply, result);
        assertEquals(1, invocationCount.get());
        assertSame(event, receivedEvent.get());
        verifyNoInteractions(event);
    }

    @Test
    void should_ForwardNullAndReturnDelegateReply_When_EventIsNull() {
        // given
        AtomicReference<ILoggingEvent> receivedEvent = new AtomicReference<>();
        BaseRollingLogFilter filter = new BaseRollingLogFilter() {
            @Override
            protected FilterReply filter(ILoggingEvent value) {
                receivedEvent.set(value);
                return FilterReply.NEUTRAL;
            }
        };

        // when
        FilterReply result = filter.decide(null);

        // then
        assertEquals(FilterReply.NEUTRAL, result);
        assertNull(receivedEvent.get());
    }

    @Test
    void should_PropagateSameException_When_DelegateThrowsException() {
        // given
        ILoggingEvent event = mock(ILoggingEvent.class);
        IllegalArgumentException expected = new IllegalArgumentException("filter failed");
        BaseRollingLogFilter filter = new BaseRollingLogFilter() {
            @Override
            protected FilterReply filter(ILoggingEvent value) {
                throw expected;
            }
        };

        // when
        IllegalArgumentException result = assertThrows(
                IllegalArgumentException.class,
                () -> filter.decide(event)
        );

        // then
        assertSame(expected, result);
        verifyNoInteractions(event);
    }
}
