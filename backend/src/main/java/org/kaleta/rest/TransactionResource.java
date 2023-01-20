package org.kaleta.rest;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.dto.YearTransactionDto;
import org.kaleta.service.TransactionService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/transaction")
public class TransactionResource
{
    @Inject
    TransactionService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    @Path("/{year}/{debitPrefix}/{creditPrefix}")
    public List<YearTransactionDto> getTransactionsMatching(@PathParam String year, @PathParam String debitPrefix, @PathParam String creditPrefix)
    {
        ParamValidators.validateYear(year);
        ParamValidators.validateDebitPrefix(debitPrefix);
        ParamValidators.validateCreditPrefix(creditPrefix);

        return service.getTransactionsMatching(year, debitPrefix, creditPrefix);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    @Path("/{year}")
    public List<YearTransactionDto> getTransactionsMatching(@PathParam String year)
    {
        ParamValidators.validateYear(year);

        return service.getTransactionsMatching(year, "", "");
    }
}
