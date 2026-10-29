package org.kaleta.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.kaleta.Utils;
import org.kaleta.persistence.api.TransactionDao;
import org.kaleta.persistence.entity.Transaction;

import java.util.List;
import java.util.Set;

@ApplicationScoped
public class TransactionService
{
    private final TransactionDao transactionDao;

    @Inject
    public TransactionService(TransactionDao transactionDao)
    {
        this.transactionDao = transactionDao;
    }

    /**
     * @return list of balance (excluding off-balance) transactions for specified year
     */
    public List<Transaction> getBalanceTransactions(String year)
    {
        return transactionDao.listByDescriptionMatching(year, "");
    }

    /**
     * @return list of transactions matching description for specified year
     */
    public List<Transaction> getTransactionsMatchingDescription(String year, String description)
    {
        return transactionDao.listByDescriptionMatching(year, description);
    }

    /**
     * @return list of transactions matching schema prefix (debit or credit) for specified year
     */
    public List<Transaction> getTransactionsMatching(String year, String schemaPrefix)
    {
        return transactionDao.list(year, schemaPrefix);
    }

    /**
     * @return list of transactions matching conditions
     * <p>
     * debit, credit inputs: exact account (e.g. 500.1 - this one account)
     *                       account prefix with % (e.g. 50% all accounts that id starts with 50)
     *                       null or empty string - all accounts
     * <p>
     * description inputs: a value that must be present in the description of transaction
     *                     a value prefixed with '!' that can't be in description
     *                     null or empty string - all descriptions
     */
    public List<Transaction> getTransactionsMatching(String year, String debit, String credit, String description)
    {
        return transactionDao.listByAccounts(year, debit, credit, description);
    }

    /**
     * @param year - year condition
     * @param schemaId- schemaId condition
     * @param month - month condition
     * <p>
     * schemaId input: exact schema ID (e.g. 2, 21, 210) for both debit/credit sides
     * <p>
     * month input: month number (e.g. 1, 2, ..., 12), use "" for all months
     *
     * @return transactions matching conditions
     *
     * Note: off-balance transactions excluded
     */
    public List<Transaction> getSchemaTransactions(String year, String schemaId, String month)
    {
        return transactionDao.listBySchema(year, schemaId, month);
    }

    /**
     * @return list of transactions for specified year filtered to contain only transactions that are interesting for budgeting (e.i. of classes 2, 4, 5, 6)
     */
    public List<Transaction> getBudgetTransactions(String year)
    {
        return transactionDao.listForClasses2456(year);
    }

    /**
     * @return list of closing transactions for all years (e.i. for account 701.0)
     */
    public List<Transaction> getClosingTransactions()
    {
        return transactionDao.listClosingBalanceTransactions();
    }

    /**
     * @return list of profit transactions for all years (e.i. for account 710.0)
     */
    public List<Transaction> getProfitTransactions()
    {
        return transactionDao.listClosingProfitTransactions();
    }

    /**
     * @return list of financial asset transactions for specified year (e.i. schema 23x and 549)
     */
    public List<Transaction> getFinancialAssetTransactions(String year)
    {
        return transactionDao.listFinancialAssetTransactions(year);
    }

    /**
     * @return monthly profit values for specified year
     */
    public Integer[] getMonthlyProfit(String year)
    {
        Integer[] monthlyProfit = Utils.initialMonthlyBalance();
        for (Transaction transaction : transactionDao.listProfitTransactions(year))
        {
            int monthIndex = Integer.parseInt(transaction.getDate().substring(2,4)) - 1;
            if (transaction.getDebit().startsWith("5") || transaction.getDebit().startsWith("6")) {
                monthlyProfit[monthIndex] -= transaction.getAmount();
            }
            if (transaction.getCredit().startsWith("5") || transaction.getCredit().startsWith("6")) {
                monthlyProfit[monthIndex] += transaction.getAmount();
            }
        }
        return monthlyProfit;
    }

    /**
     * @return all transactions matching schema prefix (e.i. for all years)
     */
    public List<Transaction> getMatching(Set<String> schemas)
    {
        return  transactionDao.listMatching(schemas);
    }
}
