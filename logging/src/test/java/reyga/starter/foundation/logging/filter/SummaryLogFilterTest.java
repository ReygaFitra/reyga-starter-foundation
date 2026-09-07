package reyga.starter.foundation.logging.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import reyga.starter.foundation.common.enumeration.HeaderEnum;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SummaryLogFilterTest {

    @Test
    void should_ReturnAccept_When_SummaryMarkerMatches() {
        // given
        BaseSummaryLogFilter filter = new SummaryLogFilter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getMDCPropertyMap()).thenReturn(
                Map.of(HeaderEnum.SUMMARY_LOG.getValue(), "AFTER COMPLETION"));

        // when
        FilterReply result = filter.decide(event);

        // then
        assertEquals(FilterReply.ACCEPT, result);
        verify(event).getMDCPropertyMap();
        verifyNoMoreInteractions(event);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"BEFORE COMPLETION", "after completion", " AFTER COMPLETION "})
    void should_ReturnDeny_When_SummaryMarkerDoesNotMatch(String marker) {
        // given
        BaseSummaryLogFilter filter = new SummaryLogFilter();
        ILoggingEvent event = mock(ILoggingEvent.class);
        Map<String, String> mdc = new HashMap<>();
        if (marker != null) {
            mdc.put(HeaderEnum.SUMMARY_LOG.getValue(), marker);
        }
        when(event.getMDCPropertyMap()).thenReturn(mdc);

        // when
        FilterReply result = filter.decide(event);

        // then
        assertEquals(FilterReply.DENY, result);
        verify(event).getMDCPropertyMap();
        verifyNoMoreInteractions(event);
    }
}
