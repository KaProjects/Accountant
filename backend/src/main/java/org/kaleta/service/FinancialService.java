package org.kaleta.service;

import org.kaleta.entity.Account;
import org.kaleta.entity.json.FinAssetsConfig;
import org.kaleta.model.FinancialAsset;

public interface FinancialService {

    FinancialAsset getFinancialAsset(Account account);

    FinancialAsset getFinancialAsset(FinAssetsConfig.Group.Account account);
}
