package org.kaleta;

import org.kaleta.persistence.entity.Account;

public class AccountUtils
{
    public static void validateFinAssetAccount(Account account)
    {
        String fullId = account.getFullId();
        if (!fullId.startsWith("23")){
            throw new IllegalArgumentException("Only 23x accounts are financial assets, but was '" + fullId + "'");
        }
        if (!fullId.contains(".")){
            throw new IllegalArgumentException("Full account id required, e.i. 'groupId.semanticId', but was '" + fullId + "'");
        }
    }

    /**
     * The account a financial asset's creation - the money put into it - is booked against: 549,
     * then the asset's group digit and its own number, as 549.0-1 for the asset 230.1.
     * <p>
     * Every year of the books names it so. Up to 2020 the code expected an older form without the
     * group digit, which the books no longer use: it found no creations and no revaluations in those
     * years, and took every revaluation of an asset for a deposit or a withdrawal - which made what
     * funded an asset the same as what it was worth, month by month.
     */
    public static String getFinCreationAccountId(Account account)
    {
        return finAccountId(account, Constants.Schema.FIN_CREATION_ID);
    }

    /** The account a financial asset's gains in value are booked against, named as its creation's. */
    public static String getFinRevRevaluationAccountId(Account account)
    {
        return finAccountId(account, Constants.Schema.FIN_REV_REVALUATION_ID);
    }

    /** The account a financial asset's losses in value are booked against, named as its creation's. */
    public static String getFinExpRevaluationAccountId(Account account)
    {
        return finAccountId(account, Constants.Schema.FIN_EXP_REVALUATION_ID);
    }

    private static String finAccountId(Account account, String schemaId)
    {
        validateFinAssetAccount(account);
        String id = account.getFullId();
        return schemaId + "." + id.charAt(2) + "-" + id.split("\\.")[1];
    }

    /**
     * @return true if specified account type is debit type, false if credit type
     */
    public static boolean isDebit(Constants.AccountType type)
    {
        if (type.equals(Constants.AccountType.X)) {
            throw new IllegalArgumentException("Off-balance type is neither debit nor credit!");
        }
        return type.equals(Constants.AccountType.A) || type.equals(Constants.AccountType.E);
    }
}
