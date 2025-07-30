package reyga.starter.foundation.core.service;

import reyga.starter.foundation.common.model.dto.request.BaseRequest;

public interface FoundationService<REQ extends BaseRequest, RES> {

    RES execute(REQ req);

}
