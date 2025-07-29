package reyga.starter.foundation.common_database.repository;

import java.util.List;
import java.util.Optional;

public interface BaseJdbcRepository<E, ID> {
    E save(E entity);
    List<E> findAll();
    Optional<E> findById(ID id);
}
