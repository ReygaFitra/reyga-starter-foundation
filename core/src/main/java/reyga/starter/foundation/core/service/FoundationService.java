package reyga.starter.foundation.core.service;

import reyga.starter.foundation.core.dto.content.BaseContent;
import reyga.starter.foundation.core.dto.request.BaseRequest;

public interface FoundationService<REQ extends BaseRequest, RES, CTN extends BaseContent> {

    RES execute(REQ req, CTN content);

}
