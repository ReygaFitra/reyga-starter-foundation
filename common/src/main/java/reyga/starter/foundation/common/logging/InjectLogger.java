package reyga.starter.foundation.common.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a non-static {@link CommonLogger} field for injection by {@link LoggerInjector}.
 *
 * <p>The annotated field may be private or inherited, but it must not be static or
 * final. An existing non-null logger is preserved.</p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectLogger {
}
