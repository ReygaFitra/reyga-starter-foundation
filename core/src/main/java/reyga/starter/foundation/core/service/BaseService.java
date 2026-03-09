package reyga.starter.foundation.core.service;

import reyga.starter.foundation.common.logging.BaseLogging;
import reyga.starter.foundation.core.dto.request.BaseRequest;

public abstract class BaseService<T extends BaseRequest, R> extends BaseLogging implements FoundationService<T, R> {

    @Override
    public R execute(T req) {
        logInformation(req);
        return processFlow(req);
    }

    protected abstract R processFlow(T req);

    protected void logInformation(T req) {
        log.info("Executing Service...");
        log.info("Request : ", req.toString());
    }

}
