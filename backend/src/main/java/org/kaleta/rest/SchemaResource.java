package org.kaleta.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.kaleta.dto.YearSchemaDto;
import org.kaleta.rest.validation.ValidYear;
import org.kaleta.service.SchemaService;

@Path("/schema")
public class SchemaResource
{
    @Inject
    SchemaService schemaService;

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{year}")
    public Response getSchema(@PathParam("year") @ValidYear String year)
    {
        return Response.ok(YearSchemaDto.from(schemaService.getSchema(year))).build();
    }
}
