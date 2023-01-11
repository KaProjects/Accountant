package org.kaleta.rest;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.dto.YearTransactionDto;
import org.kaleta.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/transaction")
public class TransactionResource {

    @Inject
    TransactionService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    @Path("/{year}/{debitPrefix}/{creditPrefix}")
    public List<YearTransactionDto> getTransactionsMatching(@PathParam String year, @PathParam String debitPrefix, @PathParam String creditPrefix) {
        InputValidators.validateYear(year);

        if (!debitPrefix.matches("\\d?\\d?\\d"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Debit Prefix Parameter");
        if (!creditPrefix.matches("\\d?\\d?\\d"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Credit Prefix Parameter");

        return service.getTransactionsMatching(year, debitPrefix, creditPrefix);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    @Path("/{year}")
    public List<YearTransactionDto> getTransactionsMatching(@PathParam String year) {
        InputValidators.validateYear(year);

        return service.getTransactionsMatching(year, "", "");
    }
}
