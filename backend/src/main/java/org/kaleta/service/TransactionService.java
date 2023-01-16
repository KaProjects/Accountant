package org.kaleta.service;

import org.kaleta.dto.YearTransactionDto;
import org.kaleta.entity.Account;
import org.kaleta.entity.Transaction;

import java.util.List;

public interface TransactionService {

    List<YearTransactionDto> getTransactionsMatching(String year, String debitPrefix, String creditPrefix);

    Integer[] monthlyBalanceByAccounts(String year, String debit, String credit);

    Integer[] monthlyBalanceByAccounts(String year, String debit, String credit, String description);

    Integer sumExpensesOf(List<Transaction> transactions);

    Integer getInitialValue(Account account);

    Integer[] monthlyBalanceByAccount(Account account);
}
