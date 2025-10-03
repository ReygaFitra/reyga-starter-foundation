package reyga.starter.foundation.core.process.node;

import java.util.function.Supplier;

public class IfContext {
    public final Supplier<Boolean> condition;
    public boolean executed = false;

    public IfContext(Supplier<Boolean> condition) {
        this.condition = condition;
    }
}
