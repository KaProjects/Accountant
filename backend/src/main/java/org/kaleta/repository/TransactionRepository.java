package org.kaleta.repository;

import org.kaleta.entity.Transaction;
import org.kaleta.entity.xml.Transactions;

import java.util.List;

public interface TransactionRepository {

    void syncTransactions(Transactions data);

    /**
     * @param year
     * @param debitPrefix
     * @param creditPrefix
     *
     * @return transactions matching parameters
     *
     * Note: off-balance transactions excluded
     */
    List<Transaction> listMatching(String year, String debitPrefix, String creditPrefix);

    /**
     * @param year - year condition
     * @param debit - debit condition
     * @param credit - credit condition
     * @param description - required string in description
     * <p>
     * debit, credit inputs: exact account (e.g. 500.1 - this one account)
     *                       account prefix with % (e.g. 50% all accounts that id starts with 50)
     *                       null or empty string - all accounts
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

    Transaction getInitialTransaction(String year, String accountId, boolean isDebit);
}
