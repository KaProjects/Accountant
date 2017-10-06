package org.kaleta.accountant.common;

public class Constants {

    public static class Schema {
        public static final String DEFAULT_CLASS_0 = "Assets";
        public static final String DEFAULT_CLASS_1 = "Resources";
        public static final String DEFAULT_CLASS_2 = "Finance";
        public static final String DEFAULT_CLASS_3 = "Relations";
        public static final String DEFAULT_CLASS_4 = "Funding";
        public static final String DEFAULT_CLASS_5 = "Expenses";
        public static final String DEFAULT_CLASS_6 = "Revenues";
        public static final String DEFAULT_CLASS_7 = "Off Balance";

        public static final String INIT_ACC_ID = "711";
        public static final String CLOSING_ACC_ID = "712";
        public static final String PROFIT_ACC_ID = "721";
    }

    public static class Color {
        public static final java.awt.Color INCOME_GREEN = java.awt.Color.getHSBColor(120/360f,0.5f,0.75f);
        public static final java.awt.Color EXPENSE_RED = java.awt.Color.getHSBColor(0/360f,0.5f,1);
    }

    public static class AccountType {
        public static final String ASSET = "A";
        public static final String LIABILITY = "L";
        public static final String EXPENSE = "E";
        public static final String REVENUE = "R";
        public static final String OFF_BALANCE = "X";
    }
}
