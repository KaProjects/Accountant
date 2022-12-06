package org.kaleta.model;

import lombok.Data;
import org.kaleta.Utils;

import java.util.*;

@Data
public class BudgetComponent {

    private final List<Row> rows = new ArrayList<>();
    private String name;

    public Integer[] getActualMonths() {
        if (rows.size() == 0){
            return new Integer[]{0,0,0,0,0,0,0,0,0,0,0,0};
        }
        Integer[][] rowMonths = new Integer[rows.size()][12];
        for (int i=0;i<rows.size();i++){
            rowMonths[i] = rows.get(i).getActualMonths();
        }
        return Utils.mergeIntegerArrays(rowMonths);
    }

    public Integer[] getPlannedMonths() {
        if (rows.size() == 0){
            return new Integer[]{0,0,0,0,0,0,0,0,0,0,0,0};
        }
        Integer[][] rowMonths = new Integer[rows.size()][12];
        for (int i=0;i<rows.size();i++){
            rowMonths[i] = rows.get(i).getPlannedMonths();
        }
        return Utils.mergeIntegerArrays(rowMonths);
    }

    @Data
    public static class Row {

        private final List<Row> subRows = new ArrayList<>();
        private String name;
        private Integer[] monthsPlanned = new Integer[]{};
        private Integer[] monthsActual = new Integer[]{};

        public Integer[] getActualMonths() {
            if (monthsActual.length != 0){
                return monthsActual;
            } else {
                Integer[][] subRowMonths = new Integer[subRows.size()][12];
                for (int i=0;i<subRows.size();i++){
                    subRowMonths[i] = subRows.get(i).getActualMonths();
                }
                return Utils.mergeIntegerArrays(subRowMonths);
            }
        }

        public Integer[] getPlannedMonths() {
            if (monthsPlanned.length != 0){
                return monthsPlanned;
            } else {
                Integer[][] subRowMonths = new Integer[subRows.size()][12];
                for (int i=0;i<subRows.size();i++){
                    subRowMonths[i] = subRows.get(i).getPlannedMonths();
                }
                return Utils.mergeIntegerArrays(subRowMonths);
            }
        }

    }
}
