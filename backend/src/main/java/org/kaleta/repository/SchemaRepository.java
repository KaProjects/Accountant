package org.kaleta.repository;

import org.kaleta.entity.Schema;

import java.util.List;

public interface SchemaRepository
{
    /**
     * Syncs schema in database from data specified
     */
    void syncSchema(org.kaleta.entity.xml.Schema data);

    /**
     * @return account specified by ID
     */
    Schema getAccountById(String year, String accountId);

    /**
     * @return list of all schema objects for specified year
     */
    List<Schema> list(String year);

    /**
     * @return list of all schema objects matching id prefix and year
     */
    List<Schema> list(String year, String idPrefix);

    /**
     * @return list of all schema objects for the latest year
     */
    List<Schema> listLatest();

    /**
     * @return all years from data
     */
    List<String> getYears();
}
