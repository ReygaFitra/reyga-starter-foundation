package reyga.starter.foundation.core.service.foundation;

import reyga.starter.foundation.common.model.dto.content.BaseContent;
import reyga.starter.foundation.common.model.dto.request.BaseRequest;

public interface FoundationBuilderService<T extends BaseRequest, R, C extends BaseContent> {

    R execute(T req, C content);

}
