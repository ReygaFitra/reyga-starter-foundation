package reyga.starter.foundation.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Spring-managed method for processing by the configured
 * {@code BaseAspectAround} behavior.
 *
 * <p>Interception is active only when {@code reyga.config.aspect.around=true}
 * and {@code reyga.config.aspect.behavior} references a valid behavior bean.</p>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AroundExecution {
}
