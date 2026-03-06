package reyga.starter.foundation.core.component;

import org.springframework.beans.factory.annotation.Autowired;
import reyga.starter.foundation.common.logging.BaseLogging;

public abstract class BaseTransactionalExecutor extends BaseLogging {
    protected TransactionalExecutor transactionalExecutor;

    @Autowired(required = false)
    protected void setTransactionalExecutor(TransactionalExecutor transactionalExecutor) {
        this.transactionalExecutor = transactionalExecutor;
    }
}
