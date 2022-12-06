package org.kaleta.repository;

import org.kaleta.entity.Budgeting;

import java.util.List;

public interface BudgetingRepository {

    List<Budgeting> getSchemaByIdPrefix(String year, String idPrefix);
}
