package org.kaleta.service;

import org.kaleta.dto.YearTransactionDto;
import org.kaleta.model.BudgetComponent;

import java.util.List;

public interface BudgetingService
{
    /**
     * @return named budget component for specified ID prefix and year
     */
    BudgetComponent getBudgetComponent(String year, String name, String idPrefix);

    /**
     * @return list of transactions for specified ID, year and month
     */
    List<YearTransactionDto> getBudgetTransactions(String year, String budgetId, String month);
}
