package reyga.starter.foundation.core.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;

@Getter
public class GenericEvent<T> extends ApplicationEvent {
    protected T event;

    public GenericEvent(Object source, T event) {
        super(source);
        this.event = event;
    }

    public GenericEvent(Object source, Clock clock, T event) {
        super(source, clock);
        this.event = event;
    }

    public GenericEvent(Object source) {
        super(source);
    }

    public GenericEvent(Object source, Clock clock) {
        super(source, clock);
    }
}
