package reyga.starter.foundation.logging.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reyga.starter.foundation.common.enumeration.HeaderEnum;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SummaryLogFilterTest {

    @AfterEach
    void tearDown() {
        SummaryLogFilter.setDelegate(null);
    }

    @Test
    void decide_returnsAccept_whenSummaryFlagSet() {
        SummaryLogFilter filter = new SummaryLogFilter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        Map<String, String> mdc = new HashMap<>();
        mdc.put(HeaderEnum.SUMMARY_LOG.getValue(), "AFTER COMPLETION");
        when(event.getMDCPropertyMap()).thenReturn(mdc);

        FilterReply reply = filter.decide(event);
        assertEquals(FilterReply.ACCEPT, reply);
    }

    @Test
    void decide_returnsDeny_whenSummaryFlagMissing() {
        SummaryLogFilter filter = new SummaryLogFilter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getMDCPropertyMap()).thenReturn(new HashMap<>());

        FilterReply reply = filter.decide(event);
        assertEquals(FilterReply.DENY, reply);
    }

    @Test
    void decide_delegatesWhenProvided() {
        SummaryLogFilter.setDelegate(new BaseSummaryLogFilter() {
            @Override
            protected FilterReply filter(ILoggingEvent event) {
                return FilterReply.NEUTRAL;
            }
        });
        SummaryLogFilter filter = new SummaryLogFilter();
        FilterReply reply = filter.decide(mock(ILoggingEvent.class));
        assertEquals(FilterReply.NEUTRAL, reply);
    }
}
