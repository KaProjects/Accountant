package org.kaleta.service;

import org.kaleta.dto.YearTransactionDto;
import org.kaleta.entity.Account;
import org.kaleta.entity.Transaction;

import java.util.List;

public interface TransactionService
{
    /**
     * @return list of transactions matching debit and credit prefixes for specified year
     */
    List<Transaction> getTransactionsMatching(String year, String debitPrefix, String creditPrefix);

    /**
     * @return list of transactions matching schema prefix (debit or credit) for specified year
     */
    List<Transaction> getTransactionsMatching(String year, String schemaPrefix);

    /**
     * @return list of transactions DTO matching conditions
     * <p>
     * debit, credit inputs: exact account (e.g. 500.1 - this one account)
     *                       account prefix with % (e.g. 50% all accounts that id starts with 50)
     *                       null or empty string - all accounts
     * <p>
     * description inputs: a value that must be present in the description of transaction
     *                     a value prefixed with '!' that can't be in description
     *                     null or empty string - all descriptions
     */
    List<YearTransactionDto> getTransactionsMatching(String year, String debit, String credit, String description);

    /**
     * @return monthly balance of transactions matching conditions
     * <p>
     * debit, credit inputs: exact account (e.g. 500.1 - this one account)
     *                       account prefix with % (e.g. 50% all accounts that id starts with 50)
     *                       null or empty string - all accounts
     */
    Integer[] monthlyBalanceByAccounts(String year, String debit, String credit);

    /**
     * @return monthly balance of transactions matching conditions
     * <p>
     * debit, credit inputs: exact account (e.g. 500.1 - this one account)
     *                       account prefix with % (e.g. 50% all accounts that id starts with 50)
     *                       null or empty string - all accounts
     * <p>
     * description inputs: a value that must be present in the description of transaction
     *                     a value prefixed with '!' that can't be in description
     *                     null or empty string - all descriptions
     */
    Integer[] monthlyBalanceByAccounts(String year, String debit, String credit, String description);

    /**
     * @return sum of expense transactions
     */
    Integer sumExpensesOf(List<Transaction> transactions);

    /**
     * @return initial value of specified account
     */
    Integer getInitialValue(Account account);

    /**
     * @return cumulative monthly balance for specified account
     */
    Integer[] cumulativeMonthlyBalanceByAccount(Account account);

    /**
     * @return monthly balance for specified account
     */
    Integer[] monthlyBalanceByAccount(Account account);

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
    List<Transaction> getSchemaTransactions(String year, String schemaId, String month);
}
