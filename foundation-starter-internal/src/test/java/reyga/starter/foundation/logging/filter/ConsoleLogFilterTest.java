package reyga.starter.foundation.logging.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ConsoleLogFilterTest {

    @AfterEach
    void tearDown() {
        ConsoleLogFilter.setDelegate(null);
    }

    @Test
    void decide_returnsNeutral_whenDelegateNull() {
        ConsoleLogFilter filter = new ConsoleLogFilter();
        FilterReply reply = filter.decide(mock(ILoggingEvent.class));
        assertEquals(FilterReply.NEUTRAL, reply);
    }

    @Test
    void decide_delegatesWhenProvided() {
        ConsoleLogFilter.setDelegate(new BaseConsoleLogFilter() {
            @Override
            protected FilterReply filter(ILoggingEvent event) {
                return FilterReply.ACCEPT;
            }
        });
        ConsoleLogFilter filter = new ConsoleLogFilter();
        FilterReply reply = filter.decide(mock(ILoggingEvent.class));
        assertEquals(FilterReply.ACCEPT, reply);
    }
}
