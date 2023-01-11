package org.kaleta.rest;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
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
public class AccountResource {

    @Inject
    AccountService accountService;

    @Inject
    SchemaService schemaService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    @Path("/{year}")
    public List<AccountDto> getAllAccounts(@PathParam String year) {
        InputValidators.validateYear(year);

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
