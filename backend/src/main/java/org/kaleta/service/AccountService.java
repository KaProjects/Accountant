package org.kaleta.service;

import org.kaleta.entity.Account;

import java.util.List;
import java.util.Map;

public interface AccountService
{
    /**
     * @return map of names of all accounts with full account IDs as keys
     */
    Map<String, String> getAccountNamesMap(String year);

    /**
     * @return list of all accounts for specified year
     */
    List<Account> list(String year);

    /**
     * @return list of accounts for specified year which schema ID matches specified prefix
     */
    List<Account> listBySchemaId(String year, String schemaIdPrefix);

    /**
     * @return financial creation account ID of specified finance asset account
     */
    String getFinCreationAccountId(Account account);

    /**
     * @return financial revenue revaluation account ID of specified finance asset account
     */
    String getFinRevRevaluationAccountId(Account account);

    /**
     * @return financial expense revaluation account ID of specified finance asset account
     */
    String getFinExpRevaluationAccountId(Account account);

    /**
     * @return account with specified ID for specified year
     */
    Account getAccount(String year, String fullId);
}