package org.kaleta.service;

import org.kaleta.entity.Transaction;
import org.kaleta.model.AccountingData;
import org.kaleta.model.AccountingYearlyData;

import java.util.List;

public interface AccountingService
{
    /**
     * @return cash flow data for specified year
     */
    AccountingData getCashFlowData(String year);

    /**
     * @return profit's expenses data for specified year
     */
    AccountingData getProfitExpensesData(String year);

    /**
     * @return profit's revenues data for specified year
     */
    AccountingData getProfitRevenuesData(String year);

    /**
     * @return list of transactions for specified ID, year and month
     */
    List<Transaction> getSchemaTransactions(String year, String schemaId, String month);

    /**
     * @return closing data for all but active years
     */
    AccountingYearlyData getYearlyClosingData();

    /**
     * @return profit data for all but active years
     */
    AccountingYearlyData getYearlyProfitData();
}
