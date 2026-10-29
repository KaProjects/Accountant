package org.kaleta.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.kaleta.AccountUtils;
import org.kaleta.model.AccountingData;
import org.kaleta.model.AccountingYearlyData;
import org.kaleta.model.SchemaClass;
import org.kaleta.persistence.entity.Account;
import org.kaleta.persistence.entity.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AccountingService
{
    private final TransactionService transactionService;
    private final SchemaService schemaService;
    private final AccountService accountService;

    @Inject
    public AccountingService(TransactionService transactionService, SchemaService schemaService, AccountService accountService)
    {
        this.transactionService = transactionService;
        this.schemaService = schemaService;
        this.accountService = accountService;
    }

    /**
     * @return balance sheet data for specified year grouped by classId (e.i. classes 0, 1, 2, 3, 4)
     */
    public Map<String, AccountingData> getBalanceData(String year)
    {
        Map<String, AccountingData> map = new HashMap<>();
        for (String classId : new String[]{"0", "1", "2", "3", "4"})
        {
            List<Transaction> transactions = transactionService.getTransactionsMatching(year, classId);
            List<Account> accounts = accountService.listBySchema(year, classId);
            SchemaClass schemaClass = schemaService.getClass(year, classId);
            map.put(classId, new AccountingData(transactions, accounts, schemaClass));
        }
        return map;
    }

    /**
     * @return cash flow data for specified year
     */
    public AccountingData getCashFlowData(String year)
    {
        List<Transaction> transactions = transactionService.getTransactionsMatching(year, "2");
        List<Account> accounts = accountService.listBySchema(year, "2");
        SchemaClass schemaClass = schemaService.getClass(year, "2");
        return new AccountingData(transactions, accounts, schemaClass);
    }

    /**
     * @return profit's expenses data for specified year
     */
    public AccountingData getProfitExpensesData(String year)
    {
        List<Transaction> transactions = transactionService.getTransactionsMatching(year, "5");
        List<Account> accounts = accountService.listBySchema(year, "5");
        SchemaClass schemaClass = schemaService.getClass(year, "5");
        return new AccountingData(transactions, accounts, schemaClass);
    }

    /**
     * @return profit's revenues data for specified year
     */
    public AccountingData getProfitRevenuesData(String year)
    {
        List<Transaction> transactions = transactionService.getTransactionsMatching(year, "6");
        List<Account> accounts = accountService.listBySchema(year, "6");
        SchemaClass schemaClass = schemaService.getClass(year, "6");
        return new AccountingData(transactions, accounts, schemaClass);
    }

    /**
     * @return list of transactions for specified ID, year and month
     */
    public List<Transaction> getSchemaTransactions(String year, String schemaId, String month)
    {
        List<Transaction> transactions = transactionService.getSchemaTransactions(year, schemaId, month);

        // filter correcting transactions between same schema account
        transactions.removeIf(transaction -> transaction.getDebit().substring(0,3).equals(transaction.getCredit().substring(0,3)));

        boolean isDebit = AccountUtils.isDebit(schemaService.getAccountType(year, schemaId));

        transactions.forEach(transaction -> {
            if ((transaction.getCredit().startsWith(schemaId) && isDebit)
                || (transaction.getDebit().startsWith(schemaId) && !isDebit))
            {
                transaction.setAmount(-transaction.getAmount());
            }
        });

        Map<String, String> accountNames = accountService.getAccountNamesMap(year);
        transactions.forEach(transaction -> {
            transaction.setDebit(accountNames.get(transaction.getDebit()));
            transaction.setCredit(accountNames.get(transaction.getCredit()));
        });

        return transactions;
    }

    /**
     * @return closing data for all but active years (e.i. data from account 701.0 transactions)
     */
    public AccountingYearlyData getYearlyClosingData()
    {
        List<Transaction> transactions = transactionService.getClosingTransactions();
        return new AccountingYearlyData(transactions, schemaService.getYears());
    }

    /**
     * @return profit data for all but active years (e.i. data from account 710.0 transactions)
     */
    public AccountingYearlyData getYearlyProfitData()
    {
        List<Transaction> transactions = transactionService.getProfitTransactions();
        return new AccountingYearlyData(transactions, schemaService.getYears());
    }
}
