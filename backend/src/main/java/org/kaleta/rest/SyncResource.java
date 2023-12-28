package org.kaleta.rest;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.service.SyncService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/sync")
public class SyncResource
{
    @ConfigProperty(name = "data.location")
    String dataLocation;

    @Inject
    SyncService service;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{year}")
    public Response syncYear(@PathParam String year)
    {
        return Endpoint.process(() -> ParamValidators.validateYear(year), () -> {
            try {
                String dataSource = dataLocation + year;
                return service.sync(dataSource);
            } catch (IOException ioe) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ioe.getMessage());
            }
        });
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/all")
    public Response syncAll()
    {
        return Endpoint.process(() -> {}, () -> {
            try {
                StringBuilder sb = new StringBuilder();
                for (String year : service.getYears(dataLocation)){
                    String dataSource = dataLocation + year;
                    sb.append(service.sync(dataSource));
                }
                return sb.toString();
            } catch (IOException ioe) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ioe.getMessage());
            }
        });

    }
}