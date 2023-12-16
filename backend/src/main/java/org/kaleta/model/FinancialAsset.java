package org.kaleta.model;

import lombok.Data;
import org.kaleta.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
public class FinancialAsset
{
    private String fullId;
    private String name;

    private Integer initialValue;
    private Integer[] deposits;
    private Integer[] withdrawals;
    private Integer[] revaluations;

    private String[] labels;

    private Integer[] balances;

    /**
     *
     * funding = initial value + deposits - withdrawals
     * @return cumulative funding
     */
    public Integer[] getMonthlyCumulativeFunding()
    {
        Integer[] monthlyFunding = Utils.subtractIntegerArrays(deposits, withdrawals);
        monthlyFunding[0] += initialValue;

        return Utils.toCumulativeArray(monthlyFunding);
    }

    public Integer[] getMonthlyCumulativeDeposits()
    {
        return Utils.toCumulativeArray(deposits);
    }

    public Integer[] getMonthlyCumulativeWithdrawals()
    {
        return Utils.toCumulativeArray(withdrawals);
    }

    public Integer getDepositsSum() {
        return Utils.sumArray(deposits);
    }

    public Integer getWithdrawalsSum() {
        return Utils.sumArray(withdrawals);
    }

    public Integer getCurrentValue() {
        return balances[balances.length - 1];
    }

    public BigDecimal getCurrentReturn() {
        BigDecimal funding = BigDecimal.valueOf(initialValue + getDepositsSum());
        BigDecimal realisation = BigDecimal.valueOf(getCurrentValue() + getWithdrawalsSum());
        return realisation.divide(funding, 4, RoundingMode.HALF_UP).subtract(new BigDecimal(1)).multiply(new BigDecimal(100));
    }
}
