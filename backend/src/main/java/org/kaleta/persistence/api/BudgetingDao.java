package org.kaleta.persistence.api;

import org.kaleta.persistence.entity.Budgeting;

import java.util.List;

public interface BudgetingDao
{
    /**
     * @return budget schema for specified year, in the order of the rows' ids
     */
    List<Budgeting> getSchema(String year);

    /** Replaces every row of the specified year's budget schema with the given ones. */
    void replaceSchema(String year, List<Budgeting> rows);
}
