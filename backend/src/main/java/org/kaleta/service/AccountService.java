package org.kaleta.service;

import org.kaleta.entity.Account;

import java.util.List;
import java.util.Map;

public interface AccountService {

    Map<String, String> getAccountNamesMap(String year);

    List<Account> list(String year);

    List<Account> listBySchemaId(String year, String schemaIdPrefix);

    /**
     * Composes full id of financial creation account of specified finance asset account.
     */
    String getFinCreationAccountId(Account account);

    /**
     * Composes full id of financial revenue revaluation account of specified finance asset account.
     */
    String getFinRevRevaluationAccountId(Account account);

    /**
     * Composes full id of financial expense revaluation account of specified finance asset account.
     */
    String getFinExpRevaluationAccountId(Account account);

    Account getAccount(String year, String fullId);
}
