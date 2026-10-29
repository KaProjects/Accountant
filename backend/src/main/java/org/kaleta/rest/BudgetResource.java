package org.kaleta.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.kaleta.dto.BudgetSchemaDto;
import org.kaleta.dto.YearTransactionDto;
import org.kaleta.rest.validation.ValidBudgetId;
import org.kaleta.rest.validation.ValidMonth;
import org.kaleta.rest.validation.ValidYear;
import org.kaleta.service.BudgetingService;

import java.util.List;
import java.util.stream.Collectors;

@Path("/budget")
public class BudgetResource
{
    @Inject
    BudgetingService service;

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{year}")
    public Response getBudget(@PathParam("year") @ValidYear String year)
    {
        return Response.ok(service.getBudget(year)).build();
    }

    /** The rows of the year's budget as they are stored, for the budget editor. */
    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{year}/schema")
    public Response getSchema(@PathParam("year") @ValidYear String year)
    {
        return Response.ok(service.getStoredSchema(year)).build();
    }

    /** Replaces every stored row of the year's budget with the ones given. */
    @PUT
    @SecurityRequirement(name = "AccountantSecurity")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{year}/schema")
    public Response saveSchema(@PathParam("year") @ValidYear String year, @NotNull List<@Valid @NotNull BudgetSchemaDto> rows)
    {
        service.saveSchema(year, rows);
        return Response.noContent().build();
    }

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{year}/transaction/{budgetId}/month/{month}")
    public Response getTransactions(@PathParam("year") @ValidYear String year, @PathParam("budgetId") @ValidBudgetId String budgetId, @PathParam("month") @ValidMonth String month)
    {
        return Response.ok(YearTransactionDto.from(service.getBudgetTransactions(year, budgetId, month)).stream().sorted().collect(Collectors.toList())).build();
    }
}
