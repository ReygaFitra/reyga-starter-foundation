package reyga.starter.foundation.common.logging;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public abstract class BaseLogging implements ApplicationContextAware {
    protected static CustomLogger log;

    private static ApplicationContext applicationContext;

    @PostConstruct
    private void initLogger() {
        CustomLoggerFactory customLoggerFactory = applicationContext.getBean(CustomLoggerFactory.class);
        log = customLoggerFactory.getLogger();
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }
}
