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
import org.kaleta.service.ViewService;

@Path("/view")
public class ViewResource
{
    @Inject
    ViewService viewService;

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{year}/vacation")
    public Response getVacations(@PathParam("year") @ValidYear String year)
    {
        return Response.ok(viewService.getVacations(year)).build();
    }

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{year}")
    public Response getViews(@PathParam("year") @ValidYear String year)
    {
        return Response.ok(viewService.getViews(year)).build();
    }
}
