package reyga.starter.foundation.logging.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Default console filter instantiated directly by Logback.
 * Customize filtering by extending BaseConsoleLogFilter and selecting that class in Logback XML.
 */
public class ConsoleLogFilter extends BaseConsoleLogFilter {

    @Override
    protected FilterReply filter(ILoggingEvent event) {
        return FilterReply.NEUTRAL;
    }
}
