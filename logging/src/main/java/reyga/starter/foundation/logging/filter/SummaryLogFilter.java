package reyga.starter.foundation.logging.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import reyga.starter.foundation.common.enumeration.HeaderEnum;

/**
 * Default summary filter instantiated directly by Logback.
 * Customize filtering by extending BaseSummaryLogFilter and selecting that class in Logback XML.
 */
public class SummaryLogFilter extends BaseSummaryLogFilter {

    @Override
    protected FilterReply filter(ILoggingEvent event) {
        String summaryFlag = event.getMDCPropertyMap().get(HeaderEnum.SUMMARY_LOG.getValue());
        return "AFTER COMPLETION".equals(summaryFlag) ? FilterReply.ACCEPT : FilterReply.DENY;
    }
}
