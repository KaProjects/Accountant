package org.kaleta.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.kaleta.Constants;
import org.kaleta.dto.FinancialAssetsDto;
import org.kaleta.model.FinancialAsset;
import org.kaleta.model.FinancialAssetsData;
import org.kaleta.model.FinancialAssetsOverallData;
import org.kaleta.model.SchemaClass;
import org.kaleta.persistence.entity.Account;
import org.kaleta.persistence.entity.Transaction;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@ApplicationScoped
public class FinancialService
{
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final SchemaService schemaService;

    @Inject
    public FinancialService(TransactionService transactionService, AccountService accountService, SchemaService schemaService)
    {
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.schemaService = schemaService;
    }

    /**
     * @return data for financial assets for specified year
     */
    public FinancialAssetsData getFinancialAssetsData(String year)
    {
        Map<String, List<Account>> finAssetAccounts = accountService.getFinancialAssetAccounts(year);
        SchemaClass class2 = schemaService.getClass(year, "2");
        List<Transaction> finAssetTransactions = transactionService.getFinancialAssetTransactions(year);
        return new FinancialAssetsData(finAssetAccounts, finAssetTransactions, class2.getGroup(Constants.Schema.FIN_GROUP_ID));
    }

    /**
     * @return data for financial assets of every year that has any, stitched into one timeline
     */
    public FinancialAssetsOverallData getFinancialAssetsOverallData()
    {
        Map<String, FinancialAssetsData> dataByYear = new TreeMap<>();
        for (String year : schemaService.getYears()) {
            dataByYear.put(year, getFinancialAssetsData(year));
        }
        return new FinancialAssetsOverallData(dataByYear);
    }

    /**
     * @return the financial assets of the specified year, by group, each account month by month,
     * up to the current month
     */
    public FinancialAssetsDto getFinancialAssets(String year)
    {
        FinancialAssetsDto dto = new FinancialAssetsDto();

        FinancialAssetsData data = getFinancialAssetsData(year);

        for (String schemaId : data.getAssetGroups())
        {
            FinancialAssetsDto.Group groupDto = new FinancialAssetsDto.Group();
            groupDto.setName(data.getAssetGroupName(schemaId).toUpperCase());

            for (Account account : data.getAssetsByGroup(schemaId))
            {
                FinancialAsset asset = data.getFinancialAsset(account);
                groupDto.getAccounts().add(FinancialAssetsDto.from(asset));
            }
            dto.getGroups().add(groupDto);
        }
        dto.trimFutureMonths();
        return dto;
    }

    /**
     * @return the financial assets of every year that has any, by group, each asset month by
     * month across the years, up to the current month
     */
    public FinancialAssetsDto getFinancialAssetsOverall()
    {
        FinancialAssetsDto dto = new FinancialAssetsDto();

        FinancialAssetsOverallData data = getFinancialAssetsOverallData();

        for (String schemaId : data.getAssetGroups())
        {
            FinancialAssetsDto.Group groupDto = new FinancialAssetsDto.Group();
            groupDto.setName(data.getAssetGroupName(schemaId).toUpperCase());

            for (String assetId : data.getAssetIds(schemaId))
            {
                FinancialAsset asset = data.getFinancialAsset(schemaId, assetId);
                groupDto.getAccounts().add(FinancialAssetsDto.from(asset));
            }
            dto.getGroups().add(groupDto);
        }
        dto.trimFutureMonths();
        return dto;
    }
}
