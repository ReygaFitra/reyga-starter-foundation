package reyga.starter.foundation.logging.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RollingLogFilterTest {

    @Test
    void should_ReturnNeutral_When_EventIsProvided() {
        // given
        BaseRollingLogFilter filter = new RollingLogFilter();
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
        BaseRollingLogFilter filter = new RollingLogFilter();

        // when
        FilterReply result = filter.decide(null);

        // then
        assertEquals(FilterReply.NEUTRAL, result);
    }
}
