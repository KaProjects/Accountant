package org.kaleta.rest;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
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
public class FinancialResource {

    @Inject
    FinancialService financialService;

    @Inject
    SchemaService schemaService;

    @Inject
    AccountService accountService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    @Path("/assets/{year}")
    public FinancialAssetsDto getFinancialAssetsProgress(@PathParam String year)
    {
        InputValidators.validateYear(year);

        FinancialAssetsDto dto = new FinancialAssetsDto();

        for (Schema schemaAcc : schemaService.getSchemaAccountsByGroup(year, Constants.Schema.FIN_GROUP_ID)){
            FinancialAssetsDto.Group groupDto = new FinancialAssetsDto.Group();
            groupDto.setName(schemaAcc.getName());

            for (Account account : accountService.listBySchemaId(year, schemaAcc.getYearId().getId())){
                FinancialAssetsDto.Group.Account accountDto = new FinancialAssetsDto.Group.Account();
                accountDto.setName(account.getName());

                FinancialAsset asset = financialService.getFinancialAsset(account);
                accountDto.setValuationSequence(asset.getMonthlyCumulativeValuation());
                accountDto.setFundingSequence(asset.getMonthlyCumulativeFunding());
                accountDto.setDeposits(asset.getDeposits());
                accountDto.setRevaluations(asset.getRevaluations());
                accountDto.setWithdrawals(asset.getWithdrawals());
                accountDto.setLabels(asset.getLabels());

                groupDto.getAccounts().add(accountDto);
            }

            dto.getGroups().add(groupDto);
        }

        return dto;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    @Path("/assets")
    public FinancialAssetsDto getFinancialAssetsProgress() throws IOException {
        // TODO: 11.01.2023 construct FinancialAsset for every year, not one for all
        // TODO: 11.01.2023 try concat years with initial value for every year as base

        // TODO: 11.01.2023 for current asset: check previous years for account (avoid config.json)


        JsonMapper mapper = new JsonMapper();
        String json = inputStreamToString(getClass().getClassLoader().getResourceAsStream("fin_assets.json"));
        FinAssetsConfig config = mapper.readValue(json, FinAssetsConfig.class);

        FinancialAssetsDto dto = new FinancialAssetsDto();

        for (FinAssetsConfig.Group group : config.getGroups()){
            FinancialAssetsDto.Group groupDto = new FinancialAssetsDto.Group();
            groupDto.setName(group.getName());

            for (FinAssetsConfig.Group.Account account : group.getAccounts()){
                FinancialAssetsDto.Group.Account accountDto = new FinancialAssetsDto.Group.Account();
                accountDto.setName(account.getName());

                FinancialAsset asset = financialService.getFinancialAsset(account);
                accountDto.setValuationSequence(asset.getMonthlyCumulativeValuation());
                accountDto.setFundingSequence(asset.getMonthlyCumulativeFunding());
                accountDto.setDeposits(asset.getDeposits());
                accountDto.setRevaluations(asset.getRevaluations());
                accountDto.setWithdrawals(asset.getWithdrawals());
                accountDto.setLabels(asset.getLabels());

                groupDto.getAccounts().add(accountDto);
            }

            dto.getGroups().add(groupDto);
        }

        return dto;
    }
}
