package reyga.starter.foundation.common.logging;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
public class LoggerInjector implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, @NonNull String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                this.checkInjectLoggerInstance(field, bean);
            }
            clazz = clazz.getSuperclass();
        }
        
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, String beanName) throws BeansException {
        return bean;
    }

    private void checkInjectLoggerInstance(Field field, Object bean) {
        if (field.isAnnotationPresent(InjectLogger.class) && CommonLogger.class.isAssignableFrom(field.getType())) {
            try {
                field.setAccessible(true);
                if (field.get(bean) == null) {
                    field.set(bean, CommonLoggerFactory.getLogger(bean.getClass()));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to inject CommonLogger into " + bean.getClass().getName(), e);
            }
        }
    }
}
