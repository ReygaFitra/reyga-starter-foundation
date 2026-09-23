package reyga.starter.foundation.core.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

public abstract class BaseGenericEventListener<E extends ApplicationEvent> {
    protected boolean supports(E event) {
        return false;
    }

    protected boolean supportsAsync(E event) {
        return false;
    }

    protected boolean supportsTransactional(E event) {
        return false;
    }

    protected boolean supportsAsyncTransactional(E event) {
        return false;
    }

    protected void onEvent(E event) {
    }

    protected void onEventAsync(E event) {
    }

    protected void onBeforeCommit(E event) {
    }

    protected void onAfterCommit(E event) {
    }

    protected void onAfterRollback(E event) {
    }

    protected void onAfterCompletion(E event) {
    }

    protected void onAsyncBeforeCommit(E event) {
    }

    protected void onAsyncAfterCommit(E event) {
    }

    protected void onAsyncAfterRollback(E event) {
    }

    protected void onAsyncAfterCompletion(E event) {
    }

    @EventListener(condition = "#root.target.supports(#event)")
    public void handleEvent(E event) {
        onEvent(event);
    }

    @Async
    @EventListener(condition = "#root.target.supportsAsync(#event)")
    public void handleEventAsync(E event) {
        onEventAsync(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, condition = "#root.target.supportsTransactional(#event)")
    public void handleBeforeCommit(E event) {
        onBeforeCommit(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, condition = "#root.target.supportsTransactional(#event)")
    public void handleAfterCommit(E event) {
        onAfterCommit(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK, condition = "#root.target.supportsTransactional(#event)")
    public void handleAfterRollback(E event) {
        onAfterRollback(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION, condition = "#root.target.supportsTransactional(#event)")
    public void handleAfterCompletion(E event) {
        onAfterCompletion(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, condition = "#root.target.supportsAsyncTransactional(#event)")
    public void handleAsyncBeforeCommit(E event) {
        onAsyncBeforeCommit(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, condition = "#root.target.supportsAsyncTransactional(#event)")
    public void handleAsyncAfterCommit(E event) {
        onAsyncAfterCommit(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK, condition = "#root.target.supportsAsyncTransactional(#event)")
    public void handleAsyncAfterRollback(E event) {
        onAsyncAfterRollback(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION, condition = "#root.target.supportsAsyncTransactional(#event)")
    public void handleAsyncAfterCompletion(E event) {
        onAsyncAfterCompletion(event);
    }
}
