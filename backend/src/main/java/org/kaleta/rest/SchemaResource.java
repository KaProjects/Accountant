package org.kaleta.rest;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.dto.SchemaDto;
import org.kaleta.entity.Schema;
import org.kaleta.service.SchemaService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

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
    public List<SchemaDto> getSchema(@PathParam String year)
    {
        ParamValidators.validateYear(year);

        List<SchemaDto> list = new ArrayList<>();
        for (Schema schema : schemaService.list(year)) {
            SchemaDto dto = new SchemaDto(schema);
            list.add(dto);
        }
        return list;
    }
}
