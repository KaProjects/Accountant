package org.kaleta.model;

import org.junit.jupiter.api.Test;
import org.kaleta.Constants;
import org.kaleta.persistence.entity.Transaction;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.kaleta.framework.Generator.account;
import static org.kaleta.framework.Generator.transaction;

/**
 * How a year's transactions make an asset's deposits, withdrawals and revaluations: money put in
 * is booked against its creation account (549), changes in its value against the revaluation
 * accounts (629 up, 548 down), and anything else moving it in or out is a deposit or withdrawal.
 */
class FinancialAssetsDataTest
{
    private static SchemaClass.Group group23()
    {
        SchemaClass.Group group = new SchemaClass.Group(Constants.Schema.FIN_GROUP_ID, "financial");
        group.addAccount(new SchemaClass.Group.Account("230", "shares", Constants.AccountType.A));
        return group;
    }

    private static FinancialAsset assetOf(String year, List<Transaction> transactions)
    {
        FinancialAssetsData data = new FinancialAssetsData(
                Map.of("230", List.of(account(year, "230.0", "fund", ""))), transactions, group23());
        return data.getFinancialAsset(account(year, "230.0", "fund", ""));
    }

    /** A year booked as the books are: the accounts named with the asset's group digit, as 549.0-0. */
    private static List<Transaction> bookedTheCurrentWay(String year, String creation, String gain, String loss)
    {
        return List.of(
                transaction(year, "0101", 1000, "230.0", Constants.Account.INIT_ACC_ID),
                // 500 put in from salary in February, booked as the asset's creation
                transaction(year, "1002", 500, creation, "302.0"),
                // its value then went up 600 - the 500 bought and 100 gained - and later down 50
                transaction(year, "1102", 600, "230.0", gain),
                transaction(year, "1505", 50, loss, "230.0"));
    }

    @Test
    void aYearUpTo2020HasItsCreationAndRevaluationsRecognised()
    {
        // these years were once read with an older naming the books no longer use: the creation
        // went unnoticed, and every revaluation was taken for a deposit or a withdrawal, so what
        // funded the asset followed what it was worth exactly
        FinancialAsset asset = assetOf("2019", bookedTheCurrentWay("2019", "549.0-0", "629.0-0", "548.0-0"));

        assertThat(asset.getDepositsSum(), is(500));
        assertThat(asset.getWithdrawalsSum(), is(0));
        assertThat(asset.getRevaluations()[1], is(600));
        assertThat(asset.getRevaluations()[4], is(-50));
        assertThat(asset.getCurrentValue(), is(1550));
    }

    @Test
    void aLaterYearIsReadTheSameWay()
    {
        FinancialAsset asset = assetOf("2021", bookedTheCurrentWay("2021", "549.0-0", "629.0-0", "548.0-0"));

        assertThat(asset.getDepositsSum(), is(500));
        assertThat(asset.getRevaluations()[1], is(600));
    }
}
