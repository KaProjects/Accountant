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

    /**
     * Every asset ever held in the group, by id: an account's number within its schema account
     * only grows, so this is the order the assets were opened in. Ordered by their first year
     * instead, two assets opened in the same year came out in whichever order that year listed
     * them.
     */
    public List<String> getAssetIds(String groupSchemaId)
    {
        Set<String> ids = new LinkedHashSet<>();
        for (FinancialAssetsData data : dataByYear.values())
        {
            if (!data.getAssetGroups().contains(groupSchemaId)) continue;
            data.getAssetsByGroup(groupSchemaId).forEach(account -> ids.add(account.getFullId()));
        }
        List<String> sorted = new ArrayList<>(ids);
        sorted.sort(FinancialAssetsOverallData::compareIds);
        return sorted;
    }

    /**
     * Whether the asset is still held: whether the latest year of the books has it. An asset sold
     * off is one of the earlier years only.
     */
    public boolean isHeldInLatestYear(String groupSchemaId, String assetId)
    {
        return !dataByYear.isEmpty() && findAccount(dataByYear.lastEntry().getValue(), groupSchemaId, assetId) != null;
    }

    /**
     * Account ids by their parts as numbers - the schema account, then the account's number in it
     * and any number after a dash - so that 230.10 comes after 230.9 rather than before it.
     */
    static int compareIds(String first, String second)
    {
        String[] a = first.split("[.-]");
        String[] b = second.split("[.-]");
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            int compared = comparePart(a[i], b[i]);
            if (compared != 0) return compared;
        }
        return Integer.compare(a.length, b.length);
    }

    private static int comparePart(String a, String b)
    {
        if (a.matches("\\d+") && b.matches("\\d+")) return new java.math.BigInteger(a).compareTo(new java.math.BigInteger(b));
        return a.compareTo(b);
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
