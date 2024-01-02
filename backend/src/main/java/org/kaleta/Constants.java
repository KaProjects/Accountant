package org.kaleta;

public class Constants
{
    public static class Schema
    {
        public static final String FIN_GROUP_ID = "23";
        public static final String FIN_CREATION_ID = "546";
        public static final String FIN_REV_REVALUATION_ID = "624";
        public static final String FIN_EXP_REVALUATION_ID = "544";
        public static final String ACCUMULATED_DEP_GROUP_ID = "09";
        public static final String DEPRECIATION_GROUP_ID = "50";
        public static final String CONSUMPTION_GROUP_ID = "51";
    }

    public static class Account
    {
        public static final String INIT_ACC_ID = "700.0";
        public static final String CLOSING_ACC_ID = "701.0";
        public static final String PROFIT_ACC_ID = "710.0";
        public static final String PERSONAL_CAPITAL_ACC_ID = "400.0";
        public static final String ACCUMULATED_EARNINGS_ACC_ID = "401.0";
    }

    public enum AccountType
    {
        A, L, R, E, X
    }
}
