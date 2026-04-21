package reyga.starter.foundation.core.service.foundation;

import reyga.starter.foundation.common.model.dto.request.BaseRequest;

public interface FoundationService<T extends BaseRequest, R> {

    R execute(T req);

}
