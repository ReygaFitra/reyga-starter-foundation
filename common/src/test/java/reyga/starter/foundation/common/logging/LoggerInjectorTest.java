package reyga.starter.foundation.common.logging;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.*;

class LoggerInjectorTest {

    private final LoggerInjector injector = new LoggerInjector();

    @Test
    void should_InjectLoggerAndReturnSameBean_When_AnnotatedLoggerIsNull() {
        // Given
        ChildBean bean = new ChildBean();

        // When
        Object beforeResult = injector.postProcessBeforeInitialization(bean, "bean");
        Object afterResult = injector.postProcessAfterInitialization(bean, "bean");

        // Then
        assertSame(bean, beforeResult);
        assertSame(bean, afterResult);
        assertNotNull(bean.logger);
        assertNotNull(bean.parentLogger());
        assertNull(bean.unannotatedLogger);
        assertNull(bean.annotatedWrongType);
    }

    @Test
    void should_PreserveExistingLogger_When_AnnotatedLoggerAlreadyHasValue() {
        // Given
        ChildBean bean = new ChildBean();
        CommonLogger existing = CommonLoggerFactory.getLogger(ChildBean.class);
        bean.logger = existing;

        // When
        Object result = injector.postProcessBeforeInitialization(bean, "bean");

        // Then
        assertSame(bean, result);
        assertSame(existing, bean.logger);
        assertNotNull(bean.parentLogger());
    }

    @Test
    void should_ExposeRuntimeFieldAnnotation_When_InjectLoggerMetadataIsInspected() {
        // Given
        Class<InjectLogger> annotation = InjectLogger.class;

        // When
        Retention retention = annotation.getAnnotation(Retention.class);
        Target target = annotation.getAnnotation(Target.class);

        // Then
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
        assertArrayEquals(new ElementType[]{ElementType.FIELD}, target.value());
    }

    private static class ParentBean {
        @InjectLogger
        private CommonLogger parentLogger;

        protected CommonLogger parentLogger() {
            return parentLogger;
        }
    }

    private static final class ChildBean extends ParentBean {
        @InjectLogger
        private CommonLogger logger;
        private CommonLogger unannotatedLogger;
        @InjectLogger
        private String annotatedWrongType;
    }
}
