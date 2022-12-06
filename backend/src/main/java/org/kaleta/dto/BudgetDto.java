package org.kaleta.dto;

import lombok.Data;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Data
public class BudgetDto {

    private List<String> columns = new ArrayList<>();
    private List<Row> rows = new ArrayList<>();
    private Integer lastFilledMonth;

    public BudgetDto(String year, Integer lastFilledMonth){
        this.lastFilledMonth = lastFilledMonth;
        columns.add("Budget " + year);
        columns.addAll(List.of(new DateFormatSymbols(Locale.US).getMonths()));
        columns.remove(13);
        columns.add("actual");
        columns.add("actual/"+lastFilledMonth);
        columns.add("budget/"+lastFilledMonth);
        columns.add("delta/"+lastFilledMonth);
    }

    public BudgetDto.Row addRow(BudgetDto.Row.Type type, String name, Integer[] monthlyActual, Integer[] monthlyPlanned){
        BudgetDto.Row row = new BudgetDto.Row(lastFilledMonth);
        row.setType(type);
        row.setName(name);
        row.setActual(List.of(monthlyActual));
        row.setActualSum(Arrays.stream(monthlyActual).mapToInt(Integer::intValue).sum());
        row.setActualAvg((lastFilledMonth != 0) ? row.getActualSum()/lastFilledMonth : 0);
        row.setPlanned(List.of(monthlyPlanned));
        row.setPlannedSum(Arrays.stream(monthlyPlanned).mapToInt(Integer::intValue).sum());
        row.setPlannedAvg(row.getPlannedSum()/12);
        Integer plannedSumToFilledMonth = 0;
        for (int m=0;m<lastFilledMonth;m++){
            plannedSumToFilledMonth += monthlyPlanned[m];
        }
        row.setPlannedSumToFilledMonth(plannedSumToFilledMonth);
        row.setPlannedAvgToFilledMonth((lastFilledMonth != 0) ? plannedSumToFilledMonth/lastFilledMonth : 0);
        row.setDeltaSum(row.getActualSum() - row.getPlannedSumToFilledMonth());
        row.setDeltaAvg(row.getActualAvg() - row.getPlannedAvgToFilledMonth());
        this.getRows().add(row);
        return row;
    }

    @Data
    public static class Row {
        private Type type;
        private String name;
        private List<Integer> actual = new ArrayList<>();
        private List<Integer> planned = new ArrayList<>();
        private Integer actualSum;
        private Integer actualAvg;
        private Integer plannedSum;
        private Integer plannedSumToFilledMonth;
        private Integer plannedAvg;
        private Integer plannedAvgToFilledMonth;
        private Integer deltaSum;
        private Integer deltaAvg;
        private List<Row> subRows = new ArrayList<>();
        private Integer lastFilledMonth;
        public Row(Integer lastFilledMonth){
            this.lastFilledMonth = lastFilledMonth;
        }

        public void addSubRow(String name, Integer[] monthlyActual, Integer[] monthlyPlanned){
            BudgetDto.Row row = new BudgetDto.Row(lastFilledMonth);
            row.setType(Type.SUB_ROW);
            row.setName(name);
            row.setActual(List.of(monthlyActual));
            row.setActualSum(Arrays.stream(monthlyActual).mapToInt(Integer::intValue).sum());
            row.setActualAvg((lastFilledMonth != 0) ? row.getActualSum()/lastFilledMonth : 0);
            row.setPlanned(List.of(monthlyPlanned));
            row.setPlannedSum(Arrays.stream(monthlyPlanned).mapToInt(Integer::intValue).sum());
            row.setPlannedAvg(row.getPlannedSum()/12);
            Integer plannedSumToFilledMonth = 0;
            for (int m=0;m<lastFilledMonth;m++){
                plannedSumToFilledMonth += monthlyPlanned[m];
            }
            row.setPlannedSumToFilledMonth(plannedSumToFilledMonth);
            row.setPlannedAvgToFilledMonth((lastFilledMonth != 0) ? plannedSumToFilledMonth/lastFilledMonth : 0);
            row.setDeltaSum(row.getActualSum() - row.getPlannedSumToFilledMonth());
            row.setDeltaAvg(row.getActualAvg() - row.getPlannedAvgToFilledMonth());
            this.getSubRows().add(row);
        }
        public enum Type {
            INCOME,
            INCOME_SUM,
            EXPENSE,
            EXPENSE_SUM,
            BALANCE,
            SUB_ROW,
            OF_BUDGET,
            OF_BUDGET_BALANCE
        }
    }
}
