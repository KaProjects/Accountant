package org.kaleta.persistence.api;

import org.kaleta.persistence.entity.Budgeting;

import java.util.List;

public interface BudgetingDao
{
    /**
     * @return budget schema for specified ID and year
     */
    Budgeting getSchemaById(String year, String id);

    /**
     * @return budget schema for specified year
     */
    List<Budgeting> getSchema(String year);
}
