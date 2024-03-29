package org.kaleta.service;

import org.kaleta.dao.BudgetingDao;
import org.kaleta.entity.Budgeting;
import org.kaleta.entity.Transaction;
import org.kaleta.model.BudgetingData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BudgetingServiceImpl implements BudgetingService
{
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final BudgetingDao budgetingDao;

    @Autowired
    public BudgetingServiceImpl(TransactionService transactionService, AccountService accountService, BudgetingDao budgetingDao)
    {
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.budgetingDao = budgetingDao;
    }

    @Override
    public BudgetingData getBudgetData(String year)
    {
        List<Budgeting> schema = budgetingDao.getSchema(year);
        List<Transaction> transactions = transactionService.getBudgetTransactions(year);
        return new BudgetingData(schema, transactions);
    }

    @Override
    public List<Transaction> getBudgetTransactions(String year, String budgetId, String month)
    {
        Budgeting schema = budgetingDao.getSchemaById(year, budgetId);

        if (schema.getDebit() == null && schema.getCredit() == null){
            throw new IllegalArgumentException("Budget schema specified by budgetId='" + budgetId + "' doesn't have debit/credit accounts specified.");
        }

        List<Transaction> transactions = new ArrayList<>();
        if (schema.getDebit().equals(schema.getCredit())) {
            transactions.addAll(transactionService.getTransactionsMatching(year, schema.getDebit(), "", schema.getDescription()));
            List<Transaction> creditTransactions = transactionService.getTransactionsMatching(year, "", schema.getCredit(), schema.getDescription());
            creditTransactions.forEach(transaction -> transaction.setAmount(-transaction.getAmount()));
            transactions.addAll(creditTransactions);
        } else if (schema.getDescription() != null && schema.getDescription().equals("finXasset")) {
            transactions.addAll(transactionService.getTransactionsMatching(year, schema.getDebit(), "", ""));
            List<Transaction> creditTransactions = transactionService.getTransactionsMatching(year, "", schema.getCredit(), "Sale of ");
            creditTransactions.forEach(transaction -> transaction.setAmount(-transaction.getAmount()));
            transactions.addAll(creditTransactions);
        } else {
            transactions.addAll(transactionService.getTransactionsMatching(year, schema.getDebit(), schema.getCredit(), schema.getDescription()));
        }

        transactions.removeIf(transaction -> !transaction.getDate().endsWith(month.length() == 1 ? "0" + month : month));

        // filter correcting transactions between same schema account
        transactions.removeIf(transaction -> transaction.getDebit().substring(0,3).equals(transaction.getCredit().substring(0,3)));

        Map<String, String> accountNames = accountService.getAccountNamesMap(year);
        transactions.forEach(transaction -> {
            transaction.setDebit(accountNames.get(transaction.getDebit()));
            transaction.setCredit(accountNames.get(transaction.getCredit()));
        });

        return transactions;
    }
}
