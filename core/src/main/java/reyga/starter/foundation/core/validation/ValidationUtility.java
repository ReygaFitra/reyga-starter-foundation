package reyga.starter.foundation.core.validation;

import java.util.List;

/**
 * Injectable validation contract. Create a bean with ValidationConfig.builder() or enable
 * YAML validation. Fluent methods return the same utility, not a mutable request chain.
 * Implementations must not retain requests. Invalid constraints use the configured
 * processor's exception policy ({@code ValidationFaultException} for default behavior).
 */
public interface ValidationUtility extends AutoCloseable {
    /**
     * Validates with configured detail format and groups.
     * @param request non-null request
     * @return this utility
     * @throws NullPointerException if request is null
     * @throws IllegalStateException if this utility was closed
     */
    ValidationUtility validateRequest(Object request);

    /**
     * Validates with configured detail format and explicit groups.
     * @param request non-null request
     * @param groups non-null group interfaces; empty selects Jakarta Default
     * @return this utility
     * @throws NullPointerException if request, groups or a group is null
     * @throws IllegalArgumentException if a group is not an interface
     * @throws IllegalStateException if this utility was closed
     */
    ValidationUtility validateRequest(Object request, List<? extends Class<?>> groups);

    /**
     * Validates with explicit detail format and configured groups.
     * @param request non-null request
     * @param useMapPattern true for field maps, false for message set
     * @param <T> request type
     */
    <T> void validateRequest(T request, boolean useMapPattern);

    /**
     * Validates with explicit options without changing configured defaults.
     * @param request non-null request
     * @param useMapPattern true for field maps, false for message set
     * @param groups non-null group interfaces; empty selects Jakarta Default
     * @param <T> request type
     */
    <T> void validateRequest(T request, boolean useMapPattern, List<? extends Class<?>> groups);

    /**
     * Releases owned resources, never caller-owned validators/processors.
     * Spring infers this destroy method for beans; standalone callers use try-with-resources.
     * Closing must be idempotent and performed after validation calls finish.
     */
    @Override
    default void close() {}
}
