package reyga.starter.foundation.core.process;

import reyga.starter.foundation.core.process.node.AbstractProcessBuilder;
import reyga.starter.foundation.core.process.node.IfContext;
import reyga.starter.foundation.core.process.node.ProcessNode;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * ProcessBuilder: Build a service process flow using builder pattern.
 */
public class ProcessBuilder extends AbstractProcessBuilder {

    private ProcessBuilder() {}

    public static ProcessBuilder start() {
        return new ProcessBuilder();
    }

    public ProcessBuilder customProcessConstruction(String processId, Function<java.util.Map<String, Object>, ?> process) {
        addNode(new ProcessNode(processId, () -> {
            Object result = process.apply(contentService);
            if (result != null) {
                contentService.put(processId, result);
            }
            return result;
        }));
        return this;
    }

    public ProcessBuilder validate(String processId, Predicate<java.util.Map<String, Object>> validator) {
        addNode(new ProcessNode(processId, () -> {
            if (!validator.test(contentService)) {
                throw new IllegalStateException("Validation failed at processId: " + processId);
            }
            return null;
        }));
        return this;
    }

    public <T> ProcessBuilder createDto(String processId, Supplier<T> supplier) {
        addNode(new ProcessNode(processId, () -> {
            T result = supplier.get();
            contentService.put(processId, result);
            return result;
        }));
        return this;
    }

    public <T> ProcessBuilder doingQuery(String processId, Supplier<T> querySupplier) {
        addNode(new ProcessNode(processId, () -> {
            T result = querySupplier.get();
            contentService.put(processId, result);
            return result;
        }));
        return this;
    }

    public <T> ProcessBuilder doLooping(String processId, Supplier<List<T>> loopSupplier, Function<T, ?> action) {
        initializeSubProcess(processId);
        addNode(new ProcessNode(processId, () -> {
            List<T> items = loopSupplier.get();
            if (items != null) {
                for (T item : items) {
                    action.apply(item);
                }
            }
            return null;
        }));
        processIdList.add(processId);
        return this;
    }

    public ProcessBuilder endLooping() {
        closeLastProcess("doLooping");
        return this;
    }

    public ProcessBuilder doWhile(String processId, Supplier<Boolean> condition, Runnable action) {
        initializeSubProcess(processId);
        addNode(new ProcessNode(processId, () -> {
            while (condition.get()) {
                action.run();
            }
            return null;
        }));
        processIdList.add(processId);
        return this;
    }

    public ProcessBuilder endWhile() {
        closeLastProcess("doWhile");
        return this;
    }

    public ProcessBuilder doIf(String processId, Supplier<Boolean> condition) {
        initializeSubProcess(processId);
        ProcessNode node = new ProcessNode(processId, () -> null);
        node.setIfContext(new IfContext(condition));
        addNode(node);
        processIdList.add(processId);
        return this;
    }

    public ProcessBuilder doElseIf(String processId, Supplier<Boolean> condition) {
        initializeSubProcess(processId);
        ProcessNode node = new ProcessNode(processId, () -> null);
        node.setIfContext(new IfContext(condition));
        addNode(node);
        processIdList.add(processId);
        return this;
    }

    public ProcessBuilder doElse(String processId) {
        initializeSubProcess(processId);
        ProcessNode node = new ProcessNode(processId, () -> null);
        node.setIfContext(new IfContext(() -> true));
        addNode(node);
        processIdList.add(processId);
        return this;
    }

    public ProcessBuilder endIf() {
        closeLastProcess("doIf / doElseIf / doElse");
        return this;
    }

    public ProcessBuilder useTry(String processId) {
        initializeSubProcess(processId);
        addNode(new ProcessNode(processId, () -> null));
        processIdList.add(processId);
        return this;
    }

    public ProcessBuilder useCatch(String processId, Function<Exception, ?> handler) {
        if (processIdList.isEmpty()) {
            throw new IllegalStateException("useCatch must be after useTry");
        }
        String tryId = processIdList.get(processIdList.size() - 1);
        ProcessNode tryNode = getNode(tryId);
        tryNode.setExceptionHandler(handler);
        return this;
    }

    public ProcessBuilder useFinally(String processId, Runnable finalizer) {
        if (processIdList.isEmpty()) {
            throw new IllegalStateException("useFinally must be after useTry");
        }
        String tryId = processIdList.get(processIdList.size() - 1);
        ProcessNode tryNode = getNode(tryId);
        tryNode.setFinalizer(finalizer);
        return this;
    }
}