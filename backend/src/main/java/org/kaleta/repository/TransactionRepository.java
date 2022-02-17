package org.kaleta.repository;

import org.kaleta.entity.Transaction;
import org.kaleta.entity.xml.Transactions;

import java.util.List;

public interface TransactionRepository {

    void syncTransactions(Transactions data);

    List<Transaction> listMatching(String year, String debitPrefix, String creditPrefix);

}
