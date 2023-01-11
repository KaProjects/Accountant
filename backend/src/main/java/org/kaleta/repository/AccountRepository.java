package org.kaleta.repository;

import org.kaleta.entity.Account;
import org.kaleta.entity.xml.Accounts;

import java.util.List;

public interface AccountRepository {

    void syncAccounts(Accounts data);

    List<Account> list(String year);

    List<Account> list(String year, String schemaIdPrefix);

    Account get(String year, String schemaId, String semanticId);
}
