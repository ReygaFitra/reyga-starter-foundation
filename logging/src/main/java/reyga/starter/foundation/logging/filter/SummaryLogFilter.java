package reyga.starter.foundation.logging.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import reyga.starter.foundation.common.enumeration.HeaderEnum;

public class SummaryLogFilter extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {
        String summaryFlag = event.getMDCPropertyMap().get(HeaderEnum.SUMMARY_LOG.getValue());
        return "AFTER COMPLETION".equals(summaryFlag) ? FilterReply.ACCEPT : FilterReply.DENY;
    }

}
