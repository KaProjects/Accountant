package org.kaleta.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.kaleta.rest.validation.ValidAccountId;
import org.kaleta.rest.validation.ValidYear;
import org.kaleta.service.AccountService;

@Path("/transaction")
public class TransactionResource
{
    @Inject
    AccountService accountService;

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{year}/{accountId}")
    public Response getAccountTransactions(@PathParam("year") @ValidYear String year, @PathParam("accountId") @ValidAccountId String accountId)
    {
        return Response.ok(accountService.getAccountTransactions(year, accountId)).build();
    }
}
