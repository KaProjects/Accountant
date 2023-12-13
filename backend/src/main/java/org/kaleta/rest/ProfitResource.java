package org.kaleta.rest;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.Utils;
import org.kaleta.dto.ProfitDto;
import org.kaleta.dto.YearTransactionDto;
import org.kaleta.model.GroupComponent;
import org.kaleta.service.AccountingService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/profit")
public class ProfitResource
{
    @Inject
    AccountingService service;

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    @Path("/{year}")
    public ProfitDto getProfit(@PathParam String year)
    {
        ParamValidators.validateYear(year);

        ProfitDto profitDto = new ProfitDto(year);

        GroupComponent group60 = service.getGroupComponent(year, "60");
        GroupComponent group55a = service.getGroupComponent(year, "55", "0", "1", "2");
        GroupComponent group63a = service.getGroupComponent(year, "63", "1", "2", "3");

        GroupComponent group51 = service.getGroupComponent(year, "51");
        GroupComponent group52 = service.getGroupComponent(year, "52");
        GroupComponent group53 = service.getGroupComponent(year, "53");

        GroupComponent group50 = service.getGroupComponent(year, "50");
        GroupComponent group61 = service.getGroupComponent(year, "61");
        GroupComponent group55 = service.getGroupComponent(year, "56");
        GroupComponent group62 = service.getGroupComponent(year, "62");
        GroupComponent group54 = service.getGroupComponent(year, "54");
        GroupComponent group63b = service.getGroupComponent(year, "63", "0");
        GroupComponent group55b = service.getGroupComponent(year, "55", "3", "4", "5");

        profitDto.getRows().add(from(group60, ProfitDto.Type.INCOME_GROUP));
        profitDto.getRows().add(from(group55a, ProfitDto.Type.EXPENSE_GROUP));
        profitDto.getRows().add(from(group63a, ProfitDto.Type.INCOME_GROUP));

        ProfitDto.Row netIncomeRow = new ProfitDto.Row(ProfitDto.Type.SUMMARY, "Net Income", "ni");
        netIncomeRow.setMonthlyValues(Utils.addIntegerArrays(Utils.subtractIntegerArrays(group60.getMonthlyBalance(), group55a.getMonthlyBalance()), group63a.getMonthlyBalance()));
        netIncomeRow.setTotal(group60.getBalance() - group55a.getBalance() + group63a.getBalance());
        profitDto.getRows().add(netIncomeRow);

        profitDto.getRows().add(from(group51, ProfitDto.Type.EXPENSE_GROUP));
        profitDto.getRows().add(from(group52, ProfitDto.Type.EXPENSE_GROUP));
        profitDto.getRows().add(from(group53, ProfitDto.Type.EXPENSE_GROUP));

        ProfitDto.Row operatingProfitRow = new ProfitDto.Row(ProfitDto.Type.SUMMARY, "Operating Profit", "op");
        operatingProfitRow.setMonthlyValues(Utils.subtractIntegerArrays(Utils.subtractIntegerArrays(Utils.subtractIntegerArrays(netIncomeRow.getMonthlyValues(), group51.getMonthlyBalance()), group52.getMonthlyBalance()), group53.getMonthlyBalance()));
        operatingProfitRow.setTotal(netIncomeRow.getTotal() - group51.getBalance() - group52.getBalance() - group53.getBalance());
        profitDto.getRows().add(operatingProfitRow);

        profitDto.getRows().add(from(group50, ProfitDto.Type.EXPENSE_GROUP));
        profitDto.getRows().add(from(group61, ProfitDto.Type.INCOME_GROUP));
        profitDto.getRows().add(from(group55, ProfitDto.Type.EXPENSE_GROUP));
        profitDto.getRows().add(from(group62, ProfitDto.Type.INCOME_GROUP));
        profitDto.getRows().add(from(group54, ProfitDto.Type.EXPENSE_GROUP));
        profitDto.getRows().add(from(group63b, ProfitDto.Type.INCOME_GROUP));
        profitDto.getRows().add(from(group55b, ProfitDto.Type.EXPENSE_GROUP));

        ProfitDto.Row netProfitRow = new ProfitDto.Row(ProfitDto.Type.SUMMARY, "Net Profit", "np");
        Integer[] np1 = Utils.subtractIntegerArrays(operatingProfitRow.getMonthlyValues(), group50.getMonthlyBalance());
        Integer[] np2 = Utils.subtractIntegerArrays(group61.getMonthlyBalance(), group55.getMonthlyBalance());
        Integer[] np3 = Utils.subtractIntegerArrays(group62.getMonthlyBalance(), group54.getMonthlyBalance());
        Integer[] np4 = Utils.subtractIntegerArrays(group63b.getMonthlyBalance(), group55b.getMonthlyBalance());
        netProfitRow.setMonthlyValues(Utils.mergeIntegerArrays(np1, np2, np3, np4));
        netProfitRow.setTotal(operatingProfitRow.getTotal() - group50.getBalance() + group61.getBalance() - group55.getBalance()
                + group62.getBalance() - group54.getBalance() + group63b.getBalance() - group55b.getBalance());
        profitDto.getRows().add(netProfitRow);

        return profitDto;
    }

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    @Path("/{year}/transaction/{accountId}/month/{month}")
    public List<YearTransactionDto> getTransactions(@PathParam String year, @PathParam String accountId, @PathParam String month)
    {
        ParamValidators.validateYear(year);
        ParamValidators.validateSchemaAccountId(accountId);
        ParamValidators.validateMonth(month);

        return service.getSchemaTransactions(year, accountId, month);
    }

    private ProfitDto.Row from(GroupComponent groupComponent, ProfitDto.Type type){
        ProfitDto.Row groupRow = new ProfitDto.Row(type, groupComponent.getName(), groupComponent.getSchemaId());
        groupRow.setMonthlyValues(groupComponent.getMonthlyBalance());
        groupRow.setTotal(groupComponent.getBalance());
        for (GroupComponent.AccountComponent accountComponent : groupComponent.getAccounts()){
            ProfitDto.Type accountType = type == ProfitDto.Type.INCOME_GROUP ? ProfitDto.Type.INCOME_ACCOUNT : ProfitDto.Type.EXPENSE_ACCOUNT;
            ProfitDto.Row accountRow = new ProfitDto.Row(accountType, accountComponent.getName(), accountComponent.getSchemaId());
            accountRow.setMonthlyValues(accountComponent.getMonthlyBalance());
            accountRow.setTotal(accountComponent.getBalance());
            groupRow.getAccounts().add(accountRow);
        }
        return groupRow;
    }
}
