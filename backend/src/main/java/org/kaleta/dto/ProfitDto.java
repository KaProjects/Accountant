package org.kaleta.dto;

import lombok.Data;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Data
public class ProfitDto
{
    private List<String> columns = new ArrayList<>();
    private List<Row> rows = new ArrayList<>();

    public ProfitDto(String year)
    {
        columns.add("Income Statement " + year);
        columns.addAll(List.of(new DateFormatSymbols(Locale.US).getMonths()));
        columns.remove(13);
        columns.add("Total");
    }

    @Data
    public static class Row
    {
        private Type type;
        private String name;
        private String schemaId;
        private Integer[] monthlyValues;
        private Integer total;
        private List<Row> accounts = new ArrayList<>();

        public Row(Type type, String name, String schemaId)
        {
            this.type = type;
            this.name = name;
            this.schemaId = schemaId;
        }
    }

    public enum Type
    {
        SUMMARY,
        INCOME_GROUP,
        EXPENSE_GROUP,
        INCOME_ACCOUNT,
        EXPENSE_ACCOUNT
    }
}
