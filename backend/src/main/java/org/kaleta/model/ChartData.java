package org.kaleta.model;

import lombok.Data;
import org.kaleta.Constants;
import org.kaleta.Utils;
import org.kaleta.entity.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.kaleta.Constants.Label.NET_PROFIT;

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
        Config config = getConfigs(new HashMap<>()).get(id);
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
                    Integer amount = isBalance(schemaId) ? transaction.getAmount() : -transaction.getAmount();
                    monthlyData.put(key, monthlyData.get(key) + amount);
                }
                if (transaction.getCredit().startsWith(schemaId) && notClosing(transaction.getDebit()))
                {
                    Integer amount = isBalance(schemaId) ? -transaction.getAmount() : transaction.getAmount();
                    monthlyData.put(key, monthlyData.get(key) + amount);
                }
            }
        }
        Integer[] monthlyValues = monthlyData.values().toArray(new Integer[]{});
        if (id.startsWith("5")) {
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

    public static Map<String,Config> getConfigs(Map<String, String> schemaNames){

        Map<String,Config> configs = new HashMap<>();
        configs.put("60", new Config(schemaNames.get("60"), Config.ChartType.BALANCE, Set.of("60")));
        configs.put("55a", new Config(schemaNames.get("55"), Config.ChartType.BALANCE, Set.of("550", "551", "552")));
        configs.put("63a", new Config(schemaNames.get("63"), Config.ChartType.BALANCE, Set.of("631", "632", "633")));
        configs.put("np", new Config(NET_PROFIT, Config.ChartType.BALANCE, Set.of("60", "550", "551", "552", "631", "632", "633")));

//            AccountingDto.Row row51 = from(schemaNames.get("51"), EXPENSE_GROUP, data.getGroupValues(EXPENSE_GROUP, "51"));
//            AccountingDto.Row row52 = from(schemaNames.get("52"), EXPENSE_GROUP, data.getGroupValues(EXPENSE_GROUP, "52"));
//            AccountingDto.Row row53 = from(schemaNames.get("53"), EXPENSE_GROUP, data.getGroupValues(EXPENSE_GROUP, "53"));
//
//            AccountingDto.Row row50 = from(schemaNames.get("50"), EXPENSE_GROUP, data.getGroupValues(EXPENSE_GROUP, "50"));
//            AccountingDto.Row row61 = from(schemaNames.get("61"), INCOME_GROUP, data.getGroupValues(INCOME_GROUP, "61"));
//            AccountingDto.Row row56 = from(schemaNames.get("56"), EXPENSE_GROUP, data.getGroupValues(EXPENSE_GROUP, "56"));
//            AccountingDto.Row row62 = from(schemaNames.get("62"), INCOME_GROUP, data.getGroupValues(INCOME_GROUP, "62"));
//            AccountingDto.Row row54 = from(schemaNames.get("54"), EXPENSE_GROUP, data.getGroupValues(EXPENSE_GROUP, "54"));
//            AccountingDto.Row row63b = from(schemaNames.get("63"), INCOME_GROUP, data.getGroupValues(INCOME_GROUP, "63", "0"));
//            AccountingDto.Row row55b = from(schemaNames.get("55"), EXPENSE_GROUP, data.getGroupValues(EXPENSE_GROUP, "55", "3", "4", "5"));

        return configs;
    }

    @Data
    public static class Config
    {
        private String name;
        private ChartType type;

        private Set<String> schemas;

        public Config(){}
        public Config(String name, ChartType type, Set<String> schemas)
        {
            this.name = name;
            this.type = type;
            this.schemas = schemas;
        }

        public enum ChartType{
            BALANCE, CUMULATIVE
        }
    }
}
