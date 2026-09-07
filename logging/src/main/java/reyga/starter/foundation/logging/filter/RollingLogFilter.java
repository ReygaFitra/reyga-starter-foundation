package reyga.starter.foundation.logging.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Default rolling filter instantiated directly by Logback.
 * Customize filtering by extending BaseRollingLogFilter and selecting that class in Logback XML.
 */
public class RollingLogFilter extends BaseRollingLogFilter {

    @Override
    protected FilterReply filter(ILoggingEvent event) {
        return FilterReply.NEUTRAL;
    }
}
