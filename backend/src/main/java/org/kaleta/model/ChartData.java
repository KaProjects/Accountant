package org.kaleta.model;

import lombok.Data;
import org.kaleta.Constants;
import org.kaleta.Utils;
import org.kaleta.entity.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
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
                    if (isBalance(schemaId) && transaction.getCredit().equals(Constants.Account.INIT_ACC_ID)) continue;
                    Integer amount = isBalance(schemaId) ? transaction.getAmount() : -transaction.getAmount();
                    monthlyData.put(key, monthlyData.get(key) + amount);
                }
                if (transaction.getCredit().startsWith(schemaId) && notClosing(transaction.getDebit()))
                {
                    if (isBalance(schemaId) && transaction.getDebit().equals(Constants.Account.INIT_ACC_ID) && (!transaction.getDescription().contains("Profit from") || id.equals("l"))) continue;
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

    public static List<Config> getConfigs(Map<String, String> schemaNames)
    {
        List<Config> configs = new ArrayList<>();

        configs.add(new Config("60", schemaNames.get("60"), Config.ChartType.BALANCE, Set.of("60")));
        configs.add(new Config("55a", schemaNames.get("55") + " - " + schemaNames.get("60") + " - Naklady", Config.ChartType.BALANCE, Set.of("550", "551", "552")));
        configs.add(new Config("63a", schemaNames.get("63") + " - " + schemaNames.get("60") + " - Vynosy", Config.ChartType.BALANCE, Set.of("631", "632", "633", "634")));
        configs.add(new Config("ni", NET_INCOME, Config.ChartType.BALANCE, Set.of("60", "550", "551", "552", "631", "632", "633", "634")));

        configs.add(new Config("51", schemaNames.get("51"), Config.ChartType.BALANCE, Set.of("51")));
        configs.add(new Config("52", schemaNames.get("52"), Config.ChartType.BALANCE, Set.of("52")));
        configs.add(new Config("53", schemaNames.get("53"), Config.ChartType.BALANCE, Set.of("53")));

        configs.add(new Config("op", OPERATING_PROFIT, Config.ChartType.BALANCE, Set.of("60", "550", "551", "552", "631", "632", "633", "634", "51", "52", "53")));

        configs.add(new Config("50", schemaNames.get("50"), Config.ChartType.BALANCE, Set.of("50")));
        configs.add(new Config("61", schemaNames.get("61") + " - Vynosy", Config.ChartType.BALANCE, Set.of("61")));
        configs.add(new Config("56", schemaNames.get("56") + " - Naklady", Config.ChartType.BALANCE, Set.of("56")));
        configs.add(new Config("62", schemaNames.get("62") + " - Vynosy", Config.ChartType.BALANCE, Set.of("62")));
        configs.add(new Config("54", schemaNames.get("54") + " - Naklady", Config.ChartType.BALANCE, Set.of("54")));
        configs.add(new Config("63b", schemaNames.get("63") + " - Ostatne - Vynosy", Config.ChartType.BALANCE, Set.of("630")));
        configs.add(new Config("55b", schemaNames.get("55") + " - Ostatne - Naklady", Config.ChartType.BALANCE, Set.of("553", "554", "555")));

        configs.add(new Config("np", NET_PROFIT, Config.ChartType.BALANCE, Set.of("6", "5")));

        configs.add(new Config("20", schemaNames.get("20"), Config.ChartType.CUMULATIVE, Set.of("20")));
        configs.add(new Config("21", schemaNames.get("21"), Config.ChartType.CUMULATIVE, Set.of("21")));
        configs.add(new Config("23", schemaNames.get("23"), Config.ChartType.CUMULATIVE, Set.of("23")));
        configs.add(new Config("22", schemaNames.get("22"), Config.ChartType.CUMULATIVE, Set.of("22")));

        configs.add(new Config("cf", CASH_FLOW, Config.ChartType.CUMULATIVE, Set.of("20", "21", "22", "23")));

        configs.add(new Config("a", ASSETS, Config.ChartType.CUMULATIVE, Set.of("0", "1", "20", "21", "23", "30")));

        configs.add(new Config("0", schemaNames.get("0"), Config.ChartType.CUMULATIVE, Set.of("0")));
        configs.add(new Config("1", schemaNames.get("1"), Config.ChartType.CUMULATIVE, Set.of("1")));
        configs.add(new Config("2a", schemaNames.get("2") + " - " + ASSETS, Config.ChartType.CUMULATIVE, Set.of("20","21","23")));
        configs.add(new Config("3a", schemaNames.get("3") + " - " + ASSETS, Config.ChartType.CUMULATIVE, Set.of("30")));

        configs.add(new Config("l", LIABILITIES, Config.ChartType.CUMULATIVE, Set.of("22", "31", "4")));

        configs.add(new Config("2l", schemaNames.get("2") + " - " + LIABILITIES, Config.ChartType.CUMULATIVE, Set.of("22")));
        configs.add(new Config("3l", schemaNames.get("3") + " - " + LIABILITIES, Config.ChartType.CUMULATIVE, Set.of("31")));
        configs.add(new Config("4", schemaNames.get("4"), Config.ChartType.CUMULATIVE, Set.of("4")));

        configs.add(new Config("p", PROFIT, Config.ChartType.CUMULATIVE, Set.of("6", "5")));

        return configs;
    }

    @Data
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

        public enum ChartType{
            BALANCE, CUMULATIVE
        }
    }
}
