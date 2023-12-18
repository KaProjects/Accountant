package org.kaleta.repository;

import org.kaleta.entity.Account;
import org.kaleta.entity.xml.Accounts;

import java.util.List;

public interface AccountRepository
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
     * @return get account specified by schema ID, semantic ID and year
     */
    Account get(String year, String schemaId, String semanticId);
}
