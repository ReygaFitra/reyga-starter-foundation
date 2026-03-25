package reyga.starter.foundation.core.process.node;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class IfContext {
    public final Supplier<Boolean> condition;
    public final AtomicBoolean executed;

    public IfContext(Supplier<Boolean> condition, AtomicBoolean executed) {
        this.condition = condition;
        this.executed = executed;
    }
}
