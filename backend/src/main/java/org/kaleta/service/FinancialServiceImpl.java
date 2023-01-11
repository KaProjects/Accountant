package org.kaleta.service;

import org.kaleta.Utils;
import org.kaleta.entity.Account;
import org.kaleta.entity.json.FinAssetsConfig;
import org.kaleta.model.FinancialAsset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FinancialServiceImpl implements FinancialService {

    @Autowired
    TransactionService transactionService;

    @Autowired
    AccountService accountService;

    @Override
    public FinancialAsset getFinancialAsset(Account account){
        FinancialAsset model = new FinancialAsset();
        model.setName(account.getName());
        model.setFullId(account.getFullId());

        model.setInitialValue(transactionService.getInitialValue(account));

        String year = account.getAccountId().getYear();
        String accountId = account.getFullId();

        model.setDeposits(transactionService.monthlyBalanceByAccounts(
                year, accountService.getFinCreationAccountId(accountId), ""));

        Integer[] revaluationsPositive = transactionService.monthlyBalanceByAccounts(
                year, accountId, accountService.getFinRevRevaluationAccountId(accountId));

        Integer[] revaluationsNegative = transactionService.monthlyBalanceByAccounts(
                year, accountService.getFinExpRevaluationAccountId(accountId), accountId);

        model.setRevaluations(Utils.subtractIntegerArrays(revaluationsPositive, revaluationsNegative));

        model.setWithdrawals(Utils.subtractIntegerArrays(
                transactionService.monthlyBalanceByAccounts(year, "", accountId),
                revaluationsNegative
        ));

        model.setLabels(constructLabels(year));

        return model;
    }

    private String[] constructLabels(String year){
        String[] labels = new String[12];
        for(int i=0;i<12;i++){
            labels[i] = String.valueOf(i+1) + "/" + year.substring(2,4);
        }
        return labels;
    }

    @Override
    public FinancialAsset getFinancialAsset(FinAssetsConfig.Group.Account account) {
        FinancialAsset model = new FinancialAsset();
        model.setName(account.getName());

        FinAssetsConfig.Group.Account.Record firstRecord = account.getRecords().get(0);
        model.setInitialValue(transactionService.getInitialValue(accountService.getAccount(firstRecord.getYear(), firstRecord.getId())));

        Integer[] deposits = new Integer[]{};
        Integer[] revaluationsPositive = new Integer[]{};
        Integer[] revaluationsNegative = new Integer[]{};
        Integer[] withdrawals = new Integer[]{};
        String[] labels = new String[]{};

        for (FinAssetsConfig.Group.Account.Record record : account.getRecords()){

            Integer[] depositsAdd = transactionService.monthlyBalanceByAccounts(
                    record.getYear(), accountService.getFinCreationAccountId(record.getId()), "");
            deposits = Utils.concatArrays(deposits, depositsAdd);

            Integer[] revaluationsPositiveAdd = transactionService.monthlyBalanceByAccounts(
                    record.getYear(), record.getId(), accountService.getFinRevRevaluationAccountId(record.getId()));
            revaluationsPositive = Utils.concatArrays(revaluationsPositive, revaluationsPositiveAdd);

            Integer[] revaluationsNegativeAdd = transactionService.monthlyBalanceByAccounts(
                    record.getYear(), accountService.getFinExpRevaluationAccountId(record.getId()), record.getId());
            revaluationsNegative = Utils.concatArrays(revaluationsNegative, revaluationsNegativeAdd);

            Integer[] withdrawalsAdd = transactionService.monthlyBalanceByAccounts(record.getYear(), "", record.getId());
            withdrawals = Utils.concatArrays(withdrawals, withdrawalsAdd);

            String[] labelsAdd = constructLabels(record.getYear());
            labels = Utils.concatArrays(labels, labelsAdd);
        }

        model.setDeposits(deposits);

        model.setRevaluations(Utils.subtractIntegerArrays(revaluationsPositive, revaluationsNegative));

        model.setWithdrawals(Utils.subtractIntegerArrays(
                withdrawals,
                revaluationsNegative
        ));

        model.setLabels(labels);

        return model;
    }
}
