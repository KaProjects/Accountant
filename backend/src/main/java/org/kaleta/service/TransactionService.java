package org.kaleta.service;

import org.kaleta.dto.YearTransactionDto;
import org.kaleta.entity.Account;
import org.kaleta.entity.Transaction;

import java.util.List;

public interface TransactionService
{
    /**
     * @return list of transactions DTO matching debit and credit prefixes for specified year
     */
    List<YearTransactionDto> getTransactionsMatching(String year, String debitPrefix, String creditPrefix);

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
     * @return monthly balance for specified account
     */
    Integer[] monthlyBalanceByAccount(Account account);
}
