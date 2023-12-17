package org.kaleta.rest;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.dto.AccountDto;
import org.kaleta.entity.Account;
import org.kaleta.service.AccountService;
import org.kaleta.service.SchemaService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

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
    public List<AccountDto> getAllAccounts(@PathParam String year)
    {
        ParamValidators.validateYear(year);

        List<AccountDto> list = new ArrayList<>();
        for (Account account : accountService.list(year)) {
            AccountDto dto = new AccountDto(account);
            dto.setSchemaAccountName(schemaService.getAccountName(year, dto.getSchemaId()));
            dto.setSchemaGroupName(schemaService.getGroupName(year, dto.getSchemaId().substring(0,2)));
            dto.setSchemaClassName(schemaService.getClassName(year, dto.getSchemaId().substring(0,1)));
            list.add(dto);
        }
        return list;
    }
}
