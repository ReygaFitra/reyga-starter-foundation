package reyga.starter.foundation.core.service;

import reyga.starter.foundation.common.logging.BaseLogging;
import reyga.starter.foundation.core.dto.content.BaseContent;
import reyga.starter.foundation.core.dto.request.BaseRequest;

public abstract class BaseService<REQ extends BaseRequest, RES, CTN extends BaseContent> extends BaseLogging implements FoundationService<REQ, RES, CTN> {

    @Override
    public RES execute(REQ req, CTN content) {
        logInformation(req);
        return processFlow(req, content);
    }

    protected abstract RES processFlow(REQ req, CTN content);

    protected void logInformation(REQ req) {
        log.info("Executing Service...");
        log.info("Request : ", req.toString());
    }

}
