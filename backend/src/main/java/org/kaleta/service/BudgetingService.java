package org.kaleta.service;

import org.kaleta.dto.YearTransactionDto;
import org.kaleta.model.BudgetingData;

import java.util.List;

public interface BudgetingService
{
    /**
     * @return list of transactions for specified ID, year and month
     */
    List<YearTransactionDto> getBudgetTransactions(String year, String budgetId, String month);

    /**
     * @return budget data for specified year
     */
    BudgetingData getBudgetData(String year);
}
