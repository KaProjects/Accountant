package org.kaleta.service;

import org.kaleta.entity.Transaction;
import org.kaleta.model.AccountingData;
import org.kaleta.model.AccountingYearlyData;

import java.util.List;
import java.util.Map;

public interface AccountingService
{
    /**
     * @return balance sheet data for specified year grouped by classId (e.i. classes 0, 1, 2, 3, 4)
     */
    Map<String, AccountingData> getBalanceData(String year);

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
     * @return closing data for all but active years (e.i. data from account 701.0 transactions)
     */
    AccountingYearlyData getYearlyClosingData();

    /**
     * @return profit data for all but active years (e.i. data from account 710.0 transactions)
     */
    AccountingYearlyData getYearlyProfitData();
}
