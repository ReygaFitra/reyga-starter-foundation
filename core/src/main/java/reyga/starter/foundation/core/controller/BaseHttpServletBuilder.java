package reyga.starter.foundation.core.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import reyga.starter.foundation.common.model.dto.request.BaseRequest;

@RequiredArgsConstructor
public abstract class BaseHttpServletBuilder extends ResponseErrorBuilder {

    protected <T extends BaseRequest> void setHttpServlet(
            T request, HttpServletRequest servletRequest, HttpServletResponse servletResponse
    ) {
        request.setServletRequest(servletRequest);
        request.setServletResponse(servletResponse);
    }

    protected <T extends BaseRequest> HttpServletRequest getServletRequest(T request) {
        if (request.getServletRequest() == null) {
            return null;
        }

        return request.getServletRequest();
    }

    protected <T extends BaseRequest> HttpServletResponse getServletResponse(T request) {
        if (request.getServletResponse() == null) {
            return null;
        }

        return request.getServletResponse();
    }

}
