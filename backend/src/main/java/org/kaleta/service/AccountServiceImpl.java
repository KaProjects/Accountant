package org.kaleta.service;

import org.kaleta.Constants;
import org.kaleta.dao.AccountDao;
import org.kaleta.entity.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountServiceImpl implements AccountService
{
    @Autowired
    AccountDao accountDao;

    @Autowired
    SchemaService schemaService;

    @Override
    public Map<String, String> getAccountNamesMap(String year)
    {
        Map<String, String> map = new HashMap<>();
        for (Account account : accountDao.list(year)){
            String fullId = account.getAccountId().getSchemaId() + "." + account.getAccountId().getSemanticId();
            String name = account.getName().contains("general")
                    ? schemaService.getAccountName(year, account.getAccountId().getSchemaId())
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
    public String getFinCreationAccountId(Account account)
    {
        String id = account.getFullId();
        validateFinAssetAccount(id);
        if (Integer.parseInt(account.getAccountId().getYear()) > 2020) {
            return Constants.Schema.FIN_CREATION_ID + "." + id.charAt(2) + "-" + id.split("\\.")[1];
        } else {
            return Constants.Schema.FIN_CREATION_ID  + "." + id.split("\\.")[1];
        }
    }

    @Override
    public String getFinRevRevaluationAccountId(Account account)
    {
        String id = account.getFullId();
        validateFinAssetAccount(id);
        if (Integer.parseInt(account.getAccountId().getYear()) > 2020) {
            return Constants.Schema.FIN_REV_REVALUATION_ID + "." + id.charAt(2) + "-" + id.split("\\.")[1];
        } else {
            return Constants.Schema.FIN_REV_REVALUATION_ID  + "." + id.split("\\.")[1];
        }
    }

    @Override
    public String getFinExpRevaluationAccountId(Account account)
    {
        String id = account.getFullId();
        validateFinAssetAccount(id);
        if (Integer.parseInt(account.getAccountId().getYear()) > 2020) {
            return Constants.Schema.FIN_EXP_REVALUATION_ID + "." + id.charAt(2) + "-" + id.split("\\.")[1];
        } else {
            return Constants.Schema.FIN_EXP_REVALUATION_ID  + "." + id.split("\\.")[1];
        }
    }

    @Override
    public Account getAccount(String year, String fullId)
    {
        String[] split = fullId.split("\\.");
        return accountDao.get(year, split[0], split[1]);
    }

    public static void validateFinAssetAccount(String fullId)
    {
        if (!fullId.startsWith("23")){
            throw new IllegalArgumentException("Only 23x accounts are financial assets, but was '" + fullId + "'");
        }
        if (!fullId.contains(".")){
            throw new IllegalArgumentException("Full account id required, e.i. 'groupId.semanticId', but was '" + fullId + "'");
        }
    }
}
