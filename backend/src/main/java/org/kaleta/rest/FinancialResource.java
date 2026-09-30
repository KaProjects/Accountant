package org.kaleta.rest;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.dto.FinancialAssetsDto;
import org.kaleta.entity.Account;
import org.kaleta.model.FinancialAsset;
import org.kaleta.model.FinancialAssetsData;
import org.kaleta.model.FinancialAssetsOverallData;
import org.kaleta.service.FinancialService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/financial")
public class FinancialResource
{
    @Inject
    FinancialService financialService;

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
            dto.trimFutureMonths();
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
            FinancialAssetsDto dto = new FinancialAssetsDto();

            FinancialAssetsOverallData data = financialService.getFinancialAssetsOverallData();

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
        });
    }
}
