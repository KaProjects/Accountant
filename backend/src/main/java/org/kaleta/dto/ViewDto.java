package org.kaleta.dto;

import lombok.Data;
import org.kaleta.Utils;

import java.util.ArrayList;
import java.util.List;

@Data
public class ViewDto
{
    private List<String> columns = new ArrayList<>(List.of("Date", "Amount", "Debit", "Credit", "Description"));

    private List<View> views = new ArrayList<>();

    public static int compare(View view1, View view2) {
        return Utils.compareDates(view1.getTransactions().get(0).getDate(), view2.getTransactions().get(0).getDate());
    }

    public static int compare(View.Transaction transaction1, View.Transaction transaction2) {
        return Utils.compareDates(transaction1.getDate(), transaction2.getDate());
    }

    @Data
    public static class View
    {
        private String name;

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
