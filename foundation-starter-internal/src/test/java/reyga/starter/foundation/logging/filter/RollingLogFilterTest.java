package reyga.starter.foundation.logging.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class RollingLogFilterTest {

    @AfterEach
    void tearDown() {
        RollingLogFilter.setDelegate(null);
    }

    @Test
    void decide_returnsNeutral_whenDelegateNull() {
        RollingLogFilter filter = new RollingLogFilter();
        FilterReply reply = filter.decide(mock(ILoggingEvent.class));
        assertEquals(FilterReply.NEUTRAL, reply);
    }

    @Test
    void decide_delegatesWhenProvided() {
        RollingLogFilter.setDelegate(new BaseRollingLogFilter() {
            @Override
            protected FilterReply filter(ILoggingEvent event) {
                return FilterReply.DENY;
            }
        });
        RollingLogFilter filter = new RollingLogFilter();
        FilterReply reply = filter.decide(mock(ILoggingEvent.class));
        assertEquals(FilterReply.DENY, reply);
    }
}
