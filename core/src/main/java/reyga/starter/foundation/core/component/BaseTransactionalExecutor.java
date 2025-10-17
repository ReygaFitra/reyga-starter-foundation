package reyga.starter.foundation.core.component;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import reyga.starter.foundation.common.logging.BaseLogging;

public abstract class BaseTransactionalExecutor extends BaseLogging {
    protected static ApplicationContext context;
    protected TransactionalExecutor transactionalExecutor;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        super.setApplicationContext(context);
    }
}
