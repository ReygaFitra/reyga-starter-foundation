package reyga.starter.foundation.common.model.dto.response;

import jakarta.persistence.MappedSuperclass;
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
public abstract class BaseResponse implements Serializable {
    protected String status;
    protected String code;
    protected String message;
}
