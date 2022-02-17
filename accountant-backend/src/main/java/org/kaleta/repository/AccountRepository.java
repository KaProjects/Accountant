package org.kaleta.repository;

import org.kaleta.entity.xml.Accounts;

public interface AccountRepository {

    void syncAccounts(Accounts data);
}
