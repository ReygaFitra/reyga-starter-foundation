package reyga.starter.foundation.core.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import reyga.starter.foundation.common.model.dto.request.BaseRequest;

/**
 * Base class for building and managing HttpServlet objects within request DTOs.
 * Extends {@link ResponseErrorBuilder} to provide error handling capabilities.
 */
@RequiredArgsConstructor
public abstract class BaseHttpServletBuilder extends ResponseErrorBuilder {

    /**
     * Sets the HttpServletRequest and HttpServletResponse into the provided request DTO.
     *
     * @param request         the request DTO extending {@link BaseRequest}
     * @param servletRequest  the {@link HttpServletRequest} to set
     * @param servletResponse the {@link HttpServletResponse} to set
     * @param <T>             the type of the request DTO
     */
    protected <T extends BaseRequest> void setHttpServlet(
            T request, HttpServletRequest servletRequest, HttpServletResponse servletResponse
    ) {
        request.setServletRequest(servletRequest);
        request.setServletResponse(servletResponse);
    }

    /**
     * Retrieves the HttpServletRequest from the provided request DTO.
     *
     * @param request the request DTO
     * @param <T>     the type of the request DTO
     * @return the {@link HttpServletRequest}, or {@code null} if not present
     */
    protected <T extends BaseRequest> HttpServletRequest getServletRequest(T request) {
        if (request.getServletRequest() == null) {
            return null;
        }

        return request.getServletRequest();
    }

    /**
     * Retrieves the HttpServletResponse from the provided request DTO.
     *
     * @param request the request DTO
     * @param <T>     the type of the request DTO
     * @return the {@link HttpServletResponse}, or {@code null} if not present
     */
    protected <T extends BaseRequest> HttpServletResponse getServletResponse(T request) {
        if (request.getServletResponse() == null) {
            return null;
        }

        return request.getServletResponse();
    }

}
