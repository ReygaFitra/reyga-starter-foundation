package reyga.starter.foundation.logging.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import lombok.Setter;
import reyga.starter.foundation.common.enumeration.HeaderEnum;

public class SummaryLogFilter extends Filter<ILoggingEvent> {

    @Setter
    private static BaseSummaryLogFilter delegate;

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (delegate != null) {
            return delegate.decide(event);
        }

        String summaryFlag = event.getMDCPropertyMap().get(HeaderEnum.SUMMARY_LOG.getValue());
        if ("AFTER COMPLETION".equals(summaryFlag)) {
            return FilterReply.ACCEPT;
        }

        return FilterReply.DENY;
    }

}
