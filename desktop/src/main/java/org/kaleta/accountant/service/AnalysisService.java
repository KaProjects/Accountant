package org.kaleta.accountant.service;

import java.math.BigDecimal;

/**
 * Provides computing of financial analysis indicators.
 */
public class AnalysisService {

    AnalysisService() {
        // package-private
    }

    public BigDecimal computeL1(){


        //Service.ACCOUNT.getAllAccounts()


        return new BigDecimal("50").divide(new BigDecimal("90"), 4, BigDecimal.ROUND_HALF_UP);
    }

}
