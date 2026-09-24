package org.kaleta;

import org.junit.jupiter.api.Test;
import org.kaleta.entity.Account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.kaleta.framework.Generator.account;

/**
 * Financial asset accounts (23x) have their creation and revaluation counterpart
 * accounts derived from their id, and the derivation changed shape after 2020.
 */
public class AccountUtilsTest
{
    @Test
    void finCreationAccountIdFrom2021OnwardsKeepsTheGroupDigit()
    {
        // 230.1 -> 546.0-1 : group digit of 23x, then the semantic id
        assertThat(AccountUtils.getFinCreationAccountId(account("2021", "230.1")), is("546.0-1"));
        assertThat(AccountUtils.getFinCreationAccountId(account("2024", "233.10")), is("546.3-10"));
    }

    @Test
    void finCreationAccountIdUpTo2020OmitsTheGroupDigit()
    {
        assertThat(AccountUtils.getFinCreationAccountId(account("2020", "230.1")), is("546.1"));
        assertThat(AccountUtils.getFinCreationAccountId(account("2019", "232.7")), is("546.7"));
    }

    @Test
    void revenueRevaluationAccountIdFollowsTheSameRule()
    {
        assertThat(AccountUtils.getFinRevRevaluationAccountId(account("2021", "231.2")), is("624.1-2"));
        assertThat(AccountUtils.getFinRevRevaluationAccountId(account("2020", "231.2")), is("624.2"));
    }

    @Test
    void expenseRevaluationAccountIdFollowsTheSameRule()
    {
        assertThat(AccountUtils.getFinExpRevaluationAccountId(account("2021", "233.0")), is("544.3-0"));
        assertThat(AccountUtils.getFinExpRevaluationAccountId(account("2020", "233.0")), is("544.0"));
    }

    @Test
    void the2020BoundaryIsExclusiveOfTheOldForm()
    {
        // 2020 uses the old form, 2021 the new one
        assertThat(AccountUtils.getFinCreationAccountId(account("2020", "230.0")), is("546.0"));
        assertThat(AccountUtils.getFinCreationAccountId(account("2021", "230.0")), is("546.0-0"));
    }

    @Test
    void nonFinancialAccountsAreRejected()
    {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> AccountUtils.validateFinAssetAccount(account("2024", "210.0")));
        assertThat(e.getMessage(), containsString("Only 23x accounts are financial assets"));
    }

    @Test
    void emptySemanticIdIsNotGuardedAndFailsLate()
    {
        // Characterises a gap rather than desired behaviour: validateFinAssetAccount
        // guards against an id with no ".", but getFullId() always inserts one
        // ("230" + "." + ""), so that branch is unreachable and the empty semantic
        // id only surfaces later as split(".")[1] going out of bounds.
        Account emptySemanticId = account("2024", "230.0");
        emptySemanticId.getAccountId().setSemanticId("");

        AccountUtils.validateFinAssetAccount(emptySemanticId); // passes, though the id is incomplete

        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> AccountUtils.getFinCreationAccountId(emptySemanticId));
    }

    @Test
    void assetAndExpenseAccountsAreDebit()
    {
        assertThat(AccountUtils.isDebit(Constants.AccountType.A), is(true));
        assertThat(AccountUtils.isDebit(Constants.AccountType.E), is(true));
    }

    @Test
    void liabilityAndRevenueAccountsAreCredit()
    {
        assertThat(AccountUtils.isDebit(Constants.AccountType.L), is(false));
        assertThat(AccountUtils.isDebit(Constants.AccountType.R), is(false));
    }

    @Test
    void offBalanceAccountsAreNeitherDebitNorCredit()
    {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> AccountUtils.isDebit(Constants.AccountType.X));
        assertThat(e.getMessage(), containsString("neither debit nor credit"));
    }
}
