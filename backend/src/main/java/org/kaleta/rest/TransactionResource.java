package org.kaleta.rest;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.dto.YearAccountTransactionDto;
import org.kaleta.dto.YearTransactionDto;
import org.kaleta.entity.Transaction;
import org.kaleta.service.AccountService;
import org.kaleta.service.TransactionService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/transaction")
public class TransactionResource
{
    @Inject
    TransactionService service;

    @Inject
    AccountService accountService;

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

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{year}/{accountId}")
    public Response getAccountTransactions(@PathParam String year, @PathParam String accountId)
    {
        return Endpoint.process(() -> {
            ParamValidators.validateYear(year);
            ParamValidators.validateAccountId(accountId);
        }, () -> {
            List<YearAccountTransactionDto> dtoList = new ArrayList<>();
            Map<String, String> accountNames = accountService.getAccountNamesMap(year);
            for (Transaction transaction : service.getTransactionsMatching(year, accountId))
            {
                YearAccountTransactionDto dto = new YearAccountTransactionDto();
                dto.setDate(transaction.getDate());
                if (transaction.getDebit().equals(accountId)){
                    dto.setDebit(String.valueOf(transaction.getAmount()));
                    dto.setPair(transaction.getCredit() + " " + accountNames.get(transaction.getCredit()));
                }
                if (transaction.getCredit().equals(accountId)){
                    dto.setCredit(String.valueOf(transaction.getAmount()));
                    dto.setPair(transaction.getDebit() + " " + accountNames.get(transaction.getDebit()));
                }
                dto.setDescription(transaction.getDescription());

                dtoList.add(dto);
            }
            return dtoList.stream().sorted().collect(Collectors.toList());
        });
    }
}
