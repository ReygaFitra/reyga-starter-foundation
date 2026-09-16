package reyga.starter.foundation.core.config;

import org.junit.jupiter.api.Test;
import reyga.starter.foundation.core.exception.handler.DefaultValidationExceptionHandler;
import static org.junit.jupiter.api.Assertions.*;

class CoreConfigTest {
    @Test
    void should_ReturnValidationExceptionHandler_When_BeanIsCreated() {
        // given
        CoreConfig config = new CoreConfig();
        // when
        DefaultValidationExceptionHandler handler = config.defaultValidationExceptionHandler();
        // then
        assertNotNull(handler);
        assertEquals(DefaultValidationExceptionHandler.class, handler.getClass());
    }
}
