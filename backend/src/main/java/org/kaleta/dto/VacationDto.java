package org.kaleta.dto;

import lombok.Data;
import org.kaleta.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class VacationDto
{
    private List<String> columns = new ArrayList<>(List.of("Date", "Amount", "Debit", "Credit", "Description"));

    private List<Vacation> vacations = new ArrayList<>();

    public static int compare(Vacation vacation1, Vacation vacation2)
    {
        return Utils.compareDates(vacation1.getTransactions().get(0).getDate(), vacation2.getTransactions().get(0).getDate());
    }

    @Data
    public static class Vacation
    {
        private String name;
        private String expenses;
        private List<Transaction> transactions = new ArrayList<>();
        private Set<ChartData> chartData = new HashSet<>();

        public static int compare(Transaction transaction1, Transaction transaction2)
        {
            return Utils.compareDates(transaction1.getDate(), transaction2.getDate());
        }

        public void addChartData(String name, Integer amount){
            for (ChartData data : chartData){
                if (data.getName().equals(name)){
                    data.setValue(data.getValue() + amount);
                    return;
                }
            }
            chartData.add(new ChartData(name, amount));
        }

        @Data
        public static class Transaction
        {
            private String date;
            private String amount;
            private String debit;
            private String credit;
            private String description;
        }

        @Data
        public static class ChartData
        {
            private String name;
            private Integer value;

            public ChartData(){}
            public ChartData(String name, Integer value)
            {
                this.name = name;
                this.value = value;
            }
        }
    }
}
