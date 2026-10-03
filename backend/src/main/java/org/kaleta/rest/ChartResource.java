package org.kaleta.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.kaleta.rest.validation.ValidChartId;
import org.kaleta.service.ChartService;

@Path("/chart")
public class ChartResource
{
    @Inject
    ChartService chartService;

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/config")
    public Response getChartConfigs()
    {
        return Response.ok(chartService.getChartConfigs()).build();
    }

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/data/{id}")
    public Response getChartData(@PathParam("id") @ValidChartId String id)
    {
        return Response.ok(chartService.getChartData(id)).build();
    }
}
