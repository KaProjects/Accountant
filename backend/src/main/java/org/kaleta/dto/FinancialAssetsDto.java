package org.kaleta.dto;

import lombok.Data;
import org.kaleta.model.FinancialAsset;

import java.math.BigDecimal;
import java.util.ArrayList;
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
