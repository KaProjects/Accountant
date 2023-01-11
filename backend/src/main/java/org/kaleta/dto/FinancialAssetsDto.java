package org.kaleta.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FinancialAssetsDto {
    private List<Group> groups = new ArrayList<>();

    @Data
    public static class Group {
        private String name;

        private List<Account> accounts = new ArrayList<>();

        @Data
        public static class Account {
            private String name;

            private Integer[] valuationSequence;

            private Integer[] fundingSequence;

            private Integer[] deposits;
            private Integer[] withdrawals;
            private Integer[] revaluations;

            private String[] labels;

        }
    }
}
