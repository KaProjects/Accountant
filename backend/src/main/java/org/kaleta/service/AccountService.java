package org.kaleta.service;

import org.kaleta.entity.Account;

import java.util.List;
import java.util.Map;

public interface AccountService
{
    /**
     * @return map of names of all accounts with full account IDs as keys
     *
     * Note: schema account names are used instead of 'general' or 'general of...' account names
     */
    Map<String, String> getAccountNamesMap(String year);

    /**
     * @return list of all accounts for specified year
     */
    List<Account> list(String year);

    /**
     * @return list of accounts matching schema prefix and year
     */
    List<Account> listBySchema(String year, String schemaPrefix);

    /**
     * @return list of accounts matching metadata for specified year
     */
    List<Account> listMatchingMetadata(String year, String metadata);

    /**
     * @return map of financial asset accounts grouped by schema name for specified year
     */
    Map<String, List<Account>> getFinancialAssetAccounts(String year);
}
