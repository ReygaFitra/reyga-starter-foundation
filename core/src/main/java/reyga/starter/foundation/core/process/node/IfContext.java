package reyga.starter.foundation.core.process.node;

import java.util.function.Supplier;

public class IfContext {
    final Supplier<Boolean> condition;
    boolean executed = false;

    public IfContext(Supplier<Boolean> condition) {
        this.condition = condition;
    }
}
