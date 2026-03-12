package reyga.starter.foundation.common.model.dto.request;

import jakarta.persistence.MappedSuperclass;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter @Setter
@MappedSuperclass
@SuperBuilder(toBuilder = true)
@NoArgsConstructor @AllArgsConstructor
public abstract class BaseRequest implements Serializable {

    private transient HttpServletRequest servletRequest;
    private transient HttpServletResponse servletResponse;

}
