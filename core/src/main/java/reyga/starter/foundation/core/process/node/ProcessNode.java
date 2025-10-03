package reyga.starter.foundation.core.process.node;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class ProcessNode {
    @Getter
    private final String processId;
    private final Supplier<Object> action;
    @Setter
    private Function<Exception, ?> exceptionHandler;
    @Setter
    private Runnable finalizer;
    @Setter
    private Map<String, ProcessNode> subProcesses = new LinkedHashMap<>();
    @Setter
    private IfContext ifContext;

    public ProcessNode(String processId, Supplier<Object> action) {
        this.processId = processId;
        this.action = action;
    }

    public void execute(Map<String, Object> contentService) {
        try {
            if (ifContext != null) {
                if (ifContext.condition.get() && !ifContext.executed) {
                    ifContext.executed = true;
                    for (ProcessNode sub : subProcesses.values()) {
                        sub.execute(contentService);
                    }
                }
            } else {
                Object result = action.get();
                if (result != null) {
                    contentService.put(processId, result);
                }
                for (ProcessNode sub : subProcesses.values()) {
                    sub.execute(contentService);
                }
            }
        } catch (Exception e) {
            if (exceptionHandler != null) {
                exceptionHandler.apply(e);
            } else {
                throw e;
            }
        } finally {
            if (finalizer != null) {
                finalizer.run();
            }
        }
    }
}
