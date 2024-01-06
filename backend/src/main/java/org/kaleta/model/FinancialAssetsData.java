package org.kaleta.model;

import org.kaleta.AccountUtils;
import org.kaleta.Constants;
import org.kaleta.Utils;
import org.kaleta.entity.Account;
import org.kaleta.entity.Transaction;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FinancialAssetsData
{
    private Map<String, List<Account>> finAssetAccounts;
    private List<Transaction> finAssetTransactions;
    private SchemaClass.Group group23;

    public FinancialAssetsData(Map<String, List<Account>> finAssetAccounts, List<Transaction> finAssetTransactions, SchemaClass.Group group23)
    {
        this.finAssetAccounts = finAssetAccounts;
        this.finAssetTransactions = finAssetTransactions;
        this.group23 = group23;
    }

    public Set<String> getAssetGroups()
    {
        return finAssetAccounts.keySet();
    }

    public List<Account> getAssetsByGroup(String schemaId) {
        return finAssetAccounts.get(schemaId);
    }

    public String getAssetGroupName(String schemaId) {return group23.getAccount(schemaId).getName();}

    public FinancialAsset getFinancialAsset(Account account)
    {
        AccountUtils.validateFinAssetAccount(account);
        String accountId = account.getFullId();

        FinancialAsset model = new FinancialAsset();
        model.setName(account.getName());
        model.setFullId(accountId);
        model.setInitialValue(findInitialValue(accountId));

        Integer[] revaluationsPositive = monthlyBalance(accountId, AccountUtils.getFinRevRevaluationAccountId(account));
        Integer[] revaluationsNegative = monthlyBalance(AccountUtils.getFinExpRevaluationAccountId(account), accountId);

        model.setRevaluations(Utils.subtractIntegerArrays(revaluationsPositive, revaluationsNegative));

        model.setDeposits(Utils.addIntegerArrays(
                monthlyBalance(AccountUtils.getFinCreationAccountId(account), ""),
                Utils.subtractIntegerArrays(monthlyBalance(accountId, ""), revaluationsPositive)
        ));

        model.setWithdrawals(Utils.subtractIntegerArrays(
                monthlyBalance("", accountId),
                revaluationsNegative
        ));

        model.setBalances(cumulativeMonthlyBalance(accountId));

        model.setLabels(constructLabels(account.getAccountId().getYear()));

        return model;
    }

    private String[] constructLabels(String year)
    {
        String[] labels = new String[12];
        for(int i=0;i<12;i++){
            labels[i] = String.valueOf(i+1) + "/" + year.substring(2,4);
        }
        return labels;
    }

    private Integer[] cumulativeMonthlyBalance(String accountId)
    {
        Integer[] monthlyBalance = Utils.initialMonthlyBalance();
        for (Transaction transaction : finAssetTransactions) {
            if (transaction.getDebit().equals(accountId) && !transaction.getCredit().startsWith("7")) {
                int month = Integer.parseInt(transaction.getDate().substring(2, 4));
                monthlyBalance[month - 1] += transaction.getAmount();
            }
            if (transaction.getCredit().equals(accountId) && !transaction.getDebit().startsWith("7")) {
                int month = Integer.parseInt(transaction.getDate().substring(2, 4));
                monthlyBalance[month - 1] -= transaction.getAmount();
            }
            if (transaction.getDebit().equals(accountId) && transaction.getCredit().equals(Constants.Account.INIT_ACC_ID)){
                monthlyBalance[0] += transaction.getAmount();
            }
        }
        return Utils.toCumulativeArray(monthlyBalance);
    }

    private Integer[] monthlyBalance(String debitId, String creditId)
    {
        Integer[] monthlyBalance = Utils.initialMonthlyBalance();
        for (Transaction transaction : finAssetTransactions) {
            if (((debitId.isEmpty() && !transaction.getDebit().startsWith("7")) || transaction.getDebit().equals(debitId))
                    && ((creditId.isEmpty() && !transaction.getCredit().startsWith("7")) || transaction.getCredit().equals(creditId))) {
                int month = Integer.parseInt(transaction.getDate().substring(2, 4));
                monthlyBalance[month - 1] += transaction.getAmount();
            }
        }
        return monthlyBalance;
    }

    private Integer findInitialValue(String accountId)
    {
        List<Transaction> initTransaction = finAssetTransactions.stream().filter(transaction -> transaction.getDebit().equals(accountId) && transaction.getCredit().equals(Constants.Account.INIT_ACC_ID)).collect(Collectors.toList());
        if (initTransaction.isEmpty()) throw new IllegalArgumentException("init transaction not found for account '" + accountId + "'");
        if (initTransaction.size() > 1) throw new IllegalArgumentException(initTransaction.size() + " init transaction found for account '" + accountId + "'");
        return initTransaction.get(0).getAmount();
    }
}
