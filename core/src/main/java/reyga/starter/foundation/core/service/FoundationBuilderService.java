package reyga.starter.foundation.core.service;

import reyga.starter.foundation.common.model.dto.content.BaseContent;
import reyga.starter.foundation.common.model.dto.request.BaseRequest;

public interface FoundationBuilderService<REQ extends BaseRequest, RES, CTN extends BaseContent> {

    RES execute(REQ req, CTN content);

}
