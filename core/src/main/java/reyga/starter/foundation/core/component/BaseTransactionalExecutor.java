package reyga.starter.foundation.core.component;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseTransactionalExecutor {
    protected TransactionalExecutor transactionalExecutor;

    @Autowired(required = false)
    protected void setTransactionalExecutor(TransactionalExecutor transactionalExecutor) {
        this.transactionalExecutor = transactionalExecutor;
    }
}
