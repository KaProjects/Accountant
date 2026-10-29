package org.kaleta.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.kaleta.rest.error.DataSourceException;
import org.kaleta.rest.error.InvalidDataException;
import org.kaleta.rest.validation.ValidYear;
import org.kaleta.service.SyncService;

import java.io.IOException;

@Path("/sync")
public class SyncResource
{
    @ConfigProperty(name = "data.location")
    String dataLocation;

    @Inject
    SyncService service;

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{year}")
    public Response syncYear(@PathParam("year") @ValidYear String year)
    {
        try {
            return Response.ok(service.sync(dataLocation, year)).build();
        } catch (IOException ioe) {
            throw new DataSourceException(ioe);
        }
    }

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/all")
    public Response syncAll()
    {
        try {
            return Response.ok(service.syncAll(dataLocation)).build();
        } catch (IOException ioe) {
            throw new DataSourceException(ioe);
        }
    }

    /**
     * Syncs every year and checks each one. A year that fails its checks makes the whole answer
     * 422 Unprocessable Content, whose detail is the same report a passing run answers with.
     */
    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/all/validate")
    public Response syncValidateData()
    {
        SyncService.SyncReport report;
        try {
            report = service.syncAndValidateAll(dataLocation);
        } catch (IOException ioe) {
            throw new DataSourceException(ioe);
        }
        if (!report.valid()) {
            throw new InvalidDataException(report.text());
        }
        return Response.ok(report.text()).build();
    }
}
