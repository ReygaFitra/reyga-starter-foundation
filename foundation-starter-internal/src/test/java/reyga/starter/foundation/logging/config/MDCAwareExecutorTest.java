package reyga.starter.foundation.logging.config;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MDCAwareExecutorTest {

    @Test
    void execute_propagatesMdcAndClearsAfter() {
        Executor delegate = command -> command.run();
        MDCAwareExecutor executor = new MDCAwareExecutor(delegate);

        MDC.put("key", "value");
        AtomicBoolean ran = new AtomicBoolean(false);

        executor.execute(() -> {
            assertEquals("value", MDC.get("key"));
            ran.set(true);
        });

        assertTrue(ran.get());
        assertEquals(null, MDC.get("key"));
    }
}
