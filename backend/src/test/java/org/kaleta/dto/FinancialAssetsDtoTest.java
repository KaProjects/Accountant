package org.kaleta.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;

class FinancialAssetsDtoTest
{
    /**
     * An asset over one year, January to December, with a deposit, withdrawal and revaluation in
     * the given months and nothing in the others.
     */
    private static FinancialAssetsDto.Group.Account asset(int initialValue, boolean active, int depositMonth, int lastMonth)
    {
        FinancialAssetsDto.Group.Account account = new FinancialAssetsDto.Group.Account();
        account.setInitialValue(initialValue);
        account.setActive(active);
        Integer[] deposits = zeros();
        Integer[] withdrawals = zeros();
        Integer[] revaluations = zeros();
        deposits[depositMonth] = 1000;
        revaluations[depositMonth + 1] = 100;
        withdrawals[lastMonth] = 1100;
        String[] labels = new String[12];
        for (int month = 0; month < 12; month++) labels[month] = (month + 1) + "/24";
        account.setLabels(labels);
        account.setDeposits(deposits);
        account.setWithdrawals(withdrawals);
        account.setRevaluations(revaluations);
        account.setBalances(zeros());
        account.setFunding(zeros());
        account.setCumulativeDeposits(zeros());
        account.setCumulativeWithdrawals(zeros());
        return account;
    }

    private static Integer[] zeros()
    {
        Integer[] values = new Integer[12];
        Arrays.fill(values, 0);
        return values;
    }

    private static FinancialAssetsDto.Group.Account trimmed(FinancialAssetsDto.Group.Account account)
    {
        FinancialAssetsDto dto = new FinancialAssetsDto();
        FinancialAssetsDto.Group group = new FinancialAssetsDto.Group();
        group.getAccounts().add(account);
        dto.getGroups().add(group);
        dto.trimToActivity();
        return account;
    }

    @Test
    void anAssetBoughtFromNothingStartsInTheMonthOfItsFirstDeposit()
    {
        FinancialAssetsDto.Group.Account account = trimmed(asset(0, true, 3, 8));

        assertThat(account.getLabels()[0], is("4/24"));
        assertThat(account.getDeposits()[0], is(1000));
    }

    @Test
    void anAssetStillHeldRunsToTheEndOfItsTimeline()
    {
        assertThat(trimmed(asset(0, true, 3, 8)).getLabels().length, is(9));
    }

    @Test
    void anAssetNoLongerHeldEndsInTheMonthOfItsLastTransaction()
    {
        FinancialAssetsDto.Group.Account account = trimmed(asset(0, false, 3, 8));

        assertThat(account.getLabels(), is(arrayContaining("4/24", "5/24", "6/24", "7/24", "8/24", "9/24")));
        // every series is cut alike
        assertThat(account.getBalances().length, is(6));
        assertThat(account.getFunding().length, is(6));
        assertThat(account.getCumulativeWithdrawals().length, is(6));
        assertThat(account.getWithdrawals()[5], is(1100));
    }

    @Test
    void anAssetThatAlreadyHeldAValueStartsWhereItsTimelineDoes()
    {
        // its value at the start of the books is where it began, though nothing moved for months
        assertThat(trimmed(asset(500, false, 3, 8)).getLabels()[0], is("1/24"));
    }
}
