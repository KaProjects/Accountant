package org.kaleta.accountant.common;

public class Constants {

    public static class Context {
        public static final int PRODUCTION = 0;
        public static final int DEVEL = 1;
        public static final int TEST = 2;
    }

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

        public static final String RECEIVABLES_GROUP_NAME = "Pohladavky";
        public static final String RECEIVABLES_GROUP_ID = "0";
        public static final String RECEIVABLES_PERSONAL_ACCOUNT_NAME = "Pohladavky k Osobam";
        public static final String RECEIVABLES_PERSONAL_ACCOUNT_ID = "0";
        public static final String RECEIVABLES_INSTITUTION_ACCOUNT_NAME = "Pohladavky k Instituciam";
        public static final String RECEIVABLES_INSTITUTION_ACCOUNT_ID = "1";
        public static final String RECEIVABLES_COMPANY_ACCOUNT_NAME = "Pohladavky k Spolocnostiam";
        public static final String RECEIVABLES_COMPANY_ACCOUNT_ID = "2";

        public static final String LIABILITIES_GROUP_NAME = "Zavazky";
        public static final String LIABILITIES_GROUP_ID = "1";
        public static final String LIABILITIES_PERSONAL_ACCOUNT_NAME = "Zavazky k Osobam";
        public static final String LIABILITIES_PERSONAL_ACCOUNT_ID = "0";
        public static final String LIABILITIES_INSTITUTION_ACCOUNT_NAME = "Zavazky k Instituciam";
        public static final String LIABILITIES_INSTITUTION_ACCOUNT_ID = "1";
        public static final String LIABILITIES_COMPANY_ACCOUNT_NAME = "Zavazky k Spolocnostiam";
        public static final String LIABILITIES_COMPANY_ACCOUNT_ID = "2";

        public static final String CAPITAL_GROUP_NAME = "Capital";
        public static final String CAPITAL_GROUP_ID = "0";
        public static final String CAPITAL_PERSONAL_ACCOUNT_NAME = "Personal Capital";
        public static final String CAPITAL_PERSONAL_ACCOUNT_ID = "0";


        public static final String DEPRECIATION_GROUP_NAME = "Depreciation";
        public static final String DEPRECIATION_GROUP_ID = "0";
        public static final String DEPRECIATION_ACCOUNT_PREFIX = "D. of ";

        public static final String CONSUMPTION_GROUP_NAME = "Consumption";
        public static final String CONSUMPTION_GROUP_ID = "1";
        public static final String CONSUMPTION_ACCOUNT_PREFIX = "C. of ";

        public static final String BALANCE_GROUP_NAME = "Rozvahove";
        public static final String BALANCE_GROUP_ID = "0";
        public static final String INIT_BALANCE_ACCOUNT_NAME = "Pociatocny Ucet Rozvazny";
        public static final String INIT_BALANCE_ACCOUNT_ID = "0";
        public static final String CLOSING_BALANCE_ACCOUNT_NAME = "Konecny Ucet Rozvazny";
        public static final String CLOSING_BALANCE_ACCOUNT_ID = "1";

        public static final String PROFIT_GROUP_NAME = "Vysledkove";
        public static final String PROFIT_GROUP_ID = "0";
        public static final String PROFIT_STATEMENT_ACCOUNT_NAME = "Vykaz Zisku a Strat";
        public static final String PROFIT_STATEMENT_ACCOUNT_ID = "0";
    }

    public static class Account {
        public static final String INIT_ACC_ID = "700.0";
        public static final String CLOSING_ACC_ID = "701.0";
        public static final String PROFIT_ACC_ID = "710.0";

        public static final String PERSONAL_CAPITAL_ACC_ID = "400.0";
        public static final String ACCUMULATED_EARNINGS_ACC_ID = "401.0";

        public static final String GENERAL_ACCOUNT_NAME = "general";
    }

    public static class Color {
        public static final java.awt.Color INCOME_GREEN = java.awt.Color.getHSBColor(120/360f,0.5f,0.75f);
        public static final java.awt.Color EXPENSE_RED = java.awt.Color.getHSBColor(0/360f,0.5f,1);

        public static final java.awt.Color OVERVIEW_CLASS = java.awt.Color.getHSBColor(160 / 360f, 1f, 0.5f);
        public static final java.awt.Color OVERVIEW_GROUP = java.awt.Color.getHSBColor(200 / 360f, 1f, 0.5f);
        public static final java.awt.Color OVERVIEW_ACCOUNT = java.awt.Color.getHSBColor(290 / 360f, 1f, 0.5f);

        public static final java.awt.Color CASH_FLOW_PURPLE = java.awt.Color.getHSBColor(306/360f,0.4f,0.7f);
    }

    public static class AccountType {
        public static final String ASSET = "A";
        public static final String LIABILITY = "L";
        public static final String EXPENSE = "E";
        public static final String REVENUE = "R";
        public static final String OFF_BALANCE = "X";
    }

    public static class Transaction {
        public static final String OPEN_DESCRIPTION = "initiation";
        public static final String PURCHASE_DESCRIPTION = "purchase";
        public static final String MONTHLY_DEP_DESCRIPTION = "monthly depreciation";
        public static final String RESOURCE_ACQUIRED = "resource acquired";
        public static final String RESOURCE_CONSUMED = "resource consumed";
        public static final String CLOSE_DESCRIPTION = "closure";
    }
}
