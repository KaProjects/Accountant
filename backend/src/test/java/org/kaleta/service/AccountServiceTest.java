package org.kaleta.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.kaleta.Constants;
import org.kaleta.dto.YearAccountOverviewDto;
import org.kaleta.dto.YearAccountTransactionDto;
import org.kaleta.persistence.api.AccountDao;
import org.kaleta.persistence.entity.Account;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.kaleta.framework.Generator.account;
import static org.kaleta.framework.Generator.transaction;
import static org.mockito.Mockito.when;

@QuarkusTest
class AccountServiceTest
{
    @InjectMock
    AccountDao accountDao;
    @InjectMock
    SchemaService schemaService;
    @InjectMock
    TransactionService transactionService;

    @Inject
    AccountService accountService;

    @Test
    void getAccountNamesMap_namesAGeneralAccountAfterItsSchemaAccount()
    {
        when(schemaService.getSchemaNames("2024")).thenReturn(Map.of("210", "current account"));
        when(accountDao.list("2024")).thenReturn(List.of(
                account("2024", "210.0", "general", ""),
                account("2024", "210.1", "savings", "")));

        Map<String, String> names = accountService.getAccountNamesMap("2024");

        assertThat(names.get("210.0"), is("current account"));
        assertThat(names.get("210.1"), is("savings"));
    }

    @Test
    void getFinancialAssetAccounts_groupsThemBySchemaAccountInOrder()
    {
        Account stocks = account("2024", "231.0");
        Account bonds = account("2024", "230.1");
        Account fund = account("2024", "230.2");
        when(accountDao.list("2024", Constants.Schema.FIN_GROUP_ID)).thenReturn(List.of(stocks, bonds, fund));

        Map<String, List<Account>> groups = accountService.getFinancialAssetAccounts("2024");

        assertThat(groups.keySet(), contains("230", "231"));
        assertThat(groups.get("230"), contains(bonds, fund));
        assertThat(groups.get("231"), contains(stocks));
    }

    @Test
    void getAccountsOverview_statesADebitAccountFromItsDebits()
    {
        when(schemaService.getAccountType("2024", "210")).thenReturn(Constants.AccountType.A);
        when(accountDao.list("2024", "210")).thenReturn(List.of(account("2024", "210.1", "savings", "")));
        when(transactionService.getTransactionsMatching("2024", "210")).thenReturn(List.of(
                // opened with 100
                transaction("2024", "0101", 100, "210.1", Constants.Account.INIT_ACC_ID),
                transaction("2024", "0502", 50, "210.1", "600.0"),
                transaction("2024", "0703", 30, "510.0", "210.1"),
                // closed: not a movement of the year
                transaction("2024", "3112", 120, Constants.Account.CLOSING_ACC_ID, "210.1")));

        YearAccountOverviewDto overview = accountService.getAccountsOverview("2024", "210").get(0);

        assertThat(overview.getId(), is("210.1"));
        assertThat(overview.getInitial(), is(100));
        // the opening counts as a debit, the closing does not count at all
        assertThat(overview.getTurnover(), is(150));
        assertThat(overview.getBalance(), is(120));
    }

    @Test
    void getAccountsOverview_statesACreditAccountFromItsCredits()
    {
        when(schemaService.getAccountType("2024", "220")).thenReturn(Constants.AccountType.L);
        when(accountDao.list("2024", "220")).thenReturn(List.of(account("2024", "220.0")));
        when(transactionService.getTransactionsMatching("2024", "220")).thenReturn(List.of(
                transaction("2024", "0101", 40, Constants.Account.INIT_ACC_ID, "220.0"),
                transaction("2024", "0502", 25, "510.0", "220.0"),
                transaction("2024", "0703", 10, "220.0", "210.1")));

        YearAccountOverviewDto overview = accountService.getAccountsOverview("2024", "220").get(0);

        assertThat(overview.getInitial(), is(40));
        assertThat(overview.getTurnover(), is(65));
        assertThat(overview.getBalance(), is(55));
    }

    @Test
    void getAccountsOverview_statesNoTurnoverNorBalanceForAnOffBalanceAccount()
    {
        when(schemaService.getAccountType("2024", "790")).thenReturn(Constants.AccountType.X);
        when(accountDao.list("2024", "790")).thenReturn(List.of(account("2024", "790.0")));
        when(transactionService.getTransactionsMatching("2024", "790")).thenReturn(List.of(
                transaction("2024", "0502", 25, "790.0", "791.0")));

        YearAccountOverviewDto overview = accountService.getAccountsOverview("2024", "790").get(0);

        assertThat(overview.getTurnover(), is(nullValue()));
        assertThat(overview.getBalance(), is(nullValue()));
    }

    @Test
    void getAccountTransactions_pairsEachWithTheAccountOnItsOtherSide_latestFirst()
    {
        when(schemaService.getSchemaNames("2024")).thenReturn(Map.of());
        when(accountDao.list("2024")).thenReturn(List.of(
                account("2024", "210.1", "savings", ""),
                account("2024", "600.0", "salary", ""),
                account("2024", "510.0", "food", "")));
        when(transactionService.getTransactionsMatching("2024", "210.1")).thenReturn(List.of(
                transaction("2024", "0502", 50, "210.1", "600.0", "pay"),
                transaction("2024", "0703", 30, "510.0", "210.1", "groceries")));

        List<YearAccountTransactionDto> transactions = accountService.getAccountTransactions("2024", "210.1");

        assertThat(transactions.get(0).getDescription(), is("groceries"));
        assertThat(transactions.get(0).getCredit(), is("30"));
        assertThat(transactions.get(0).getPair(), is("510.0 food"));
        assertThat(transactions.get(1).getDebit(), is("50"));
        assertThat(transactions.get(1).getPair(), is("600.0 salary"));
    }
}
