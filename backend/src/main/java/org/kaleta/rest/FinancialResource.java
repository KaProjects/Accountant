package org.kaleta.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.kaleta.rest.validation.ValidYear;
import org.kaleta.service.FinancialService;

@Path("/financial")
public class FinancialResource
{
    @Inject
    FinancialService financialService;

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/assets/{year}")
    public Response getFinancialAssetsProgress(@PathParam("year") @ValidYear String year)
    {
        return Response.ok(financialService.getFinancialAssets(year)).build();
    }

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/assets")
    public Response getFinancialAssetsOverallProgress()
    {
        return Response.ok(financialService.getFinancialAssetsOverall()).build();
    }
}
