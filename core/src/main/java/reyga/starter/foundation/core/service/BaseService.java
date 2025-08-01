package reyga.starter.foundation.core.service;

import reyga.starter.foundation.common.logging.BaseLogging;
import reyga.starter.foundation.core.dto.request.BaseRequest;

public abstract class BaseService<REQ extends BaseRequest, RES> extends BaseLogging implements FoundationService<REQ, RES> {

    @Override
    public RES execute(REQ req) {
        validateRequest(req);
        return process(req);
    }

    protected void validateRequest(REQ req) {};

    protected abstract RES process(REQ req);
}
