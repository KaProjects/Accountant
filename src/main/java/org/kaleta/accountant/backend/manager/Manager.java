package org.kaleta.accountant.backend.manager;

/**
 * Created by Stanislav Kaleta on 16.04.2016.
 *  Provides basic CRUD operations for any entity.
 */
public interface Manager<Type> {
    /**
     * Creates object in data source.
     * @throws ManagerException
     */
    void create() throws ManagerException;

    /**
     * Retrieves object from data source.
     * @throws ManagerException
     */
    Type retrieve() throws ManagerException;

    /**
     * Updates object in data source.
     * @throws ManagerException
     */
    void update(Type object) throws ManagerException;

    /**
     * Deletes object from data source.
     * @throws ManagerException
     */
    void delete() throws ManagerException;
}