package org.kaleta.service;

import org.kaleta.entity.Account;
import org.kaleta.entity.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ViewServiceImpl implements ViewService
{
    @Autowired
    TransactionService transactionService;

    @Autowired
    AccountService accountService;

    @Override
    public Map<String, List<Transaction>> getVacationMap(String year)
    {
        List<Transaction> vacationTransactions = transactionService.getTransactionsMatchingDescription(year,"vac=");
        Map<String, List<Transaction>> map = new HashMap<>();

        for (Transaction transaction : vacationTransactions){
            String key = extractKey(transaction.getDescription(), "vac=");
            if (!map.containsKey(key)){
                map.put(key, new ArrayList<>());
            }
            map.get(key).add(transaction);
        }

        return map;
    }

    @Override
    public Map<String, List<Transaction>> getViewMap(String year)
    {
        List<Transaction> allTransactions = transactionService.getBalanceTransactions(year);
        List<Account> viewAccounts = accountService.listMatchingMetadata(year, "view=");

        Map<String, List<Transaction>> map = new HashMap<>();

        for (Transaction transaction : allTransactions){
            String key = null;
            if (transaction.getDescription().contains("view="))
            {
                key = extractKey(transaction.getDescription(), "view=");
            }
            for (Account account : viewAccounts)
            {
                if (transaction.getDebit().equals(account.getFullId()) || transaction.getCredit().equals(account.getFullId()))
                {
                    key = extractKey(account.getMetadata(), "view=");
                }
            }
            if (key != null){
                if (!map.containsKey(key)){
                    map.put(key, new ArrayList<>());
                }
                map.get(key).add(transaction);
            }
        }
        return map;
    }

    private String extractKey(String description, String key)
    {
        for (String split : description.split(" ")) {
            if (split.startsWith(key)){
                return split.substring(key.length());
            }
        }
        throw new IllegalArgumentException("Couldn't find '" + key + "' in '" + description + "'");
    }
}
