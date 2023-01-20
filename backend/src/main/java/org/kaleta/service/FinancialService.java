package org.kaleta.service;

import org.kaleta.entity.Account;
import org.kaleta.entity.json.FinAssetsConfig;
import org.kaleta.model.FinancialAsset;

public interface FinancialService
{
    /**
     * @return financial asset for specified financial asset account
     */
    FinancialAsset getFinancialAsset(Account account);

    /**
     * @return financial asset for specified financial asset config
     */
    FinancialAsset getFinancialAsset(FinAssetsConfig.Group.Account account);
}
