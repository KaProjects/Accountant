package org.kaleta.rest;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.dto.YearSchemaDto;
import org.kaleta.service.SchemaService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/schema")
public class SchemaResource
{
    @Inject
    SchemaService schemaService;

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{year}")
    public Response getSchema(@PathParam String year)
    {
        return Endpoint.process(() -> {
            ParamValidators.validateYear(year);
        }, () -> YearSchemaDto.from(schemaService.getSchema(year)));
    }
}
