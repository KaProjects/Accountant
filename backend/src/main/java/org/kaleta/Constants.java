package org.kaleta;

public class Constants
{
    public static class Schema
    {
        public static final String FIN_GROUP_ID = "23";
        public static final String FIN_CREATION_ID = "546";
        public static final String FIN_REV_REVALUATION_ID = "624";
        public static final String FIN_EXP_REVALUATION_ID = "544";
    }

    public static class Account
    {
        public static final String INIT_ACC_ID = "700.0";
        public static final String CLOSING_ACC_ID = "701.0";
        public static final String PROFIT_ACC_ID = "710.0";
    }

    public enum AccountType
    {
        A, L, R, E, X
    }
}
