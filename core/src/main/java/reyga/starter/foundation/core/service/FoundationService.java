package reyga.starter.foundation.core.service;

import reyga.starter.foundation.core.dto.request.BaseRequest;

public interface FoundationService<REQ extends BaseRequest, RES> {

    RES execute(REQ req);

}
