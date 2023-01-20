package org.kaleta.repository;

import org.kaleta.entity.Budgeting;

import java.util.List;

public interface BudgetingRepository
{
    /**
     * @return budget schema with ID for specified year
     */
    List<Budgeting> getSchemaByIdPrefix(String year, String idPrefix);
}
