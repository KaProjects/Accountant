package org.kaleta.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class VacationDto
{
    private List<String> columns = new ArrayList<>(List.of("Date", "Amount", "Debit", "Credit", "Description"));

    private List<Vacation> vacations = new ArrayList<>();

    public static int compare(Vacation vacation1, Vacation vacation2)
    {
        String date1 = vacation1.getTransactions().get(0).getDate();
        String date2 = vacation2.getTransactions().get(0).getDate();

        String m1 = date1.substring(2);
        String m2 = date2.substring(2);

        if (m1.equals(m2)){
            String d1 = date1.substring(0,2);
            String d2 = date2.substring(0,2);

            return Integer.parseInt(d1) - Integer.parseInt(d2);
        } else {
            return Integer.parseInt(m1) - Integer.parseInt(m2);
        }
    }

    @Data
    public static class Vacation
    {
        private String name;
        private String expenses;
        private List<Transaction> transactions = new ArrayList<>();

        @Data
        public static class Transaction
        {
            private String date;
            private String amount;
            private String debit;
            private String credit;
            private String description;
        }
    }
}
