package org.kaleta.service;

import org.kaleta.dto.YearTransactionDto;
import org.kaleta.entity.Transaction;

import java.util.List;

public interface TransactionService
{
    /**
     * @return list of balance (excluding off-balance) transactions for specified year
     */
    List<Transaction> getBalanceTransactions(String year);

    /**
     * @return list of transactions matching debit and credit prefixes for specified year
     */
    List<Transaction> getTransactionsMatching(String year, String debitPrefix, String creditPrefix);

    /**
     * @return list of transactions matching description for specified year
     */
    List<Transaction> getTransactionsMatchingDescription(String year, String description);

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

    /**
     * @return list of transactions for specified year filtered to contain only transactions that are interesting for budgeting (e.i. of classes 2, 4, 5, 6)
     */
    List<Transaction> getBudgetTransactions(String year);

    /**
     * @return list of closing transactions for all years (e.i. for account 701.0)
     */
    List<Transaction> getClosingTransactions();

    /**
     * @return list of profit transactions for all years (e.i. for account 710.0)
     */
    List<Transaction> getProfitTransactions();

    /**
     * @return list of financial asset transactions for specified year (e.i. schema 23x and 546)
     */
    List<Transaction> getFinancialAssetTransactions(String year);
}
