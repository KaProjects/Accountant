package org.kaleta.rest;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.dto.YearTransactionDto;
import org.kaleta.service.TransactionService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/transaction")
public class TransactionResource
{
    @Inject
    TransactionService service;

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{year}/{debitPrefix}/{creditPrefix}")
    public Response getTransactionsMatching(@PathParam String year, @PathParam String debitPrefix, @PathParam String creditPrefix)
    {
        return Endpoint.process(() -> {
            ParamValidators.validateYear(year);
            ParamValidators.validateDebitPrefix(debitPrefix);
            ParamValidators.validateCreditPrefix(creditPrefix);
        }, () -> YearTransactionDto.from(service.getTransactionsMatching(year, debitPrefix, creditPrefix)));
    }

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{year}")
    public Response getTransactions(@PathParam String year)
    {
        return Endpoint.process(() -> {
            ParamValidators.validateYear(year);
        }, () -> YearTransactionDto.from(service.getTransactionsMatching(year, "", "")));
    }
}
