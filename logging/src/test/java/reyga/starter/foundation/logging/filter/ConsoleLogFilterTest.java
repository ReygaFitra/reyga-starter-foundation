package reyga.starter.foundation.logging.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsoleLogFilterTest {

    @Test
    void should_ReturnNeutral_When_EventIsProvided() {
        // given
        BaseConsoleLogFilter filter = new ConsoleLogFilter();
        ILoggingEvent event = mock(ILoggingEvent.class);

        // when
        FilterReply result = filter.decide(event);

        // then
        assertEquals(FilterReply.NEUTRAL, result);
        verifyNoInteractions(event);
    }

    @Test
    void should_ReturnNeutral_When_EventIsNull() {
        // given
        BaseConsoleLogFilter filter = new ConsoleLogFilter();

        // when
        FilterReply result = filter.decide(null);

        // then
        assertEquals(FilterReply.NEUTRAL, result);
    }
}
