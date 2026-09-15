package org.kaleta.rest;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.service.SyncService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/all/validate")
    public Response syncValidateData()
    {
        return Endpoint.process(() -> {}, () -> {
            try {
                StringBuilder sb = new StringBuilder();
                boolean hasError = false;
                for (String year : service.getYears(dataLocation)){
                    sb.append(service.sync(dataLocation + year));
                    String message = service.validate(year, service.isActive(dataLocation, year));
                    if (!message.contains("data valid")) hasError = true;
                    sb.append(message).append("\n");
                }
                if (hasError) {
                    throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, sb.toString());
                } else {
                    return sb.toString();
                }
            } catch (IOException ioe) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ioe.getMessage());
            }
        });
    }
}