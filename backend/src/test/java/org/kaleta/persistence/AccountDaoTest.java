package org.kaleta.persistence;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.kaleta.datasource.Accounts;
import org.kaleta.persistence.api.AccountDao;
import org.kaleta.persistence.entity.Account;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.kaleta.framework.Generator.account;

/**
 * The account queries, on accounts written into year 1999, where no fixture is, and rolled back
 * after each test.
 */
@QuarkusTest
class AccountDaoTest
{
    private static final String YEAR = "1999";

    @Inject
    EntityManager entityManager;

    @Inject
    AccountDao accountDao;

    private void given(Account... accounts)
    {
        for (Account account : accounts) entityManager.persist(account);
        entityManager.flush();
    }

    private static List<String> idsOf(List<Account> accounts)
    {
        return accounts.stream().map(Account::getFullId).toList();
    }

    @Test
    @TestTransaction
    void list_answersTheAccountsOfTheYearOnly()
    {
        given(account(YEAR, "210.0"), account(YEAR, "510.1"));

        assertThat(idsOf(accountDao.list(YEAR)), containsInAnyOrder("210.0", "510.1"));
        assertThat(accountDao.list("1998"), is(empty()));
    }

    @Test
    @TestTransaction
    void list_matchesASchemaPrefix()
    {
        given(account(YEAR, "210.0"), account(YEAR, "211.0"), account(YEAR, "220.0"), account(YEAR, "510.0"));

        assertThat(idsOf(accountDao.list(YEAR, "21")), containsInAnyOrder("210.0", "211.0"));
        assertThat(idsOf(accountDao.list(YEAR, "2")), containsInAnyOrder("210.0", "211.0", "220.0"));
        assertThat(idsOf(accountDao.list(YEAR, "220")), containsInAnyOrder("220.0"));
    }

    @Test
    @TestTransaction
    void listByMetadata_findsTheMetadataAnywhereInIt()
    {
        given(account(YEAR, "510.0", "fuel", "fin=x view=car"), account(YEAR, "510.1", "food", ""));

        assertThat(idsOf(accountDao.listByMetadata(YEAR, "view=")), containsInAnyOrder("510.0"));
    }

    @Test
    @TestTransaction
    void syncAccounts_replacesTheAccountsOfTheYear()
    {
        given(account(YEAR, "210.0"), account(YEAR, "999.0"));
        Accounts data = new Accounts();
        data.setYear(YEAR);
        Accounts.Account synced = new Accounts.Account();
        synced.setSchemaId("210");
        synced.setSemanticId("1");
        synced.setName("savings");
        synced.setMetadata("");
        data.getAccount().add(synced);

        accountDao.syncAccounts(data);
        entityManager.clear();

        assertThat(idsOf(accountDao.list(YEAR)), containsInAnyOrder("210.1"));
        assertThat(accountDao.list(YEAR).get(0).getName(), is("savings"));
    }
}
