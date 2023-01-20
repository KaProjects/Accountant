package org.kaleta.service;

import org.kaleta.Utils;
import org.kaleta.entity.Account;
import org.kaleta.entity.json.FinAssetsConfig;
import org.kaleta.model.FinancialAsset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.kaleta.service.AccountServiceImpl.validateFinAssetAccount;

@Service
public class FinancialServiceImpl implements FinancialService
{
    @Autowired
    TransactionService transactionService;

    @Autowired
    AccountService accountService;

    @Override
    public FinancialAsset getFinancialAsset(Account account)
    {
        validateFinAssetAccount(account.getFullId());

        FinancialAsset model = new FinancialAsset();
        model.setName(account.getName());
        model.setFullId(account.getFullId());

        model.setInitialValue(transactionService.getInitialValue(account));

        String year = account.getAccountId().getYear();
        String accountId = account.getFullId();

        Integer[] revaluationsPositive = transactionService.monthlyBalanceByAccounts(
                year, accountId, accountService.getFinRevRevaluationAccountId(account));

        Integer[] revaluationsNegative = transactionService.monthlyBalanceByAccounts(
                year, accountService.getFinExpRevaluationAccountId(account), accountId);

        model.setRevaluations(Utils.subtractIntegerArrays(revaluationsPositive, revaluationsNegative));

        model.setDeposits(Utils.addIntegerArrays(
                transactionService.monthlyBalanceByAccounts(year, accountService.getFinCreationAccountId(account), ""),
                Utils.subtractIntegerArrays(
                        transactionService.monthlyBalanceByAccounts(year, accountId, ""),
                        revaluationsPositive
                )
        ));

        model.setWithdrawals(Utils.subtractIntegerArrays(
                transactionService.monthlyBalanceByAccounts(year, "", accountId),
                revaluationsNegative
        ));

        model.setBalances(transactionService.monthlyBalanceByAccount(account));

        model.setLabels(constructLabels(year));

        return model;
    }

    @Override
    public FinancialAsset getFinancialAsset(FinAssetsConfig.Group.Account configAccount)
    {
        FinancialAsset model = new FinancialAsset();
        model.setName(configAccount.getName());

        FinAssetsConfig.Group.Account.Record firstRecord = configAccount.getRecords().get(0);
        model.setInitialValue(transactionService.getInitialValue(accountService.getAccount(firstRecord.getYear(), firstRecord.getId())));

        Integer[] deposits = new Integer[]{};
        Integer[] revaluations = new Integer[]{};
        Integer[] withdrawals = new Integer[]{};
        String[] labels = new String[]{};
        Integer[] balances = new Integer[]{};

        for (FinAssetsConfig.Group.Account.Record record : configAccount.getRecords()){
            validateFinAssetAccount(record.getId());

            Account account = accountService.getAccount(record.getYear(), record.getId());
            FinancialAsset accountModel = this.getFinancialAsset(account);

            deposits = Utils.concatArrays(deposits, accountModel.getDeposits());
            revaluations = Utils.concatArrays(revaluations, accountModel.getRevaluations());
            withdrawals = Utils.concatArrays(withdrawals, accountModel.getWithdrawals());
            labels = Utils.concatArrays(labels, accountModel.getLabels());
            balances = Utils.concatArrays(balances, accountModel.getBalances());
        }

        model.setDeposits(deposits);
        model.setRevaluations(revaluations);
        model.setWithdrawals(withdrawals);
        model.setLabels(labels);
        model.setBalances(balances);

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
}
