package org.kaleta.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.kaleta.rest.validation.ValidSchemaAccountId;
import org.kaleta.rest.validation.ValidYear;
import org.kaleta.service.AccountService;
import org.kaleta.service.SchemaService;
import org.kaleta.service.TransactionService;

@Path("/account")
public class AccountResource
{
    @Inject
    AccountService accountService;

    @Inject
    SchemaService schemaService;

    @Inject
    TransactionService transactionService;

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{year}")
    public Response getAllAccounts(@PathParam("year") @ValidYear String year)
    {
        return Response.ok(accountService.getYearAccounts(year)).build();
    }

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{year}/{schemaId}")
    public Response getAccountsOverview(@PathParam("year") @ValidYear String year, @PathParam("schemaId") @ValidSchemaAccountId String schemaId)
    {
        return Response.ok(accountService.getAccountsOverview(year, schemaId)).build();
    }
}
