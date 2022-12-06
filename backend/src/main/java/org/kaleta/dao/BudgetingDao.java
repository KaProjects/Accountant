package org.kaleta.dao;

import org.kaleta.entity.Budgeting;
import org.kaleta.repository.BudgetingRepository;
import org.springframework.data.repository.CrudRepository;

/**
 * Only to group CrudRepository and BudgetingRepository.
 */
public interface BudgetingDao extends CrudRepository<Budgeting, String>, BudgetingRepository {
}
