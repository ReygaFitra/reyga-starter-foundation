package reyga.starter.foundation.logging.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import lombok.Setter;

public class ConsoleLogFilter extends Filter<ILoggingEvent> {

    @Setter
    private static BaseConsoleLogFilter delegate;

    @Override
    public FilterReply decide(ILoggingEvent event) {
        return delegate != null ? delegate.decide(event) : FilterReply.NEUTRAL;
    }
}
