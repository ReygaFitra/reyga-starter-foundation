package reyga.starter.foundation.common.model.dto.content;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

@Getter @Setter
@MappedSuperclass
@SuperBuilder(toBuilder = true)
@NoArgsConstructor @AllArgsConstructor
public abstract class BaseContent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Map<String, UUID> nodeId;
}
