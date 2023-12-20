package org.kaleta.rest;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.dto.YearAccountDto;
import org.kaleta.model.SchemaClass;
import org.kaleta.service.AccountService;
import org.kaleta.service.SchemaService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/account")
public class AccountResource
{
    @Inject
    AccountService accountService;

    @Inject
    SchemaService schemaService;

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{year}")
    public Response getAllAccounts(@PathParam String year)
    {
        return Endpoint.process(() -> {
            ParamValidators.validateYear(year);
        }, () -> {
            List<YearAccountDto> accounts = YearAccountDto.from(accountService.list(year));
            Map<String, SchemaClass> schema = schemaService.getSchema(year);
            accounts.forEach(account -> {
                String schemaId = account.getSchemaId();
                SchemaClass clazz = schema.get(schemaId.substring(0,1));
                account.setSchemaClassName(clazz.getName());
                SchemaClass.Group group = clazz.getGroup(schemaId.substring(0,2));
                account.setSchemaGroupName(group.getName());
                account.setSchemaAccountName(group.getAccount(schemaId).getName());
            });
            return accounts;
        });
    }
}
