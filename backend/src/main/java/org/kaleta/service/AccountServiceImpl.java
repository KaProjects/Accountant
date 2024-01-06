package org.kaleta.service;

import org.kaleta.Constants;
import org.kaleta.dao.AccountDao;
import org.kaleta.entity.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class AccountServiceImpl implements AccountService
{
    private final AccountDao accountDao;
    private final SchemaService schemaService;

    @Autowired
    public AccountServiceImpl(AccountDao accountDao, SchemaService schemaService)
    {
        this.accountDao = accountDao;
        this.schemaService = schemaService;
    }

    @Override
    public Map<String, String> getAccountNamesMap(String year)
    {
        Map<String, String> schemaNames = schemaService.getSchemaNames(year);
        Map<String, String> map = new HashMap<>();
        for (Account account : accountDao.list(year)){
            String fullId = account.getAccountId().getSchemaId() + "." + account.getAccountId().getSemanticId();
            String name = account.getName().contains("general")
                    ? schemaNames.get(account.getAccountId().getSchemaId())
                    : account.getName();

            map.put(fullId, name);
        }
        return map;
    }

    @Override
    public List<Account> list(String year)
    {
        return accountDao.list(year);
    }

    @Override
    public List<Account> listBySchema(String year, String schemaPrefix)
    {
        return accountDao.list(year, schemaPrefix);
    }

    @Override
    public List<Account> listMatchingMetadata(String year, String metadata)
    {
        return accountDao.listByMetadata(year, metadata);
    }

    @Override
    public Map<String, List<Account>> getFinancialAssetAccounts(String year)
    {
        List<Account> finAssetAccounts = accountDao.list(year, Constants.Schema.FIN_GROUP_ID);
        Map<String, List<Account>> map = new TreeMap<>();
        for (Account account : finAssetAccounts)
        {
            String schemaId = account.getAccountId().getSchemaId();
            if (!map.containsKey(schemaId)) map.put(schemaId, new ArrayList<>());
            map.get(schemaId).add(account);
        }
        return map;
    }
}
