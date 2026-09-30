package reyga.starter.foundation.common.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanInitializationException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggerInjectorTest {

    private final LoggerInjector injector = new LoggerInjector();

    @Test
    void should_InjectInheritedLoggerAndReturnSameBean_When_AnnotatedFieldsAreNull() {
        // given
        ChildBean bean = new ChildBean();

        // when
        Object beforeResult = injector.postProcessBeforeInitialization(bean, "bean");
        Object afterResult = injector.postProcessAfterInitialization(bean, "bean");

        // then
        assertSame(bean, beforeResult);
        assertSame(bean, afterResult);
        assertNotNull(bean.logger);
        assertNotNull(bean.parentLogger());
        assertNull(bean.unannotatedLogger);
    }

    @Test
    void should_PreserveExistingLogger_When_AnnotatedLoggerAlreadyHasValue() {
        // given
        ChildBean bean = new ChildBean();
        CommonLogger existing = CommonLoggerFactory.getLogger(ChildBean.class);
        bean.logger = existing;

        // when
        Object result = injector.postProcessBeforeInitialization(bean, "bean");

        // then
        assertSame(bean, result);
        assertSame(existing, bean.logger);
        assertNotNull(bean.parentLogger());
    }

    @Test
    void should_RejectAnnotatedField_When_FieldTypeIsInvalid() {
        // given
        InvalidTypeBean bean = new InvalidTypeBean();

        // when
        BeanInitializationException result = assertThrows(
                BeanInitializationException.class,
                () -> injector.postProcessBeforeInitialization(bean, "bean")
        );

        // then
        assertTrue(result.getMessage().contains("must have type CommonLogger"));
    }

    @Test
    void should_RejectAnnotatedField_When_FieldIsStaticOrFinal() {
        // when
        BeanInitializationException staticResult = assertThrows(
                BeanInitializationException.class,
                () -> injector.postProcessBeforeInitialization(new StaticFieldBean(), "bean")
        );
        BeanInitializationException finalResult = assertThrows(
                BeanInitializationException.class,
                () -> injector.postProcessBeforeInitialization(new FinalFieldBean(), "bean")
        );

        // then
        assertTrue(staticResult.getMessage().contains("must not be static or final"));
        assertTrue(finalResult.getMessage().contains("must not be static or final"));
    }

    @Test
    void should_ExposeRuntimeFieldAnnotation_When_InjectLoggerMetadataIsInspected() {
        // given
        Class<InjectLogger> annotation = InjectLogger.class;

        // when
        Retention retention = annotation.getAnnotation(Retention.class);
        Target target = annotation.getAnnotation(Target.class);

        // then
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
    }

    private static final class InvalidTypeBean {
        @InjectLogger
        private String logger;
    }

    private static final class StaticFieldBean {
        @InjectLogger
        private static CommonLogger logger;
    }

    private static final class FinalFieldBean {
        @InjectLogger
        private final CommonLogger logger = null;
    }
}
