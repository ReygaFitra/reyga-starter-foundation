package reyga.starter.foundation.core.process;

import reyga.starter.foundation.core.process.node.AbstractProcessBuilder;
import reyga.starter.foundation.core.process.node.IfContext;
import reyga.starter.foundation.core.process.node.ProcessNode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * ProcessBuilder: Build a service process flow using builder pattern.
 */
public class ProcessBuilder<T> extends AbstractProcessBuilder {

    private final Deque<AtomicBoolean> ifExecutionStack = new ArrayDeque<>();
    private final Deque<String> ifBranchStack = new ArrayDeque<>();

    private ProcessBuilder() {}

    public static <T> ProcessBuilder<T> start() {
        return new ProcessBuilder<>();
    }

    public ProcessBuilder<T> customProcessConstruction(String processId, Function<Map<String, Object>, ?> process) {
        addNode(new ProcessNode(processId, () -> {
            return process.apply(contentService);
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
            return supplier.get();
        }));
        return this;
    }

    public <R> ProcessBuilder<T> doingQuery(String processId, Supplier<R> querySupplier) {
        addNode(new ProcessNode(processId, () -> {
            return querySupplier.get();
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
        AtomicBoolean executed = new AtomicBoolean(false);
        node.setIfContext(new IfContext(condition, executed));
        addNode(node);
        processIdStack.add(processId);
        ifExecutionStack.push(executed);
        ifBranchStack.push(processId);
        return this;
    }

    public ProcessBuilder<T> doElseIf(String processId, Supplier<Boolean> condition) {
        if (ifExecutionStack.isEmpty()) {
            throw new IllegalStateException("doElseIf must be after doIf");
        }
        if (ifBranchStack.isEmpty()) {
            throw new IllegalStateException("doElseIf without matching doIf");
        }
        if (processIdStack.isEmpty()) {
            throw new IllegalStateException("doElseIf without an active if branch");
        }
        String currentBranch = processIdStack.get(processIdStack.size() - 1);
        if (!currentBranch.equals(ifBranchStack.peek())) {
            throw new IllegalStateException("doElseIf must be after closing the current if branch");
        }
        closeLastProcess("doIf/doElseIf/doElse");
        initializeSubProcess(processId);
        ProcessNode node = new ProcessNode(processId, () -> null);
        node.setIfContext(new IfContext(condition, ifExecutionStack.peek()));
        addNode(node);
        processIdStack.add(processId);
        ifBranchStack.pop();
        ifBranchStack.push(processId);
        return this;
    }

    public ProcessBuilder<T> doElse(String processId) {
        if (ifExecutionStack.isEmpty()) {
            throw new IllegalStateException("doElse must be after doIf");
        }
        if (ifBranchStack.isEmpty()) {
            throw new IllegalStateException("doElse without matching doIf");
        }
        if (processIdStack.isEmpty()) {
            throw new IllegalStateException("doElse without an active if branch");
        }
        String currentBranch = processIdStack.get(processIdStack.size() - 1);
        if (!currentBranch.equals(ifBranchStack.peek())) {
            throw new IllegalStateException("doElse must be after closing the current if branch");
        }
        closeLastProcess("doIf/doElseIf/doElse");
        initializeSubProcess(processId);
        ProcessNode node = new ProcessNode(processId, () -> null);
        node.setIfContext(new IfContext(() -> true, ifExecutionStack.peek()));
        addNode(node);
        processIdStack.add(processId);
        ifBranchStack.pop();
        ifBranchStack.push(processId);
        return this;
    }

    public ProcessBuilder<T> endIf() {
        if (ifExecutionStack.isEmpty()) {
            throw new IllegalStateException("endIf without matching doIf");
        }
        if (ifBranchStack.isEmpty()) {
            throw new IllegalStateException("endIf without matching doIf");
        }
        if (processIdStack.isEmpty()) {
            throw new IllegalStateException("endIf without an active if branch");
        }
        String currentBranch = processIdStack.get(processIdStack.size() - 1);
        if (!currentBranch.equals(ifBranchStack.peek())) {
            throw new IllegalStateException("endIf must close the current if branch");
        }
        closeLastProcess("doIf/doElseIf/doElse");
        ifExecutionStack.pop();
        ifBranchStack.pop();
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
        if (!tryId.equals(processId)) {
            throw new IllegalStateException("useCatch must target the current try block: " + tryId);
        }
        ProcessNode tryNode = getNode(tryId);
        tryNode.setExceptionHandler(handler);
        return this;
    }

    public ProcessBuilder<T> useFinally(String processId, Runnable finalizer) {
        if (processIdStack.isEmpty()) {
            throw new IllegalStateException("useFinally must be after useTry");
        }
        String tryId = processIdStack.get(processIdStack.size() - 1);
        if (!tryId.equals(processId)) {
            throw new IllegalStateException("useFinally must target the current try block: " + tryId);
        }
        ProcessNode tryNode = getNode(tryId);
        tryNode.setFinalizer(finalizer);
        return this;
    }

    public ProcessBuilder<T> endTry() {
        closeLastProcess("useTry");
        return this;
    }

    public void end() {
        for (ProcessNode node : mainProcesses.values()) {
            node.execute(contentService);
        }
    }

}
