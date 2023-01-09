package org.kaleta.rest;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.dto.VacationDto;
import org.kaleta.entity.Transaction;
import org.kaleta.service.AccountService;
import org.kaleta.service.TransactionService;
import org.kaleta.service.ViewService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Path("/view")
public class ViewResource {

    @Inject
    ViewService viewService;

    @Inject
    TransactionService transactionService;

    @Inject
    AccountService accountService;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    @Path("/{year}/vacation")
    public VacationDto getBudget(@PathParam String year){
        if (!year.matches("20\\d\\d"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Year Parameter");

        Map<String, List<Transaction>> vacations = viewService.getVacationMap(year);
        Map<String, String> accountNames = accountService.getAccountNamesMap(year);

        VacationDto dto = new VacationDto();

        for (String key : vacations.keySet()){
            VacationDto.Vacation vacation = new VacationDto.Vacation();
            vacation.setName(key.replace("_", " "));
            vacation.setExpenses(String.valueOf(transactionService.sumExpensesOf(vacations.get(key))));

            for (Transaction transaction : vacations.get(key)){
                VacationDto.Vacation.Transaction vacTr = new VacationDto.Vacation.Transaction();
                vacTr.setDate(transaction.getDate());
                vacTr.setDescription(transaction.getDescription());
                vacTr.setDescription(vacTr.getDescription().replace("vac="+key, ""));
                vacTr.setDebit(accountNames.get(transaction.getDebit()));
                vacTr.setCredit(accountNames.get(transaction.getCredit()));

                String amountPrefix = transaction.getDebit().startsWith("5")
                        ? transaction.getCredit().startsWith("5") ? "~" : ""
                        : "-";

                vacTr.setAmount(amountPrefix + transaction.getAmount());

                vacation.getTransactions().add(vacTr);
            }
            dto.getVacations().add(vacation);
        }

        dto.getVacations().sort(VacationDto::compare);
        return dto;
    }
}
