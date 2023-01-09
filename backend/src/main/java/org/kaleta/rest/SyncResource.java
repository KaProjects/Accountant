package org.kaleta.rest;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.service.SyncServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/sync")
public class SyncResource {

    @ConfigProperty(name = "data.location")
    String dataLocation;

    @Inject
    SyncServiceImpl service;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{year}")
    public String syncYear(@PathParam String year) {
        if (!year.matches("2\\d\\d\\d"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Year Parameter");
        String dataSource = dataLocation + year;
        return service.sync(dataSource);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/all")
    public String syncAll() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String year : service.getYears(dataLocation)){
            String dataSource = dataLocation + year;
            sb.append(service.sync(dataSource));
        }
        return sb.toString();
    }
}