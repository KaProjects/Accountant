package org.kaleta.rest;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.dto.ViewDto;
import org.kaleta.entity.Transaction;
import org.kaleta.service.AccountService;
import org.kaleta.service.SchemaService;
import org.kaleta.service.ViewService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

    @Path("/view")
public class ViewResource
{
    @Inject
    ViewService viewService;

    @Inject
    AccountService accountService;

    @Inject
    SchemaService schemaService;

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{year}/vacation")
    public Response getVacations(@PathParam String year)
    {
        return Endpoint.process(() -> {
            ParamValidators.validateYear(year);
        }, () -> {
            Map<String, List<Transaction>> vacations = viewService.getVacationMap(year);
            Map<String, String> accountNames = accountService.getAccountNamesMap(year);
            Map<String, String> schemaNames = schemaService.getSchemaNames(year);

            ViewDto dto = new ViewDto();

            for (String key : vacations.keySet()){
                ViewDto.View view = new ViewDto.View();
                view.setName(key);
                view.setExpenses(String.valueOf(sumExpensesOf(vacations.get(key))));

                for (Transaction transaction : vacations.get(key)){
                    ViewDto.View.Transaction vacTr = new ViewDto.View.Transaction();
                    vacTr.setDate(transaction.getDate());
                    vacTr.setDescription(transaction.getDescription());
                    vacTr.setDescription(vacTr.getDescription().replace("vac="+key, ""));
                    vacTr.setDebit(accountNames.get(transaction.getDebit()));
                    vacTr.setCredit(accountNames.get(transaction.getCredit()));
                    vacTr.setAmount(constructAmount(transaction));
                    view.getTransactions().add(vacTr);

                    setChartData(schemaNames, view, transaction);
                }
                view.getTransactions().sort(ViewDto::compare);
                dto.getViews().add(view);
            }
            dto.getViews().sort(ViewDto::compare);
            return dto;
        });
    }

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{year}")
    public Response getViews(@PathParam String year)
    {
        return Endpoint.process(() -> {
            ParamValidators.validateYear(year);
        }, () -> {
            ViewDto dto = new ViewDto();

            Map<String, List<Transaction>> views = viewService.getViewMap(year);
            Map<String, String> accountNames = accountService.getAccountNamesMap(year);
            Map<String, String> schemaNames = schemaService.getSchemaNames(year);

            for (String key : views.keySet())
            {
                ViewDto.View view = new ViewDto.View();
                view.setName(key);
                view.setExpenses(String.valueOf(sumExpensesOf(views.get(key))));

                for (Transaction transaction : views.get(key))
                {
                    ViewDto.View.Transaction trDto = new ViewDto.View.Transaction();
                    trDto.setDate(transaction.getDate());
                    trDto.setDescription(transaction.getDescription().replace("view="+key, ""));
                    trDto.setDebit(accountNames.get(transaction.getDebit()));
                    trDto.setCredit(accountNames.get(transaction.getCredit()));
                    trDto.setAmount(constructAmount(transaction));
                    view.getTransactions().add(trDto);

                    setChartData(schemaNames, view, transaction);
                }
                view.getTransactions().sort(ViewDto::compare);
                dto.getViews().add(view);
            }
            dto.getViews().sort(ViewDto::compare);
            return dto;
        });
    }

    private static void setChartData(Map<String, String> schemaNames, ViewDto.View view, Transaction transaction)
    {
        if (transaction.getDebit().startsWith("5")){
            if (!transaction.getCredit().startsWith("5"))
            { // 5 & !5
                String schemaName = schemaNames.get(transaction.getDebit().substring(0,3));
                Integer value = transaction.getAmount();
                view.addChartData(schemaName, value);
            } else
            { // 5 & 5
                Integer value = transaction.getAmount();
                String creditSchemaName = schemaNames.get(transaction.getCredit().substring(0,3));
                view.addChartData(creditSchemaName, -value);
                String debitSchemaName = schemaNames.get(transaction.getDebit().substring(0,3));
                view.addChartData(debitSchemaName, value);
            }
        } else { // !5 & ?
            String schemaName = schemaNames.get(transaction.getCredit().substring(0,3));
            Integer value = transaction.getAmount();
            view.addChartData(schemaName, -value);
        }
    }

    private String constructAmount(Transaction transaction)
    {
        String amountPrefix = transaction.getDebit().startsWith("5")
                ? transaction.getCredit().startsWith("5") ? "~" : ""
                : "-";
        return amountPrefix + transaction.getAmount();
    }

    private Integer sumExpensesOf(List<Transaction> transactions)
    {
        Integer sum = 0;
        for (Transaction transaction : transactions) {
            if (transaction.getDebit().startsWith("5"))
            {
                if (!transaction.getCredit().startsWith("5"))
                { // 5 & !5
                    sum += transaction.getAmount();
                }
                // 5 & 5 - no action - it's just change of expense
            } else { // !5 & ?
                sum -= transaction.getAmount();
            }
        }
        return sum;
    }
}
