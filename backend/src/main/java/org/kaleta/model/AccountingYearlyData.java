package org.kaleta.model;

import org.kaleta.Constants;
import org.kaleta.entity.Transaction;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class AccountingYearlyData
{
    private List<Transaction> transactions;

    public AccountingYearlyData(List<Transaction> transactions)
    {
        this.transactions = transactions;
    }

    public String[] getYears()
    {
        Set<String> closingYears = new TreeSet<>();
        transactions.forEach(transaction -> {
            closingYears.add(transaction.getYear());
        });
        String[] years = new String[closingYears.size() + 1];
        for (int i=0;i<closingYears.size();i++){
            years[i] = closingYears.toArray(new String[]{})[i];
        }
        years[years.length - 1] = String.valueOf(Integer.parseInt(years[years.length - 2]) + 1);
        return years;
    }

    public Integer[] getYearlyGroupValues(String groupId, String... accountIdSuffixes)
    {
        Map<String, Integer> yearlyData = new TreeMap<>();
        for (String year : getYears()){
            yearlyData.put(year, 0);
        }
        for (Transaction transaction : transactions){
            if ((transaction.getDebit().startsWith(groupId) && List.of(accountIdSuffixes).contains(transaction.getDebit().substring(2,3)))
                    || (transaction.getCredit().startsWith(groupId) && List.of(accountIdSuffixes).contains(transaction.getCredit().substring(2,3))))
            {
                yearlyData.put(transaction.getYear(), yearlyData.get(transaction.getYear()) + transaction.getAmount());
            }
        }
        return yearlyData.values().toArray(new Integer[]{});
    }

    public Integer[] getYearlyGroupValues(String groupId)
    {
        return getYearlyGroupValues(groupId, "0","1","2","3","4","5","6","7","8","9");
    }

    public Integer[] getYearlyClassValues(String classId, String... groupIdSuffixes)
    {
        Map<String, Integer> yearlyData = new TreeMap<>();
        for (String year : getYears()){
            yearlyData.put(year, 0);
        }
        for (Transaction transaction : transactions){
            if ((transaction.getDebit().startsWith(classId) && List.of(groupIdSuffixes).contains(transaction.getDebit().substring(1,2)))
                    || (transaction.getCredit().startsWith(classId) && List.of(groupIdSuffixes).contains(transaction.getCredit().substring(1,2))))
            {
                Integer amount = transaction.getAmount();
                if (transaction.getDebit().startsWith(Constants.Schema.ACCUMULATED_DEP_GROUP_ID)) amount = -amount;
                yearlyData.put(transaction.getYear(), yearlyData.get(transaction.getYear()) + amount);
            }
        }
        return yearlyData.values().toArray(new Integer[]{});
    }

    public Integer[] getYearlyClassValues(String classId)
    {
        return getYearlyClassValues(classId, "0","1","2","3","4","5","6","7","8","9");
    }

    public Integer[] getYearlyOverallValues()
    {
        Map<String, Integer> yearlyData = new TreeMap<>();
        for (String year : getYears()){
            yearlyData.put(year, 0);
        }
        for (Transaction transaction : transactions){
            if (!transaction.getDebit().startsWith("7"))
            {
                yearlyData.put(transaction.getYear(), yearlyData.get(transaction.getYear()) + transaction.getAmount());
            }
            if (!transaction.getCredit().startsWith("7"))
            {
                yearlyData.put(transaction.getYear(), yearlyData.get(transaction.getYear()) - transaction.getAmount());
            }
        }
        return yearlyData.values().toArray(new Integer[]{});
    }
}
