package org.kaleta.accountant.backend.manager;

/**
 * Provides basic CRUD operations for any entity.
 */
public interface Manager<Type> {
    /**
     * Creates object in data source.
     * @throws ManagerException if CRUD operation fails.
     */
    void create() throws ManagerException;

    /**
     * Retrieves object from data source.
     * @throws ManagerException if CRUD operation fails.
     */
    Type retrieve() throws ManagerException;

    /**
     * Updates object in data source.
     * @throws ManagerException if CRUD operation fails.
     */
    void update(Type object) throws ManagerException;

    /**
     * Deletes object from data source.
     * @throws ManagerException if CRUD operation fails.
     */
    void delete() throws ManagerException;
}