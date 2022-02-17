package org.kaleta.service;

import org.kaleta.dto.YearTransactionDto;

import java.util.List;

public interface TransactionService {


    List<YearTransactionDto> getTransactionsMatching(String year, String debitPrefix, String creditPrefix);
}
