package org.kaleta.accountant.common;

public class Constants {

    /**
     * Default values for schema.
     */
    public static class Schema {
        public static final String CLASS_0_NAME = "Assets";
        public static final String CLASS_1_NAME = "Resources";
        public static final String CLASS_2_NAME = "Finance";
        public static final String CLASS_3_NAME = "Relations";
        public static final String CLASS_4_NAME = "Funding";
        public static final String CLASS_5_NAME = "Expenses";
        public static final String CLASS_6_NAME = "Revenues";
        public static final String CLASS_7_NAME = "Off Balance";

        public static final String ACCUMULATED_DEP_GROUP_NAME = "Accumulated Depreciation";
        public static final String ACCUMULATED_DEP_GROUP_ID = "9";
        public static final String ACCUMULATED_DEP_ACCOUNT_PREFIX = "A. D. of ";

        public static final String DEPRECIATION_GROUP_NAME = "Depreciation";
        public static final String DEPRECIATION_GROUP_ID = "9";
        public static final String DEPRECIATION_ACCOUNT_PREFIX = "D. of ";

        public static final String CONSUMPTION_GROUP_NAME = "Consumption";
        public static final String CONSUMPTION_GROUP_ID = "8";
        public static final String CONSUMPTION_ACCOUNT_PREFIX = "C. of ";

        public static final String BALANCE_GROUP_NAME = "Rozvahove";
        public static final String BALANCE_GROUP_ID = "0";
        public static final String INIT_BALANCE_ACCOUNT_NAME = "Pociatocny ucet rozvazny";
        public static final String INIT_BALANCE_ACCOUNT_ID = "0";
        public static final String CLOSING_BALANCE_ACCOUNT_NAME = "Konecny ucet rozvazny";
        public static final String CLOSING_BALANCE_ACCOUNT_ID = "1";

        public static final String PROFIT_GROUP_NAME = "Vysledkove";
        public static final String PROFIT_GROUP_ID = "0";
        public static final String PROFIT_STATEMENT_ACCOUNT_NAME = "Vykaz zisku a strat";
        public static final String PROFIT_STATEMENT_ACCOUNT_ID = "0";

    }

    public static class Account {
        public static final String INIT_ACC_ID = "700.0";
        public static final String CLOSING_ACC_ID = "701.0";
        public static final String PROFIT_ACC_ID = "710.0";
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
