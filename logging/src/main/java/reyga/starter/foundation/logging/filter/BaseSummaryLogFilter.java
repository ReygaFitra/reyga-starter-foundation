package reyga.starter.foundation.logging.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public abstract class BaseSummaryLogFilter extends Filter<ILoggingEvent> {
    @Override
    public FilterReply decide(ILoggingEvent event) {
        return filter(event);
    }

    protected abstract FilterReply filter(ILoggingEvent event);
}
