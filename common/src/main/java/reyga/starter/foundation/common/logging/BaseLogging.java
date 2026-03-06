package reyga.starter.foundation.common.logging;

public abstract class BaseLogging {
    protected final CommonLogger log = CommonLoggerFactory.getLogger(getClass());
}
