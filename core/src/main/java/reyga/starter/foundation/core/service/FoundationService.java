package reyga.starter.foundation.core.service;

import reyga.starter.foundation.core.dto.request.BaseRequest;

public interface FoundationService<T extends BaseRequest, R> {

    R execute(T req);

}
