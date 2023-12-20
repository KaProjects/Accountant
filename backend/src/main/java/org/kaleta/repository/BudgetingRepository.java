package org.kaleta.repository;

import org.kaleta.entity.Budgeting;

import java.util.List;

public interface BudgetingRepository
{
    /**
     * @return budget schema for specified ID prefix and year
     */
    List<Budgeting> getSchemaByIdPrefix(String year, String idPrefix);

    /**
     * @return budget schema for specified ID and year
     */
    Budgeting getSchemaById(String year, String id);

    /**
     * @return budget schema for specified year
     */
    List<Budgeting> getSchema(String year);
}
