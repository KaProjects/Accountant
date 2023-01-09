package org.kaleta.service;

import org.kaleta.dao.AccountDao;
import org.kaleta.entity.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountServiceImpl implements AccountService{

    @Autowired
    AccountDao accountDao;

    @Autowired
    SchemaService schemaService;

    @Override
    public Map<String, String> getAccountNamesMap(String year){
        Map<String, String> map = new HashMap<>();
        for (Account account : accountDao.list(year)){
            String fullId = account.getAccountId().getSchemaId() + "." + account.getAccountId().getSemanticId();
            String name = account.getName().equals("general")
                    ? schemaService.getAccountName(year, account.getAccountId().getSchemaId())
                    : account.getName();

            map.put(fullId, name);
        }
        return map;
    }

    @Override
    public List<Account> list(String year) {
        return accountDao.list(year);
    }
}
