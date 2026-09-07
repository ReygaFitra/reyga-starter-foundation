package reyga.starter.foundation.core.event;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BaseGenericEventListenerTest {

    @Test
    void should_ReturnFalse_When_DefaultSupportChecksAreUsed() {
        // Given
        TrackingListener listener = new TrackingListener();
        GenericEvent<String> event = new GenericEvent<>(this, "payload");

        // When
        List<Boolean> results = List.of(
                listener.supports(event),
                listener.supportsAsync(event),
                listener.supportsTransactional(event),
                listener.supportsAsyncTransactional(event)
        );

        // Then
        assertEquals(List.of(false, false, false, false), results);
        assertTrue(listener.invocations.isEmpty());
    }

    @Test
    void should_InvokeEverySynchronousHandler_When_EntryPointsAreCalled() {
        // Given
        TrackingListener listener = new TrackingListener();
        GenericEvent<String> event = new GenericEvent<>(this, "payload");

        // When
        listener.handleEvent(event);
        listener.handleBeforeCommit(event);
        listener.handleAfterCommit(event);
        listener.handleAfterRollback(event);
        listener.handleAfterCompletion(event);

        // Then
        assertEquals(List.of(
                "event", "beforeCommit", "afterCommit", "afterRollback", "afterCompletion"
        ), listener.invocations);
        assertSame(event, listener.lastEvent);
    }

    @Test
    void should_InvokeEveryAsynchronousHandler_When_EntryPointsAreCalled() {
        // Given
        TrackingListener listener = new TrackingListener();
        GenericEvent<String> event = new GenericEvent<>(this, "payload");

        // When
        listener.handleEventAsync(event);
        listener.handleAsyncBeforeCommit(event);
        listener.handleAsyncAfterCommit(event);
        listener.handleAsyncAfterRollback(event);
        listener.handleAsyncAfterCompletion(event);

        // Then
        assertEquals(List.of(
                "eventAsync", "asyncBeforeCommit", "asyncAfterCommit", "asyncAfterRollback", "asyncAfterCompletion"
        ), listener.invocations);
        assertSame(event, listener.lastEvent);
    }

    private static final class TrackingListener extends BaseGenericEventListener<GenericEvent<String>> {
        private final List<String> invocations = new ArrayList<>();
        private GenericEvent<String> lastEvent;

        private void track(String invocation, GenericEvent<String> event) {
            invocations.add(invocation);
            lastEvent = event;
        }

        @Override protected void onEvent(GenericEvent<String> event) { track("event", event); }
        @Override protected void onEventAsync(GenericEvent<String> event) { track("eventAsync", event); }
        @Override protected void onBeforeCommit(GenericEvent<String> event) { track("beforeCommit", event); }
        @Override protected void onAfterCommit(GenericEvent<String> event) { track("afterCommit", event); }
        @Override protected void onAfterRollback(GenericEvent<String> event) { track("afterRollback", event); }
        @Override protected void onAfterCompletion(GenericEvent<String> event) { track("afterCompletion", event); }
        @Override protected void onAsyncBeforeCommit(GenericEvent<String> event) { track("asyncBeforeCommit", event); }
        @Override protected void onAsyncAfterCommit(GenericEvent<String> event) { track("asyncAfterCommit", event); }
        @Override protected void onAsyncAfterRollback(GenericEvent<String> event) { track("asyncAfterRollback", event); }
        @Override protected void onAsyncAfterCompletion(GenericEvent<String> event) { track("asyncAfterCompletion", event); }
    }
}
