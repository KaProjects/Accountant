package org.kaleta.model;

import org.kaleta.Utils;
import org.kaleta.persistence.entity.Account;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The financial assets of every year stitched into a single timeline.
 * <p>
 * An account keeps its id across the years, so an asset is simply every occurrence of
 * one id: its series are concatenated in year order, and the years it is absent from
 * are the years before it was acquired or after it was sold.
 */
public class FinancialAssetsOverallData
{
    private final TreeMap<String, FinancialAssetsData> dataByYear;

    public FinancialAssetsOverallData(Map<String, FinancialAssetsData> dataByYear)
    {
        this.dataByYear = new TreeMap<>(dataByYear);
    }

    /** Asset groups of the schema, in the order the schema lists them. */
    public Set<String> getAssetGroups()
    {
        Set<String> groups = new LinkedHashSet<>();
        dataByYear.values().forEach(data -> groups.addAll(data.getAssetGroups()));
        return groups;
    }

    /** The group's name as of the most recent year that has it. */
    public String getAssetGroupName(String groupSchemaId)
    {
        return latest(data -> data.getAssetGroups().contains(groupSchemaId),
                data -> data.getAssetGroupName(groupSchemaId));
    }

    /** Every asset ever held in the group, oldest acquisition first. */
    public List<String> getAssetIds(String groupSchemaId)
    {
        Set<String> ids = new LinkedHashSet<>();
        for (FinancialAssetsData data : dataByYear.values())
        {
            if (!data.getAssetGroups().contains(groupSchemaId)) continue;
            data.getAssetsByGroup(groupSchemaId).forEach(account -> ids.add(account.getFullId()));
        }
        return new ArrayList<>(ids);
    }

    /**
     * The asset's whole history: opening value from the year it was acquired, and the
     * monthly series of every year it was held, one after another.
     */
    public FinancialAsset getFinancialAsset(String groupSchemaId, String assetId)
    {
        FinancialAsset overall = new FinancialAsset();
        overall.setFullId(assetId);

        Integer[] deposits = new Integer[]{};
        Integer[] revaluations = new Integer[]{};
        Integer[] withdrawals = new Integer[]{};
        Integer[] balances = new Integer[]{};
        String[] labels = new String[]{};
        boolean acquired = false;

        for (FinancialAssetsData data : dataByYear.values())
        {
            Account account = findAccount(data, groupSchemaId, assetId);
            if (account == null) continue;

            FinancialAsset yearly = data.getFinancialAsset(account);

            if (!acquired)
            {
                overall.setInitialValue(yearly.getInitialValue());
                acquired = true;
            }
            overall.setName(account.getName());

            deposits = Utils.concatArrays(deposits, yearly.getDeposits());
            revaluations = Utils.concatArrays(revaluations, yearly.getRevaluations());
            withdrawals = Utils.concatArrays(withdrawals, yearly.getWithdrawals());
            balances = Utils.concatArrays(balances, yearly.getBalances());
            labels = Utils.concatArrays(labels, yearly.getLabels());
        }

        overall.setDeposits(deposits);
        overall.setRevaluations(revaluations);
        overall.setWithdrawals(withdrawals);
        overall.setBalances(balances);
        overall.setLabels(labels);

        return overall;
    }

    private static Account findAccount(FinancialAssetsData data, String groupSchemaId, String assetId)
    {
        if (!data.getAssetGroups().contains(groupSchemaId)) return null;
        return data.getAssetsByGroup(groupSchemaId).stream()
                .filter(account -> account.getFullId().equals(assetId))
                .findFirst().orElse(null);
    }

    private <T> T latest(java.util.function.Predicate<FinancialAssetsData> has,
                         java.util.function.Function<FinancialAssetsData, T> read)
    {
        T value = null;
        for (FinancialAssetsData data : dataByYear.values()) {
            if (has.test(data)) value = read.apply(data);
        }
        return value;
    }
}
