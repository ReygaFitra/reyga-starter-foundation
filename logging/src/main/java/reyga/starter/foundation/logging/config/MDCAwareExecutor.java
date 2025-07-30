package reyga.starter.foundation.logging.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
public class MDCAwareExecutor implements Executor {

    private final Executor delegate;

    @Override
    public void execute(@NonNull Runnable command) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        delegate.execute(() -> {
            try {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                command.run();
            } finally {
                MDC.clear();
            }
        });
    }
}
