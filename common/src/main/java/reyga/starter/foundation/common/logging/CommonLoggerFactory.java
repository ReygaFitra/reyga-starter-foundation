package reyga.starter.foundation.common.logging;

/**
 * Creates {@link CommonLogger} instances for classes that are not managed by Spring.
 */
public final class CommonLoggerFactory {

    private CommonLoggerFactory() {
    }

    /**
     * Creates a logger named after the supplied source class.
     *
     * @param sourceClass class that owns the logger; must not be {@code null}
     * @return a new foundation logger
     * @throws NullPointerException if {@code sourceClass} is {@code null}
     */
    public static CommonLogger getLogger(Class<?> sourceClass) {
        return new CommonLogger(sourceClass);
    }
}
