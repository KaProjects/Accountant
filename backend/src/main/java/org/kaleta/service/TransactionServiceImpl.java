package org.kaleta.service;

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
    private final SchemaService schemaService;

    @Autowired
    public TransactionServiceImpl(TransactionDao transactionDao, SchemaService schemaService)
    {
        this.transactionDao = transactionDao;
        this.schemaService = schemaService;
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
        return transactionDao.listClosingTransactions();
    }

    @Override
    public List<Transaction> getProfitTransactions()
    {
        return transactionDao.listProfitTransactions();
    }

    @Override
    public List<Transaction> getFinancialAssetTransactions(String year)
    {
        return transactionDao.listFinancialAssetTransactions(year);
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
