package reyga.starter.foundation.core.process;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ProcessBuilderTest {

    @Test
    void end_executesLoopAndStoresResults() {
        ProcessBuilder<Void> builder = ProcessBuilder.start();

        builder.createDto("items", () -> List.of(1, 2))
                .createDto("sum", AtomicInteger::new)
                .doLooping("loop", ctx -> (List<Integer>) ctx.get("items"))
                .customProcessConstruction("accumulate", ctx -> {
                    Integer item = (Integer) ctx.get("loop_item");
                    AtomicInteger sum = (AtomicInteger) ctx.get("sum");
                    sum.addAndGet(item);
                    return null;
                })
                .endLooping()
                .end();

        AtomicInteger sum = builder.getContent("sum", AtomicInteger.class);
        assertEquals(3, sum.get());
    }

    @Test
    void end_executesIfElseFlow() {
        ProcessBuilder<Void> builder = ProcessBuilder.start();

        builder.createDto("flag", () -> new ArrayList<String>())
                .doIf("ifBranch", () -> false)
                .customProcessConstruction("ifAction", ctx -> {
                    ((List<String>) ctx.get("flag")).add("if");
                    return null;
                })
                .doElse("elseBranch")
                .customProcessConstruction("elseAction", ctx -> {
                    ((List<String>) ctx.get("flag")).add("else");
                    return null;
                })
                .endIf()
                .end();

        List<String> flag = builder.getContent("flag", List.class);
        assertEquals(List.of("else"), flag);
    }

    @Test
    void end_executesElseIfWhenIfIsFalse() {
        ProcessBuilder<Void> builder = ProcessBuilder.start();

        builder.createDto("branch", () -> new ArrayList<String>())
                .doIf("ifBranch", () -> false)
                .customProcessConstruction("ifAction", ctx -> {
                    ((List<String>) ctx.get("branch")).add("if");
                    return null;
                })
                .doElseIf("elseIfBranch", () -> true)
                .customProcessConstruction("elseIfAction", ctx -> {
                    ((List<String>) ctx.get("branch")).add("elseIf");
                    return null;
                })
                .endIf()
                .end();

        List<String> branch = builder.getContent("branch", List.class);
        assertEquals(List.of("elseIf"), branch);
    }

    @Test
    void end_executesTryCatchFinally() {
        ProcessBuilder<Void> builder = ProcessBuilder.start();

        builder.createDto("flags", () -> new ArrayList<String>())
                .useTry("tryBlock")
                .customProcessConstruction("boom", ctx -> {
                    throw new IllegalStateException("fail");
                })
                .useCatch("tryBlock", ex -> {
                    ((List<String>) builder.getContent("flags")).add("catch");
                    return null;
                })
                .useFinally("tryBlock", () -> ((List<String>) builder.getContent("flags")).add("finally"))
                .endTry()
                .end();

        List<String> flags = builder.getContent("flags", List.class);
        assertEquals(List.of("catch", "finally"), flags);
    }
}
