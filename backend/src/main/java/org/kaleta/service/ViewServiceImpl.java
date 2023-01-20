package org.kaleta.service;

import org.kaleta.dao.TransactionDao;
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
    TransactionDao transactionDao;

    @Override
    public Map<String, List<Transaction>> getVacationMap(String year)
    {
        List<Transaction> vacationTransactions = transactionDao.listByDescriptionMatching(year,"vac=");
        Map<String, List<Transaction>> map = new HashMap<>();

        for (Transaction transaction : vacationTransactions){
            String key = extractVacationKey(transaction.getDescription());
            if (!map.containsKey(key)){
                map.put(key, new ArrayList<>());
            }
            map.get(key).add(transaction);
        }

        return map;
    }

    private String extractVacationKey(String description)
    {
        for (String split : description.split(" ")) {
            if (split.startsWith("vac=")){
                return split.substring(4);
            }
        }
        throw new IllegalArgumentException("Couldn't find 'vac=' in '" + description + "'");
    }
}
