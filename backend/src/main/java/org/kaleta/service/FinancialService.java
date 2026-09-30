package org.kaleta.service;

import org.kaleta.model.FinancialAssetsData;
import org.kaleta.model.FinancialAssetsOverallData;

public interface FinancialService
{
    /**
     * @return data for financial assets for specified year
     */
    FinancialAssetsData getFinancialAssetsData(String year);

    /**
     * @return data for financial assets of every year that has any, stitched into one timeline
     */
    FinancialAssetsOverallData getFinancialAssetsOverallData();
}
