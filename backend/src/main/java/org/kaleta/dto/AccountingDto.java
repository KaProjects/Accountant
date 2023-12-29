package org.kaleta.dto;

import lombok.Data;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Data
public class AccountingDto
{
    private List<String> columns = new ArrayList<>();
    private List<Row> rows = new ArrayList<>();

    public AccountingDto() {}
    public AccountingDto(String year, Type type)
    {
        if (type == Type.PROFIT_SUMMARY) {
            columns.add("Income Statement " + year);
            columns.addAll(List.of(new DateFormatSymbols(Locale.US).getMonths()));
            columns.remove(13);
            columns.add("Total");
        }
        if (type == Type.CASH_FLOW_SUMMARY) {
            columns.add("Cash Flow Statement " + year);
            columns.add("Initial");
            columns.addAll(List.of(new DateFormatSymbols(Locale.US).getMonths()));
            columns.remove(14);
            columns.add("Total");
        }
        if (type == Type.BALANCE_SUMMARY) {
            columns.add("Balance Sheet " + year);
            columns.add("Initial");
            columns.addAll(List.of(new DateFormatSymbols(Locale.US).getMonths()));
            columns.remove(14);
            columns.add("Total");
        }
    }

    public AccountingDto(String[] years, Type type)
    {
        if (type == Type.PROFIT_SUMMARY) {
            columns.add("Yearly Income Statement");
            columns.addAll(List.of(years));
            columns.add("Total");
        }
        if (type == Type.CASH_FLOW_SUMMARY) {
            columns.add("Yearly Cash Flow Statement");
            columns.addAll(List.of(years));
        }
    }

    @Data
    public static class Row
    {
        private Type type;
        private String name;
        private String schemaId;
        private Integer initial = null;
        private Integer[] monthlyValues;
        private Integer[] yearlyValues;
        private Integer total;
        private List<Row> children = new ArrayList<>();

        public Row() {}
        public Row(Type type, String name, String schemaId)
        {
            this.type = type;
            this.name = name;
            this.schemaId = schemaId;
        }
    }

    public enum Type
    {
        PROFIT_SUMMARY, INCOME_GROUP, INCOME_ACCOUNT, EXPENSE_GROUP, EXPENSE_ACCOUNT,
        CASH_FLOW_SUMMARY, CASH_FLOW_GROUP, CASH_FLOW_ACCOUNT,
        BALANCE_SUMMARY, BALANCE_CLASS, BALANCE_GROUP, BALANCE_ACCOUNT
    }
}
