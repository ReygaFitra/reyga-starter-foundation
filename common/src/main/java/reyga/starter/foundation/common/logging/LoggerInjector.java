package reyga.starter.foundation.common.logging;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Injects a class-specific {@link CommonLogger} into Spring bean fields annotated with
 * {@link InjectLogger}.
 *
 * <p>Fields are inspected across the complete class hierarchy before bean initialization.
 * An existing logger is preserved. Annotated fields must have type {@code CommonLogger}
 * and must not be static or final.</p>
 */
@Component
public class LoggerInjector implements BeanPostProcessor {

    /**
     * Creates the logger bean post-processor.
     */
    public LoggerInjector() {
    }

    /**
     * Injects missing loggers before the bean initialization callbacks run.
     *
     * @param bean bean being initialized
     * @param beanName bean name in the application context
     * @return the same bean instance
     * @throws BeansException if an annotated field is invalid or cannot be accessed
     */
    @Override
    public Object postProcessBeforeInitialization(
            @NonNull Object bean,
            @NonNull String beanName
    ) throws BeansException {
        Class<?> currentClass = bean.getClass();

        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                injectLogger(field, bean);
            }
            currentClass = currentClass.getSuperclass();
        }

        return bean;
    }

    /**
     * Returns the initialized bean without modification.
     *
     * @param bean initialized bean
     * @param beanName bean name in the application context
     * @return the same bean instance
     * @throws BeansException when another post-processing contract fails
     */
    @Override
    public Object postProcessAfterInitialization(
            @NonNull Object bean,
            @NonNull String beanName
    ) throws BeansException {
        return bean;
    }

    private void injectLogger(Field field, Object bean) {
        if (!field.isAnnotationPresent(InjectLogger.class)) {
            return;
        }

        validateField(field);
        boolean accessible = field.canAccess(bean);
        try {
            if (!accessible && !field.trySetAccessible()) {
                throw injectionFailure(field, bean, null);
            }
            if (field.get(bean) == null) {
                field.set(bean, CommonLoggerFactory.getLogger(bean.getClass()));
            }
        } catch (IllegalAccessException exception) {
            throw injectionFailure(field, bean, exception);
        } finally {
            if (!accessible) {
                field.setAccessible(false);
            }
        }
    }

    private void validateField(Field field) {
        if (field.getType() != CommonLogger.class) {
            throw new BeanInitializationException(
                    "@InjectLogger field must have type CommonLogger: " + field
            );
        }

        int modifiers = field.getModifiers();
        if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
            throw new BeanInitializationException(
                    "@InjectLogger field must not be static or final: " + field
            );
        }
    }

    private BeanInitializationException injectionFailure(
            Field field,
            Object bean,
            Exception cause
    ) {
        String message = "Failed to inject CommonLogger into field " + field.getName()
                + " on " + bean.getClass().getName();
        return cause == null
                ? new BeanInitializationException(message)
                : new BeanInitializationException(message, cause);
    }
}
