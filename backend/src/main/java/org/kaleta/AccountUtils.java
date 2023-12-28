package org.kaleta;

import org.kaleta.entity.Account;

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

    public static String getFinCreationAccountId(Account account)
    {
        validateFinAssetAccount(account);
        String id = account.getFullId();
        if (Integer.parseInt(account.getAccountId().getYear()) > 2020) {
            return Constants.Schema.FIN_CREATION_ID + "." + id.charAt(2) + "-" + id.split("\\.")[1];
        } else {
            return Constants.Schema.FIN_CREATION_ID  + "." + id.split("\\.")[1];
        }
    }

    public static String getFinRevRevaluationAccountId(Account account)
    {
        validateFinAssetAccount(account);
        String id = account.getFullId();
        if (Integer.parseInt(account.getAccountId().getYear()) > 2020) {
            return Constants.Schema.FIN_REV_REVALUATION_ID + "." + id.charAt(2) + "-" + id.split("\\.")[1];
        } else {
            return Constants.Schema.FIN_REV_REVALUATION_ID  + "." + id.split("\\.")[1];
        }
    }

    public static String getFinExpRevaluationAccountId(Account account)
    {
        validateFinAssetAccount(account);
        String id = account.getFullId();
        if (Integer.parseInt(account.getAccountId().getYear()) > 2020) {
            return Constants.Schema.FIN_EXP_REVALUATION_ID + "." + id.charAt(2) + "-" + id.split("\\.")[1];
        } else {
            return Constants.Schema.FIN_EXP_REVALUATION_ID  + "." + id.split("\\.")[1];
        }
    }
}
