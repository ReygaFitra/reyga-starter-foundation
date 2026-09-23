package reyga.starter.foundation.common.logging;

public final class CommonLoggerFactory {

    private CommonLoggerFactory() {
    }

    public static CommonLogger getLogger(Class<?> sourceClass) {
        return new CommonLogger(sourceClass);
    }
}
