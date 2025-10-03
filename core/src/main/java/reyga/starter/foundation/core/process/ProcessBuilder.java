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
public class ProcessBuilder<T> extends AbstractProcessBuilder {

    private final Map<String, Object> contentService = new HashMap<>();
    private final List<String> processIdStack = new ArrayList<>();
    private final Map<String, LinkedHashMap<String, ProcessNode>> subProcessMap = new HashMap<>();

    private final LinkedHashMap<String, ProcessNode> mainProcesses = new LinkedHashMap<>();

    private ProcessBuilder() {}

    public static <T> ProcessBuilder<T> start() {
        return new ProcessBuilder<>();
    }

    public ProcessBuilder<T> customProcessConstruction(String processId, Function<Map<String, Object>, ?> process) {
        addNode(new ProcessNode(processId, () -> {
            Object result = process.apply(contentService);
            if (result != null) {
                contentService.put(processId, result);
            }
            return result;
        }));
        return this;
    }

    public ProcessBuilder<T> validate(String processId, Predicate<Map<String, Object>> validator) {
        addNode(new ProcessNode(processId, () -> {
            if (!validator.test(contentService)) {
                throw new IllegalStateException("Validation failed at processId: " + processId);
            }
            return null;
        }));
        return this;
    }

    public <R> ProcessBuilder<T> createDto(String processId, Supplier<R> supplier) {
        addNode(new ProcessNode(processId, () -> {
            R result = supplier.get();
            contentService.put(processId, result);
            return result;
        }));
        return this;
    }

    public <R> ProcessBuilder<T> doingQuery(String processId, Supplier<R> querySupplier) {
        addNode(new ProcessNode(processId, () -> {
            R result = querySupplier.get();
            contentService.put(processId, result);
            return result;
        }));
        return this;
    }

    public <E> ProcessBuilder<T> doLooping(String processId, Function<Map<String, Object>, List<E>> loopSupplier) {
        initializeSubProcess(processId);
        addNode(new ProcessNode(processId, () -> {
            List<E> items = loopSupplier.apply(contentService);
            if (items != null) {
                for (E item : items) {
                    contentService.put(processId + "_item", item);
                    executeSubProcesses(processId);
                }
                contentService.remove(processId + "_item");
            }
            return null;
        }));
        processIdStack.add(processId);
        return this;
    }

    public ProcessBuilder<T> endLooping() {
        closeLastProcess("doLooping");
        return this;
    }

    public ProcessBuilder<T> doWhile(String processId, Supplier<Boolean> condition) {
        initializeSubProcess(processId);
        addNode(new ProcessNode(processId, () -> {
            while (condition.get()) {
                executeSubProcesses(processId);
            }
            return null;
        }));
        processIdStack.add(processId);
        return this;
    }

    public ProcessBuilder<T> endWhile() {
        closeLastProcess("doWhile");
        return this;
    }

    public ProcessBuilder<T> doIf(String processId, Supplier<Boolean> condition) {
        initializeSubProcess(processId);
        ProcessNode node = new ProcessNode(processId, () -> null);
        node.setIfContext(new IfContext(condition));
        addNode(node);
        processIdStack.add(processId);
        return this;
    }

    public ProcessBuilder<T> doElseIf(String processId, Supplier<Boolean> condition) {
        initializeSubProcess(processId);
        ProcessNode node = new ProcessNode(processId, () -> null);
        node.setIfContext(new IfContext(condition));
        addNode(node);
        processIdStack.add(processId);
        return this;
    }

    public ProcessBuilder<T> doElse(String processId) {
        initializeSubProcess(processId);
        ProcessNode node = new ProcessNode(processId, () -> null);
        node.setIfContext(new IfContext(() -> true));
        addNode(node);
        processIdStack.add(processId);
        return this;
    }

    public ProcessBuilder<T> endIf() {
        closeLastProcess("doIf/doElseIf/doElse");
        return this;
    }

    public ProcessBuilder<T> useTry(String processId) {
        initializeSubProcess(processId);
        addNode(new ProcessNode(processId, () -> null));
        processIdStack.add(processId);
        return this;
    }

    public ProcessBuilder<T> useCatch(String processId, Function<Exception, ?> handler) {
        if (processIdStack.isEmpty()) {
            throw new IllegalStateException("useCatch must be after useTry");
        }
        String tryId = processIdStack.get(processIdStack.size() - 1);
        ProcessNode tryNode = getNode(tryId);
        tryNode.setExceptionHandler(handler);
        return this;
    }

    public ProcessBuilder<T> useFinally(String processId, Runnable finalizer) {
        if (processIdStack.isEmpty()) {
            throw new IllegalStateException("useFinally must be after useTry");
        }
        String tryId = processIdStack.get(processIdStack.size() - 1);
        ProcessNode tryNode = getNode(tryId);
        tryNode.setFinalizer(finalizer);
        return this;
    }

    public void end() {
        for (ProcessNode node : mainProcesses.values()) {
            node.execute(contentService);
        }
    }

}
