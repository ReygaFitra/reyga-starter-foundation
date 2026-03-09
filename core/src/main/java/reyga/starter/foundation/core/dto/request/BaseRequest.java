package reyga.starter.foundation.core.dto.request;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public abstract class BaseRequest implements Serializable {

    private transient HttpServletRequest servletRequest;
    private transient HttpServletResponse servletResponse;

}
