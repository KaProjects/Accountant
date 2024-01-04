package org.kaleta.repository;

import org.kaleta.entity.Transaction;
import org.kaleta.entity.xml.Transactions;

import java.util.List;
import java.util.Set;

public interface TransactionRepository
{
    /**
     * Syncs transactions in database from data specified
     */
    void syncTransactions(Transactions data);

    /**
     * @param year
     * @param debitPrefix
     * @param creditPrefix
     *
     * @return transactions matching parameters
     */
    List<Transaction> list(String year, String debitPrefix, String creditPrefix);

    /**
     * @param year
     * @param schemaPrefix
     *
     * @return transactions matching schema prefix for both debit or credit and year
     */
    List<Transaction> list(String year, String schemaPrefix);

    /**
     * @param year - year condition
     * @param debit - debit condition
     * @param credit - credit condition
     * @param description - required string in description
     * <p>
     * debit, credit inputs: exact account (e.g. 500.1 - this one account)
     *                       account prefix with % (e.g. 50% all accounts that id starts with 50)
     *                       null or empty string - all accounts
     * <p>
     * description input: a value that must be present in the description of transaction
     *                     a value prefixed with '!' that can't be in description
     *                     null or empty string - all descriptions
     *
     * @return transactions matching conditions
     *
     * Note: off-balance transactions excluded
     */
    List<Transaction> listByAccounts(String year, String debit, String credit, String description);

    /**
     * @param year year condition
     * @param account account condition (is either debit or credit in a transaction)
     *
     * @return transactions matching conditions
     *
     * Note: off-balance transactions excluded
     */
    List<Transaction> listByAccount(String year, String account);

    /**
     * @param year year condition
     * @param descriptionSubString description substring condition
     *
     * @return transactions matching conditions
     *
     * Note: off-balance transactions excluded
     */
    List<Transaction> listByDescriptionMatching(String year, String descriptionSubString);

    /**
     * @param isDebit whether account is debit-like, false for credit-like
     * @return initial transaction for specified account ID
     */
    Transaction getInitialTransaction(String year, String accountId, boolean isDebit);

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
    List<Transaction> listBySchema(String year, String schemaId, String month);

    /**
     * @return list of transactions that have debit or credit of classes 2, 4, 5 or 6.
     *
     * Note: off-balance transactions excluded
     */
    List<Transaction> listForClasses2456(String year);

    /**
     * @return list of transactions that have debit or credit of closing balance account ID (701.0).
     */
    List<Transaction> listClosingBalanceTransactions();

    /**
     * @return list of transactions that have debit or credit of closing profit account ID (710.0).
     */
    List<Transaction> listClosingProfitTransactions();

    /**
     * @return list of transactions that have debit or credit of profit account ID (e.i. 5x or 6x).
     *
     * Note: off-balance transactions excluded
     */
    List<Transaction> listProfitTransactions(String year);

    /**
     * @return list of financial asset transactions for specified year (e.i. schema 23x and 546)
     */
    List<Transaction> listFinancialAssetTransactions(String year);

    /**
     * @return list of all transactions for specified year
     */
    List<Transaction> list(String year);

    /**
     * @return all transactions matching schema prefix (e.i. for all years)
     */
    List<Transaction> listMatching(Set<String> schemas);
}
