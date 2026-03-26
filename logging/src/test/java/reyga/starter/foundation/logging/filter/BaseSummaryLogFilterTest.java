package reyga.starter.foundation.logging.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class BaseSummaryLogFilterTest {

    @Test
    void decide_delegatesToFilter() {
        AtomicBoolean called = new AtomicBoolean(false);
        BaseSummaryLogFilter filter = new BaseSummaryLogFilter() {
            @Override
            protected FilterReply filter(ILoggingEvent event) {
                called.set(true);
                return FilterReply.NEUTRAL;
            }
        };

        FilterReply reply = filter.decide(mock(ILoggingEvent.class));
        assertEquals(FilterReply.NEUTRAL, reply);
        assertTrue(called.get());
    }
}
