package org.kaleta.rest;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.Constants;
import org.kaleta.dto.FinancialAssetsDto;
import org.kaleta.entity.Account;
import org.kaleta.entity.Schema;
import org.kaleta.entity.json.FinAssetsConfig;
import org.kaleta.model.FinancialAsset;
import org.kaleta.service.AccountService;
import org.kaleta.service.FinancialService;
import org.kaleta.service.SchemaService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static org.kaleta.Utils.inputStreamToString;

@Path("/financial")
public class FinancialResource
{
    @Inject
    FinancialService financialService;

    @Inject
    SchemaService schemaService;

    @Inject
    AccountService accountService;

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/assets/{year}")
    public FinancialAssetsDto getFinancialAssetsProgress(@PathParam String year)
    {
        ParamValidators.validateYear(year);

        FinancialAssetsDto dto = new FinancialAssetsDto();

        for (Schema schemaAcc : schemaService.getSchemaAccountsByGroup(year, Constants.Schema.FIN_GROUP_ID))
        {
            FinancialAssetsDto.Group groupDto = new FinancialAssetsDto.Group();
            groupDto.setName(schemaAcc.getName().toUpperCase());

            for (Account account : accountService.listBySchemaId(year, schemaAcc.getYearId().getId()))
            {
                FinancialAsset asset = financialService.getFinancialAsset(account);
                groupDto.getAccounts().add(constructAccountDto(asset));
            }

            dto.getGroups().add(groupDto);
        }

        return dto;
    }

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/assets")
    public FinancialAssetsDto getFinancialAssetsProgress() throws IOException
    {
        JsonMapper mapper = new JsonMapper();
        String json = inputStreamToString(getClass().getClassLoader().getResourceAsStream("fin_assets_config.json"));
        FinAssetsConfig config = mapper.readValue(json, FinAssetsConfig.class);

        FinancialAssetsDto dto = new FinancialAssetsDto();

        for (FinAssetsConfig.Group group : config.getGroups())
        {
            FinancialAssetsDto.Group groupDto = new FinancialAssetsDto.Group();
            groupDto.setName(group.getName().toUpperCase());

            for (FinAssetsConfig.Group.Account account : group.getAccounts())
            {
                FinancialAsset asset = financialService.getFinancialAsset(account);
                groupDto.getAccounts().add(constructAccountDto(asset));
            }

            dto.getGroups().add(groupDto);
        }

        return dto;
    }

    private FinancialAssetsDto.Group.Account constructAccountDto(FinancialAsset asset)
    {
        FinancialAssetsDto.Group.Account accountDto = new FinancialAssetsDto.Group.Account();
        accountDto.setName(asset.getName());
        accountDto.setInitialValue(asset.getInitialValue());
        accountDto.setWithdrawalsSum(asset.getWithdrawalsSum());
        accountDto.setDepositsSum(asset.getDepositsSum());
        accountDto.setCurrentValue(asset.getCurrentValue());
        accountDto.setCurrentReturn(asset.getCurrentReturn());
        accountDto.setFunding(asset.getMonthlyCumulativeFunding());
        accountDto.setDeposits(asset.getDeposits());
        accountDto.setRevaluations(asset.getRevaluations());
        accountDto.setWithdrawals(asset.getWithdrawals());
        accountDto.setLabels(asset.getLabels());
        accountDto.setBalances(asset.getBalances());
        accountDto.setCumulativeDeposits(asset.getMonthlyCumulativeDeposits());
        accountDto.setCumulativeWithdrawals(asset.getMonthlyCumulativeWithdrawals());
        return accountDto;
    }
}
