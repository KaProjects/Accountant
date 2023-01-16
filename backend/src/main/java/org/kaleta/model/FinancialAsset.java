package org.kaleta.model;

import lombok.Data;
import org.kaleta.Utils;

@Data
public class FinancialAsset {

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

    public Integer[] getMonthlyCumulativeDeposits() {
        return Utils.toCumulativeArray(deposits);
    }

    public Integer[] getMonthlyCumulativeWithdrawals() {
        return Utils.toCumulativeArray(withdrawals);
    }
}
