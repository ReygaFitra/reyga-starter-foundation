package reyga.starter.foundation.core.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import reyga.starter.foundation.common.logging.BaseLogging;
import reyga.starter.foundation.core.dto.request.BaseRequest;

public abstract class BaseService<REQ extends BaseRequest, RES> extends BaseLogging implements FoundationService<REQ, RES> {

    @Override
    public RES execute(REQ req) {
        logInformation(req);
        return processFlow(req);
    }

    protected abstract RES processFlow(REQ req);

    protected void logInformation(REQ req) {
        log.info("Executing Service...");
        log.info("Request : ", req.toString());
    }

    protected HttpServletRequest getHttpServletRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest servlRequest = null;

        if (requestAttributes instanceof ServletRequestAttributes) {
            servlRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        }

        return servlRequest;
    }

    protected HttpServletResponse getHttpServletResponse() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletResponse servlResponse = null;

        if (requestAttributes instanceof ServletRequestAttributes) {
            servlResponse = ((ServletRequestAttributes) requestAttributes).getResponse();
        }

        return servlResponse;
    }

}
