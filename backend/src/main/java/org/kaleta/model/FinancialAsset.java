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

    /**
     *
     * valuation = initial value +/- revaluations - withdrawals
     * @return cumulative valuation
     */
    public Integer[] getMonthlyCumulativeValuation()
    {
        Integer[] monthlyValuation = Utils.subtractIntegerArrays(revaluations, withdrawals);

        return Utils.toCumulativeArray(initialValue, monthlyValuation);
    }

    /**
     *
     * funding = initial value + deposits - withdrawals
     * @return cumulative funding
     */
    public Integer[] getMonthlyCumulativeFunding()
    {
        Integer[] monthlyFunding = Utils.subtractIntegerArrays(deposits, withdrawals);

        return Utils.toCumulativeArray(initialValue, monthlyFunding);
    }

    public String[] getLabels(){
        return Utils.concatArrays(new String[]{"0"}, labels);
    }

}
