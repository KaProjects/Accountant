package org.kaleta.rest;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.AccountUtils;
import org.kaleta.Utils;
import org.kaleta.dto.FinancialAssetsDto;
import org.kaleta.entity.Account;
import org.kaleta.entity.json.FinAssetsConfig;
import org.kaleta.model.FinancialAsset;
import org.kaleta.model.FinancialAssetsData;
import org.kaleta.service.AccountService;
import org.kaleta.service.FinancialService;
import org.kaleta.service.SchemaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

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
    public Response getFinancialAssetsProgress(@PathParam String year)
    {
        return Endpoint.process(() -> {
            ParamValidators.validateYear(year);
        }, () -> {
            FinancialAssetsDto dto = new FinancialAssetsDto();

            FinancialAssetsData data = financialService.getFinancialAssetsData(year);

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
            return dto;
        });
    }

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/assets")
    public Response getFinancialAssetsOverallProgress()
    {
        return Endpoint.process(() -> {}, () -> {
            FinAssetsConfig config;
            try {
                JsonMapper mapper = new JsonMapper();
                String json = inputStreamToString(getClass().getClassLoader().getResourceAsStream("fin_assets_config.json"));
                config = mapper.readValue(json, FinAssetsConfig.class);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }

            Map<String, FinancialAssetsData> data = new TreeMap<>();
            for (String year : config.getYears()) {
                data.put(year, financialService.getFinancialAssetsData(year));
            }

            FinancialAssetsDto dto = new FinancialAssetsDto();

            for (FinAssetsConfig.Group group : config.getGroups())
            {
                FinancialAssetsDto.Group groupDto = new FinancialAssetsDto.Group();
                groupDto.setName(group.getName().toUpperCase());

                for (FinAssetsConfig.Group.Account configAccount : group.getAccounts())
                {
                    FinancialAsset model = new FinancialAsset();
                    model.setName(configAccount.getName());

                    Integer[] deposits = new Integer[]{};
                    Integer[] revaluations = new Integer[]{};
                    Integer[] withdrawals = new Integer[]{};
                    String[] labels = new String[]{};
                    Integer[] balances = new Integer[]{};

                    for (FinAssetsConfig.Group.Account.Record record : configAccount.getRecords())
                    {
                        Account account = Account.from(record, configAccount.getName());
                        AccountUtils.validateFinAssetAccount(account);

                        FinancialAsset accountModel = data.get(record.getYear()).getFinancialAsset(account);

                        if (record.equals(configAccount.getRecords().get(0)))
                        {
                            model.setInitialValue(accountModel.getInitialValue());
                        }

                        deposits = Utils.concatArrays(deposits, accountModel.getDeposits());
                        revaluations = Utils.concatArrays(revaluations, accountModel.getRevaluations());
                        withdrawals = Utils.concatArrays(withdrawals, accountModel.getWithdrawals());
                        labels = Utils.concatArrays(labels, accountModel.getLabels());
                        balances = Utils.concatArrays(balances, accountModel.getBalances());
                    }

                    model.setDeposits(deposits);
                    model.setRevaluations(revaluations);
                    model.setWithdrawals(withdrawals);
                    model.setLabels(labels);
                    model.setBalances(balances);

                    groupDto.getAccounts().add(FinancialAssetsDto.from(model));
                }
                dto.getGroups().add(groupDto);
            }
            return dto;
        });
    }
}
