package org.kaleta.service;

import org.kaleta.Utils;
import org.kaleta.dao.TransactionDao;
import org.kaleta.dto.YearTransactionDto;
import org.kaleta.entity.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService
{
    private final TransactionDao transactionDao;

    @Autowired
    public TransactionServiceImpl(TransactionDao transactionDao)
    {
        this.transactionDao = transactionDao;
    }

    @Override
    public List<Transaction> getBalanceTransactions(String year)
    {
        return transactionDao.listByDescriptionMatching(year, "");
    }

    @Override
    public List<Transaction> getTransactionsMatching(String year, String debitPrefix, String creditPrefix)
    {
        return transactionDao.list(year, debitPrefix, creditPrefix);
    }

    @Override
    public List<Transaction> getTransactionsMatchingDescription(String year, String description)
    {
        return transactionDao.listByDescriptionMatching(year, description);
    }

    @Override
    public List<Transaction> getTransactionsMatching(String year, String schemaPrefix)
    {
        return transactionDao.list(year, schemaPrefix);
    }

    @Override
    public List<YearTransactionDto> getTransactionsMatching(String year, String debit, String credit, String description)
    {
        return mapYearTransactions(transactionDao.listByAccounts(year, debit, credit, description));
    }

    @Override
    public List<Transaction> getSchemaTransactions(String year, String schemaId, String month)
    {
        return transactionDao.listBySchema(year, schemaId, month);
    }

    @Override
    public List<Transaction> getBudgetTransactions(String year)
    {
        return transactionDao.listForClasses2456(year);
    }

    @Override
    public List<Transaction> getClosingTransactions()
    {
        return transactionDao.listClosingBalanceTransactions();
    }

    @Override
    public List<Transaction> getProfitTransactions()
    {
        return transactionDao.listClosingProfitTransactions();
    }

    @Override
    public List<Transaction> getFinancialAssetTransactions(String year)
    {
        return transactionDao.listFinancialAssetTransactions(year);
    }

    @Override
    public Integer[] getMonthlyProfit(String year)
    {
        Integer[] monthlyProfit = Utils.initialMonthlyBalance();
        for (Transaction transaction : transactionDao.listProfitTransactions(year))
        {
            int monthIndex = Integer.parseInt(transaction.getDate().substring(2,4)) - 1;
            if (transaction.getDebit().startsWith("5") || transaction.getDebit().startsWith("6")) {
                monthlyProfit[monthIndex] -= transaction.getAmount();
            }
            if (transaction.getCredit().startsWith("5") || transaction.getCredit().startsWith("6")) {
                monthlyProfit[monthIndex] += transaction.getAmount();
            }
        }
        return monthlyProfit;
    }

    private List<YearTransactionDto> mapYearTransactions(List<Transaction> transactions)
    {
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
}
