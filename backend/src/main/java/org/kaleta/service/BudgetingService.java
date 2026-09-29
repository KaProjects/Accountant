package org.kaleta.service;

import org.kaleta.entity.Transaction;
import org.kaleta.model.BudgetingData;

import java.util.List;

public interface BudgetingService
{
    /**
     * @return list of transactions for specified ID, year and month
     */
    List<Transaction> getBudgetTransactions(String year, String budgetId, String month);

    /**
     * @return budget data for specified year
     */
    BudgetingData getBudgetData(String year);
}
