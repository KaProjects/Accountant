package org.kaleta.persistence.api;

import org.kaleta.datasource.Accounts;
import org.kaleta.persistence.entity.Account;

import java.util.List;

public interface AccountDao
{
    /**
     * Syncs accounts in database from data specified
     */
    void syncAccounts(Accounts data);

    /**
     * @return list of all account for specified year
     */
    List<Account> list(String year);

    /**
     * @return list of accounts matching schema prefix and year
     */
    List<Account> list(String year, String schemaPrefix);

    /**
     * @return list of account matching metadata for specified year
     */
    List<Account> listByMetadata(String year, String metadata);
}
