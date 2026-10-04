package org.kaleta.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import org.kaleta.model.FinancialAsset;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

@Data
@RegisterForReflection
public class FinancialAssetsDto
{
    private List<Group> groups = new ArrayList<>();

    @Data
    @RegisterForReflection
    public static class Group
    {
        private String name;

        private List<Account> accounts = new ArrayList<>();

        @Data
        @RegisterForReflection
        public static class Account
        {
            private String id;
            private String name;
            /** Whether the asset is still held, in the latest year of the books. */
            private Boolean active;

            private Integer initialValue;
            private Integer depositsSum;
            private Integer withdrawalsSum;
            private Integer currentValue;
            private BigDecimal currentReturn;
            /** The money-weighted return per year, in percent; null where there is none to state. */
            private BigDecimal annualReturn;

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

    /**
     * Cuts each asset's timeline down to the months it was in use: from the month money first went
     * into it, and - for an asset no longer held - up to the month of its last transaction.
     * <p>
     * The timelines are put together a year at a time, so they used to start in the January of the
     * year an asset was bought and run to the December of the year it was sold, with months of
     * nothing either side. An asset that already held a value when the books begin starts at their
     * start, as that value is where it began; an asset still held runs to the current month, as it
     * is worth something in every one of them.
     */
    public void trimToActivity()
    {
        for (Group group : groups)
        {
            for (Group.Account account : group.getAccounts())
            {
                int months = account.getLabels().length;
                int first = 0;
                if (account.getInitialValue() == null || account.getInitialValue() == 0) {
                    while (first < months && !movedIn(account, first)) first++;
                }
                int last = months - 1;
                if (!Boolean.TRUE.equals(account.getActive())) {
                    while (last >= first && !movedIn(account, last)) last--;
                }
                if (first > last) continue; // nothing ever moved: there is no stretch to keep
                if (first == 0 && last == months - 1) continue;

                int from = first;
                int to = last + 1;
                account.setLabels(Arrays.copyOfRange(account.getLabels(), from, to));
                account.setDeposits(Arrays.copyOfRange(account.getDeposits(), from, to));
                account.setWithdrawals(Arrays.copyOfRange(account.getWithdrawals(), from, to));
                account.setRevaluations(Arrays.copyOfRange(account.getRevaluations(), from, to));
                account.setBalances(Arrays.copyOfRange(account.getBalances(), from, to));
                account.setFunding(Arrays.copyOfRange(account.getFunding(), from, to));
                account.setCumulativeDeposits(Arrays.copyOfRange(account.getCumulativeDeposits(), from, to));
                account.setCumulativeWithdrawals(Arrays.copyOfRange(account.getCumulativeWithdrawals(), from, to));
            }
        }
    }

    /** Whether anything was put in, taken out or revalued in the given month. */
    private static boolean movedIn(Group.Account account, int month)
    {
        return account.getDeposits()[month] != 0 || account.getWithdrawals()[month] != 0 || account.getRevaluations()[month] != 0;
    }

    public static FinancialAssetsDto.Group.Account from(FinancialAsset asset)
    {
        FinancialAssetsDto.Group.Account accountDto = new FinancialAssetsDto.Group.Account();
        accountDto.setId(asset.getFullId());
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
