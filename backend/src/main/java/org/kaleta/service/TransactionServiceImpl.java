package org.kaleta.service;

import org.kaleta.dao.TransactionDao;
import org.kaleta.dto.YearTransactionDto;
import org.kaleta.entity.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    TransactionDao transactionDao;

    @Override
    public List<YearTransactionDto> getTransactionsMatching(String year, String debitPrefix, String creditPrefix) {
        return mapYearTransactions(transactionDao.listMatching(year, debitPrefix, creditPrefix));
    }

    private List<YearTransactionDto> mapYearTransactions(List<Transaction> transactions) {
        List<YearTransactionDto> output = new ArrayList<>();
        for (Transaction transaction : transactions) {
            YearTransactionDto dto = new YearTransactionDto();
            dto.setDate(transaction.getDate());
            dto.setDescription(transaction.getDescription());
            dto.setAmount(String.valueOf(transaction.getAmount()));
            dto.setDebit(transaction.getDebit());
            dto.setCredit(transaction.getCredit());
            output.add(dto);
        }
        return output;
    }

    public Integer[] monthlyBalanceByAccounts(String year, String debit, String credit){
        return monthlyBalanceByAccounts(year, debit, credit, "");
    }
    @Override
    public Integer[] monthlyBalanceByAccounts(String year, String debit, String credit, String description) {
        List<Transaction> transactions = transactionDao.listByAccounts(year, debit, credit, description);
        Integer[] monthlyBalance = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (Transaction tr : transactions) {
            int month = Integer.parseInt(tr.getDate().substring(2, 4));
            monthlyBalance[month - 1] += tr.getAmount();
        }

        return monthlyBalance;
    }
}
