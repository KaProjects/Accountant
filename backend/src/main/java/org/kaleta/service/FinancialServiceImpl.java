package org.kaleta.service;

import org.kaleta.Constants;
import org.kaleta.entity.Account;
import org.kaleta.entity.Transaction;
import org.kaleta.model.FinancialAssetsData;
import org.kaleta.model.SchemaClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FinancialServiceImpl implements FinancialService
{
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final SchemaService schemaService;

    @Autowired
    public FinancialServiceImpl(TransactionService transactionService, AccountService accountService, SchemaService schemaService)
    {
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.schemaService = schemaService;
    }

    @Override
    public FinancialAssetsData getFinancialAssetsData(String year)
    {
        Map<String, List<Account>> finAssetAccounts = accountService.getFinancialAssetAccounts(year);
        SchemaClass class2 = schemaService.getClass(year, "2");
        List<Transaction> finAssetTransactions = transactionService.getFinancialAssetTransactions(year);
        return new FinancialAssetsData(finAssetAccounts, finAssetTransactions, class2.getGroup(Constants.Schema.FIN_GROUP_ID));
    }
}
