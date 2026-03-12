package reyga.starter.foundation.core.dto.content;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

@Getter @Setter
@MappedSuperclass
@SuperBuilder(toBuilder = true)
@NoArgsConstructor @AllArgsConstructor
public abstract class BaseContent implements Serializable {
    protected transient Map<String, UUID> nodeId;
}
