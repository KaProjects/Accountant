package org.kaleta.model;

import org.junit.jupiter.api.Test;
import org.kaleta.Constants;
import org.kaleta.persistence.entity.Account;
import org.kaleta.persistence.entity.Transaction;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.kaleta.framework.Generator.account;
import static org.kaleta.framework.Generator.transaction;

/**
 * The overall view no longer reads a hand-maintained mapping of which account an asset
 * used in which year: an account keeps its id across the years, so an asset is every
 * occurrence of that id. These tests pin that stitching down.
 */
class FinancialAssetsOverallDataTest
{
    private static SchemaClass.Group group23()
    {
        SchemaClass.Group group = new SchemaClass.Group(Constants.Schema.FIN_GROUP_ID, "financial");
        group.addAccount(new SchemaClass.Group.Account("230", "shares", Constants.AccountType.A));
        group.addAccount(new SchemaClass.Group.Account("231", "stocks", Constants.AccountType.A));
        return group;
    }

    /** One year holding the given accounts, each opened with the given amount. */
    private static FinancialAssetsData year(String y, Map<String, Integer> openingByFullId)
    {
        List<Account> accounts = openingByFullId.keySet().stream()
                .map(fullId -> account(y, fullId, "asset " + fullId, "")).toList();
        List<Transaction> transactions = openingByFullId.entrySet().stream()
                .map(e -> transaction(y, "0101", e.getValue(), e.getKey(), Constants.Account.INIT_ACC_ID))
                .toList();
        return new FinancialAssetsData(Map.of("230", accounts), transactions, group23());
    }

    @Test
    void anAssetHeldForSeveralYearsBecomesOneContinuousSeries()
    {
        FinancialAssetsOverallData data = new FinancialAssetsOverallData(Map.of(
                "2024", year("2024", Map.of("230.0", 100)),
                "2025", year("2025", Map.of("230.0", 150))));

        FinancialAsset asset = data.getFinancialAsset("230", "230.0");

        assertThat(asset.getInitialValue(), is(100));           // from the year it was acquired
        assertThat(asset.getLabels().length, is(24));           // two years of months, in order
        assertThat(asset.getLabels()[0], is("1/24"));
        assertThat(asset.getLabels()[12], is("1/25"));
        assertThat(asset.getBalances().length, is(24));
    }

    @Test
    void anAssetAcquiredLaterStartsAtItsOwnFirstYear()
    {
        FinancialAssetsOverallData data = new FinancialAssetsOverallData(Map.of(
                "2024", year("2024", Map.of("230.0", 100)),
                "2025", year("2025", Map.of("230.0", 150, "230.1", 400))));

        FinancialAsset late = data.getFinancialAsset("230", "230.1");

        assertThat(late.getInitialValue(), is(400));
        assertThat(late.getLabels(), is(arrayContaining(
                "1/25", "2/25", "3/25", "4/25", "5/25", "6/25",
                "7/25", "8/25", "9/25", "10/25", "11/25", "12/25")));
    }

    @Test
    void everyAssetEverHeldIsListed()
    {
        FinancialAssetsOverallData data = new FinancialAssetsOverallData(Map.of(
                "2024", year("2024", Map.of("230.0", 100)),
                "2025", year("2025", Map.of("230.1", 400))));

        // 230.0 was sold during 2024 and 230.1 bought in 2025 - both belong in the view
        assertThat(data.getAssetIds("230"), contains("230.0", "230.1"));
        assertThat(data.getFinancialAsset("230", "230.0").getLabels().length, is(12));
    }

    @Test
    void groupAndAssetNamesComeFromTheMostRecentYear()
    {
        FinancialAssetsOverallData data = new FinancialAssetsOverallData(Map.of(
                "2024", year("2024", Map.of("230.0", 100)),
                "2025", year("2025", Map.of("230.0", 150))));

        assertThat(data.getAssetGroupName("230"), is("shares"));
        assertThat(data.getFinancialAsset("230", "230.0").getName(), is("asset 230.0"));
    }
}
