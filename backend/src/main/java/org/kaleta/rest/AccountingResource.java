package org.kaleta.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.kaleta.dto.YearTransactionDto;
import org.kaleta.rest.validation.ValidMonth;
import org.kaleta.rest.validation.ValidSchemaAccountId;
import org.kaleta.rest.validation.ValidYear;
import org.kaleta.service.AccountingService;
import org.kaleta.service.StatementService;

import java.util.stream.Collectors;

@Path("/accounting")
public class AccountingResource
{
    @Inject
    AccountingService service;

    @Inject
    StatementService statementService;

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profit/{year}")
    public Response getProfit(@PathParam("year") @ValidYear String year)
    {
        return Response.ok(statementService.getProfit(year)).build();
    }

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profit")
    public Response getOverallProfit()
    {
        return Response.ok(statementService.getOverallProfit()).build();
    }

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/cashflow/{year}")
    public Response getCashFlow(@PathParam("year") @ValidYear String year)
    {
        return Response.ok(statementService.getCashFlow(year)).build();
    }

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/cashflow")
    public Response getOverallCashFlow()
    {
        return Response.ok(statementService.getOverallCashFlow()).build();
    }

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/balance/{year}")
    public Response getBalanceSheet(@PathParam("year") @ValidYear String year)
    {
        return Response.ok(statementService.getBalanceSheet(year)).build();
    }

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/balance")
    public Response getOverallBalanceSheet()
    {
        return Response.ok(statementService.getOverallBalanceSheet()).build();
    }

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{year}/transaction/{accountId}/month/{month}")
    public Response getTransactions(@PathParam("year") @ValidYear String year, @PathParam("accountId") @ValidSchemaAccountId String accountId, @PathParam("month") @ValidMonth String month)
    {
        return Response.ok(YearTransactionDto.from(service.getSchemaTransactions(year, accountId, month))
                .stream().sorted().collect(Collectors.toList())).build();
    }
}
