package org.kaleta.dto;

import lombok.Data;
import org.kaleta.model.FinancialAsset;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

@Data
public class FinancialAssetsDto
{
    private List<Group> groups = new ArrayList<>();

    @Data
    public static class Group
    {
        private String name;

        private List<Account> accounts = new ArrayList<>();

        @Data
        public static class Account
        {
            private String name;

            private Integer initialValue;
            private Integer depositsSum;
            private Integer withdrawalsSum;
            private Integer currentValue;
            private BigDecimal currentReturn;

            private Integer[] deposits;
            private Integer[] withdrawals;
            private Integer[] revaluations;

            private String[] labels;

            private Integer[] balances;
            private Integer[] funding;
            private Integer[] cumulativeDeposits;
            private Integer[] cumulativeWithdrawals;
        }
    }

    public void trimFutureMonths()
    {
        for (Group group : groups)
        {
            for (Group.Account account : group.getAccounts())
            {
                String[] labels = account.getLabels();
                Integer futureIndex = null;
                for (int i=0;i<labels.length;i++)
                {
                    int month = Integer.parseInt(labels[i].split("/")[0]);
                    int year = Integer.parseInt(labels[i].split("/")[1]) + 2000;
                    if (year == new GregorianCalendar().get(Calendar.YEAR) && month > new GregorianCalendar().get(Calendar.MONTH) + 1)
                    {
                        futureIndex = i;
                        break;
                    }
                }
                if (futureIndex != null)
                {
                    account.setLabels(Arrays.copyOfRange(account.getLabels(), 0, futureIndex));

                    account.setDeposits(Arrays.copyOfRange(account.getDeposits(), 0, futureIndex));
                    account.setWithdrawals(Arrays.copyOfRange(account.getWithdrawals(), 0, futureIndex));
                    account.setRevaluations(Arrays.copyOfRange(account.getRevaluations(), 0, futureIndex));

                    account.setBalances(Arrays.copyOfRange(account.getBalances(), 0, futureIndex));
                    account.setFunding(Arrays.copyOfRange(account.getFunding(), 0, futureIndex));
                    account.setCumulativeDeposits(Arrays.copyOfRange(account.getCumulativeDeposits(), 0, futureIndex));
                    account.setCumulativeWithdrawals(Arrays.copyOfRange(account.getCumulativeWithdrawals(), 0, futureIndex));
                }
            }
        }
    }

    public static FinancialAssetsDto.Group.Account from(FinancialAsset asset)
    {
        FinancialAssetsDto.Group.Account accountDto = new FinancialAssetsDto.Group.Account();
        accountDto.setName(asset.getName());
        accountDto.setInitialValue(asset.getInitialValue());
        accountDto.setWithdrawalsSum(asset.getWithdrawalsSum());
        accountDto.setDepositsSum(asset.getDepositsSum());
        accountDto.setCurrentValue(asset.getCurrentValue());
        accountDto.setCurrentReturn(asset.getCurrentReturn());
        accountDto.setFunding(asset.getMonthlyCumulativeFunding());
        accountDto.setDeposits(asset.getDeposits());
        accountDto.setRevaluations(asset.getRevaluations());
        accountDto.setWithdrawals(asset.getWithdrawals());
        accountDto.setLabels(asset.getLabels());
        accountDto.setBalances(asset.getBalances());
        accountDto.setCumulativeDeposits(asset.getMonthlyCumulativeDeposits());
        accountDto.setCumulativeWithdrawals(asset.getMonthlyCumulativeWithdrawals());
        return accountDto;
    }
}
