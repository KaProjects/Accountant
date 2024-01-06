package org.kaleta.service;

import org.kaleta.model.FinancialAssetsData;

public interface FinancialService
{
    /**
     * @return data for financial assets for specified year
     */
    FinancialAssetsData getFinancialAssetsData(String year);
}
