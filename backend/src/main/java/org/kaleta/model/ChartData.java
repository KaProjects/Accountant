package org.kaleta.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import org.kaleta.Constants;
import org.kaleta.Utils;
import org.kaleta.persistence.entity.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.kaleta.Constants.Label.ASSETS;
import static org.kaleta.Constants.Label.CASH_FLOW;
import static org.kaleta.Constants.Label.LIABILITIES;
import static org.kaleta.Constants.Label.NET_INCOME;
import static org.kaleta.Constants.Label.NET_PROFIT;
import static org.kaleta.Constants.Label.OPERATING_PROFIT;
import static org.kaleta.Constants.Label.PROFIT;

public class ChartData
{
    private final List<Transaction> transactions;
    private final List<String> years;
    private Map<String, String> firstYears;

    public ChartData(List<Transaction> transactions, List<String> years)
    {
        this.transactions = transactions;
        this.years = years;
    }

    public String[] getLabels()
    {
        String[] labels = new String[years.size() * 12];
        for (int y=0;y<years.size();y++)
        {
            for (int m=0;m<12;m++) labels[y * 12 + m] = String.format("%02d", m+1) + "/" + years.get(y).substring(2,4);
        }
        return labels;
    }

    public Integer[] getValues(String id)
    {
        Config config = getConfigs().get(id);
        Map<String, Integer> monthlyData = new TreeMap<>();
        for (String year : years){
            for (int i=0;i<12;i++) monthlyData.put(year + String.format("%02d", i+1), 0);
        }
        for (Transaction transaction : transactions)
        {
            String key = transaction.getYear() + transaction.getDate().substring(2,4);

            for (String schemaId : config.getSchemas())
            {
                if (transaction.getDebit().startsWith(schemaId) && notClosing(transaction.getCredit()))
                {
                    if (isBalance(schemaId) && transaction.getCredit().equals(Constants.Account.INIT_ACC_ID)
                            && !isOpeningBalance(transaction.getDebit(), transaction.getYear())) continue;
                    Integer amount = isBalance(schemaId) ? transaction.getAmount() : -transaction.getAmount();
                    monthlyData.put(key, monthlyData.get(key) + amount);
                }
                if (transaction.getCredit().startsWith(schemaId) && notClosing(transaction.getDebit()))
                {
                    if (isBalance(schemaId) && transaction.getDebit().equals(Constants.Account.INIT_ACC_ID)
                            && !isOpeningBalance(transaction.getCredit(), transaction.getYear())
                            && (!transaction.getDescription().contains("Profit from") || id.equals("l"))) continue;
                    Integer amount = isBalance(schemaId) ? -transaction.getAmount() : transaction.getAmount();
                    monthlyData.put(key, monthlyData.get(key) + amount);
                }
            }
        }
        Integer[] monthlyValues = monthlyData.values().toArray(new Integer[]{});
        if (id.startsWith("5") || id.equals("22") || id.equals("l") || id.equals("2l") || id.equals("3l") || id.equals("4")) {
            return Utils.invertValues(monthlyValues);
        } else {
            return monthlyValues;
        }
    }

    /**
     * Every year re-initiates each balance account with the previous year's closing balance,
     * and a cumulative series must not count those again because it already carries the
     * movements they summarise. An initiation in the first year the account appears at all is
     * different: nothing precedes it, so it is the opening balance the account really started
     * from and it belongs in the series. Skipping that one as well left every cumulative chart
     * permanently short of the ledger's starting position, which showed as negative cash.
     */
    private boolean isOpeningBalance(String accountId, String year)
    {
        if (firstYears == null)
        {
            firstYears = new HashMap<>();
            for (Transaction transaction : transactions)
            {
                for (String account : List.of(transaction.getDebit(), transaction.getCredit()))
                {
                    String earliest = firstYears.get(account);
                    if (earliest == null || transaction.getYear().compareTo(earliest) < 0) {
                        firstYears.put(account, transaction.getYear());
                    }
                }
            }
        }
        return year.equals(firstYears.get(accountId));
    }

    private boolean notClosing(String accountId)
    {
        return !accountId.equals(Constants.Account.CLOSING_ACC_ID) && !accountId.equals(Constants.Account.PROFIT_ACC_ID);
    }

    private boolean isBalance(String schemaId)
    {
        return !(schemaId.startsWith("5") || schemaId.startsWith("6"));
    }

    public static Map<String,Config> getConfigs()
    {
        Map<String,Config> configs = new TreeMap<>();
        for (Config config : getConfigs(new HashMap<>()))
        {
            configs.put(config.getId(), config);
        }
        return configs;
    }

    /**
     * The name of a schema element in the latest year, or its ID where that year has no name for
     * it - a group renamed or dropped since, say. The chart names are put together from these, and
     * a missing one used to be written into them as the word "null".
     */
    private static String nameOf(Map<String, String> schemaNames, String id)
    {
        String name = schemaNames.get(id);
        return name != null ? name : id;
    }

    /**
     * The schemas a chart is drawn from, in the order they are written. {@code Set.of} would hold
     * the same ones, but iterate them in an order it deliberately changes from one run of the JVM
     * to the next, so the chart configuration answered with them came out differently every time.
     */
    private static Set<String> schemas(String... ids)
    {
        return Collections.unmodifiableSet(new LinkedHashSet<>(List.of(ids)));
    }

    public static List<Config> getConfigs(Map<String, String> schemaNames)
    {
        List<Config> configs = new ArrayList<>();

        configs.add(new Config("60", nameOf(schemaNames, "60"), Config.ChartType.BALANCE, schemas("60")));
        configs.add(new Config("55a", nameOf(schemaNames, "55") + " - " + nameOf(schemaNames, "60") + " - Naklady", Config.ChartType.BALANCE, schemas("550", "551", "552")));
        configs.add(new Config("63a", nameOf(schemaNames, "63") + " - " + nameOf(schemaNames, "60") + " - Vynosy", Config.ChartType.BALANCE, schemas("631", "632", "633", "634")));
        configs.add(new Config("ni", NET_INCOME, Config.ChartType.BALANCE, schemas("60", "550", "551", "552", "631", "632", "633", "634")));

        configs.add(new Config("51", nameOf(schemaNames, "51"), Config.ChartType.BALANCE, schemas("51")));
        configs.add(new Config("52", nameOf(schemaNames, "52"), Config.ChartType.BALANCE, schemas("52")));
        configs.add(new Config("53", nameOf(schemaNames, "53"), Config.ChartType.BALANCE, schemas("53")));

        configs.add(new Config("op", OPERATING_PROFIT, Config.ChartType.BALANCE, schemas("60", "550", "551", "552", "631", "632", "633", "634", "51", "52", "53")));

        configs.add(new Config("50", nameOf(schemaNames, "50"), Config.ChartType.BALANCE, schemas("50")));
        configs.add(new Config("61", nameOf(schemaNames, "61") + " - Vynosy", Config.ChartType.BALANCE, schemas("61")));
        configs.add(new Config("56", nameOf(schemaNames, "56") + " - Naklady", Config.ChartType.BALANCE, schemas("56")));
        configs.add(new Config("62", nameOf(schemaNames, "62") + " - Vynosy", Config.ChartType.BALANCE, schemas("62")));
        configs.add(new Config("54", nameOf(schemaNames, "54") + " - Naklady", Config.ChartType.BALANCE, schemas("54")));
        configs.add(new Config("63b", nameOf(schemaNames, "63") + " - Ostatne - Vynosy", Config.ChartType.BALANCE, schemas("630", "635")));
        configs.add(new Config("55b", nameOf(schemaNames, "55") + " - Ostatne - Naklady", Config.ChartType.BALANCE, schemas("553", "554", "555", "556")));

        configs.add(new Config("np", NET_PROFIT, Config.ChartType.BALANCE, schemas("6", "5")));

        configs.add(new Config("20", nameOf(schemaNames, "20"), Config.ChartType.CUMULATIVE, schemas("20")));
        configs.add(new Config("21", nameOf(schemaNames, "21"), Config.ChartType.CUMULATIVE, schemas("21")));
        configs.add(new Config("23", nameOf(schemaNames, "23"), Config.ChartType.CUMULATIVE, schemas("23")));
        configs.add(new Config("22", nameOf(schemaNames, "22"), Config.ChartType.CUMULATIVE, schemas("22")));

        configs.add(new Config("cf", CASH_FLOW, Config.ChartType.CUMULATIVE, schemas("20", "21", "22", "23")));

        configs.add(new Config("a", ASSETS, Config.ChartType.CUMULATIVE, schemas("0", "1", "20", "21", "23", "30")));

        configs.add(new Config("0", nameOf(schemaNames, "0"), Config.ChartType.CUMULATIVE, schemas("0")));
        configs.add(new Config("1", nameOf(schemaNames, "1"), Config.ChartType.CUMULATIVE, schemas("1")));
        configs.add(new Config("2a", nameOf(schemaNames, "2") + " - " + ASSETS, Config.ChartType.CUMULATIVE, schemas("20","21","23")));
        configs.add(new Config("3a", nameOf(schemaNames, "3") + " - " + ASSETS, Config.ChartType.CUMULATIVE, schemas("30")));

        configs.add(new Config("l", LIABILITIES, Config.ChartType.CUMULATIVE, schemas("22", "31", "4")));

        configs.add(new Config("2l", nameOf(schemaNames, "2") + " - " + LIABILITIES, Config.ChartType.CUMULATIVE, schemas("22")));
        configs.add(new Config("3l", nameOf(schemaNames, "3") + " - " + LIABILITIES, Config.ChartType.CUMULATIVE, schemas("31")));
        configs.add(new Config("4", nameOf(schemaNames, "4"), Config.ChartType.CUMULATIVE, schemas("4")));

        configs.add(new Config("p", PROFIT, Config.ChartType.CUMULATIVE, schemas("6", "5")));

        return configs;
    }

    @Data
    @RegisterForReflection
    public static class Config
    {
        private String id;
        private String name;
        private ChartType type;

        private Set<String> schemas;

        public Config(){}
        public Config(String id, String name, ChartType type, Set<String> schemas)
        {
            this.id = id;
            this.name = name;
            this.type = type;
            this.schemas = schemas;
        }

        @RegisterForReflection
        public enum ChartType{
            BALANCE, CUMULATIVE
        }
    }
}
