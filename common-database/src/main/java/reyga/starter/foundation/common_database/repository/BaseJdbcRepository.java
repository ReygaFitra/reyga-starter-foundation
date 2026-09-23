package reyga.starter.foundation.common_database.repository;

import java.util.List;
import java.util.Optional;

/**
 * Minimal persistence contract for JDBC repositories.
 *
 * <p>Applications should declare a domain-specific repository interface that extends
 * this contract, then provide an implementation backed by {@code QueryProcessor} and
 * {@code QueryBuilder}. Keeping this interface free of JDBC types prevents persistence
 * details from leaking into the service layer.</p>
 *
 * @param <E> aggregate or entity type
 * @param <I> identifier type
 */
public interface BaseJdbcRepository<E, I> {

    /**
     * Persists an entity according to the implementation's insert or update policy.
     *
     * @param entity entity to persist
     * @return persisted entity
     */
    E save(E entity);

    /**
     * Returns every entity visible to the repository.
     *
     * @return all entities, never {@code null}
     */
    List<E> findAll();

    /**
     * Finds an entity by its identifier.
     *
     * @param id entity identifier
     * @return matching entity, or an empty optional
     */
    Optional<E> findById(I id);
}
