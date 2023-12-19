package org.kaleta.rest;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.Utils;
import org.kaleta.dto.AccountingDto;
import org.kaleta.dto.YearTransactionDto;
import org.kaleta.entity.Transaction;
import org.kaleta.model.AccountingData;
import org.kaleta.model.GroupComponent;
import org.kaleta.service.AccountingService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/accounting")
public class AccountingResource
{
    @Inject
    AccountingService service;

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{year}/profit")
    public Response getProfit(@PathParam String year)
    {
        return Endpoint.process(() -> {
            ParamValidators.validateYear(year);
        }, () -> {
            AccountingDto profitDto = new AccountingDto(year, AccountingDto.Type.PROFIT_SUMMARY);

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

            profitDto.getRows().add(from(group60, AccountingDto.Type.INCOME_GROUP));
            profitDto.getRows().add(from(group55a, AccountingDto.Type.EXPENSE_GROUP));
            profitDto.getRows().add(from(group63a, AccountingDto.Type.INCOME_GROUP));

            AccountingDto.Row netIncomeRow = new AccountingDto.Row(AccountingDto.Type.PROFIT_SUMMARY, "Net Income", "ni");
            netIncomeRow.setMonthlyValues(Utils.addIntegerArrays(Utils.subtractIntegerArrays(group60.getMonthlyBalance(), group55a.getMonthlyBalance()), group63a.getMonthlyBalance()));
            netIncomeRow.setTotal(group60.getBalance() - group55a.getBalance() + group63a.getBalance());
            profitDto.getRows().add(netIncomeRow);

            profitDto.getRows().add(from(group51, AccountingDto.Type.EXPENSE_GROUP));
            profitDto.getRows().add(from(group52, AccountingDto.Type.EXPENSE_GROUP));
            profitDto.getRows().add(from(group53, AccountingDto.Type.EXPENSE_GROUP));

            AccountingDto.Row operatingProfitRow = new AccountingDto.Row(AccountingDto.Type.PROFIT_SUMMARY, "Operating Profit", "op");
            operatingProfitRow.setMonthlyValues(Utils.subtractIntegerArrays(Utils.subtractIntegerArrays(Utils.subtractIntegerArrays(netIncomeRow.getMonthlyValues(), group51.getMonthlyBalance()), group52.getMonthlyBalance()), group53.getMonthlyBalance()));
            operatingProfitRow.setTotal(netIncomeRow.getTotal() - group51.getBalance() - group52.getBalance() - group53.getBalance());
            profitDto.getRows().add(operatingProfitRow);

            profitDto.getRows().add(from(group50, AccountingDto.Type.EXPENSE_GROUP));
            profitDto.getRows().add(from(group61, AccountingDto.Type.INCOME_GROUP));
            profitDto.getRows().add(from(group55, AccountingDto.Type.EXPENSE_GROUP));
            profitDto.getRows().add(from(group62, AccountingDto.Type.INCOME_GROUP));
            profitDto.getRows().add(from(group54, AccountingDto.Type.EXPENSE_GROUP));
            profitDto.getRows().add(from(group63b, AccountingDto.Type.INCOME_GROUP));
            profitDto.getRows().add(from(group55b, AccountingDto.Type.EXPENSE_GROUP));

            AccountingDto.Row netProfitRow = new AccountingDto.Row(AccountingDto.Type.PROFIT_SUMMARY, "Net Profit", "np");
            Integer[] np1 = Utils.subtractIntegerArrays(operatingProfitRow.getMonthlyValues(), group50.getMonthlyBalance());
            Integer[] np2 = Utils.subtractIntegerArrays(group61.getMonthlyBalance(), group55.getMonthlyBalance());
            Integer[] np3 = Utils.subtractIntegerArrays(group62.getMonthlyBalance(), group54.getMonthlyBalance());
            Integer[] np4 = Utils.subtractIntegerArrays(group63b.getMonthlyBalance(), group55b.getMonthlyBalance());
            netProfitRow.setMonthlyValues(Utils.mergeIntegerArrays(np1, np2, np3, np4));
            netProfitRow.setTotal(operatingProfitRow.getTotal() - group50.getBalance() + group61.getBalance() - group55.getBalance()
                    + group62.getBalance() - group54.getBalance() + group63b.getBalance() - group55b.getBalance());
            profitDto.getRows().add(netProfitRow);

            return profitDto;
        });
    }

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/inefficient/{year}/cashflow")
    public Response getCashFlowInefficient(@PathParam String year)
    {
        return Endpoint.process(() -> {
            ParamValidators.validateYear(year);
        }, () -> {
            GroupComponent group20 = service.getGroupComponent(year, "20");
            GroupComponent group21 = service.getGroupComponent(year, "21");
            GroupComponent group23 = service.getGroupComponent(year, "23");
            GroupComponent group22 = service.getGroupComponent(year, "22").inverted();

            AccountingDto cashFlowDto = new AccountingDto(year, AccountingDto.Type.CASH_FLOW_SUMMARY);

            cashFlowDto.getRows().add(from(group20, AccountingDto.Type.CASH_FLOW_GROUP));
            cashFlowDto.getRows().add(from(group21, AccountingDto.Type.CASH_FLOW_GROUP));
            cashFlowDto.getRows().add(from(group23, AccountingDto.Type.CASH_FLOW_GROUP));
            cashFlowDto.getRows().add(from(group22, AccountingDto.Type.CASH_FLOW_GROUP));

            AccountingDto.Row cashFlowRow = new AccountingDto.Row(AccountingDto.Type.CASH_FLOW_SUMMARY, "Cash Flow", "cf");
            cashFlowRow.setInitial(group20.getInitialValue() + group21.getInitialValue() + group23.getInitialValue() + group22.getInitialValue());
            cashFlowRow.setMonthlyValues(Utils.mergeIntegerArrays(group20.getMonthlyBalance(), group21.getMonthlyBalance(), group23.getMonthlyBalance(), group22.getMonthlyBalance()));
            cashFlowRow.setTotal(group20.getBalance() + group21.getBalance() + group23.getBalance() + group22.getBalance());
            cashFlowDto.getRows().add(cashFlowRow);

            return cashFlowDto;
        });
    }

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{year}/cashflow")
    public Response getCashFlow(@PathParam String year)
    {
        return Endpoint.process(() -> {
            ParamValidators.validateYear(year);
        }, () -> {
            AccountingData cashFlowData = service.getCashFlowData(year);

            GroupComponent group20 = cashFlowData.getGroupComponent(year, "20");
            GroupComponent group21 = cashFlowData.getGroupComponent(year, "21");
            GroupComponent group23 = cashFlowData.getGroupComponent(year, "23");
            GroupComponent group22 = cashFlowData.getGroupComponent(year, "22").inverted();

            AccountingDto cashFlowDto = new AccountingDto(year, AccountingDto.Type.CASH_FLOW_SUMMARY);

            cashFlowDto.getRows().add(from(group20, AccountingDto.Type.CASH_FLOW_GROUP));
            cashFlowDto.getRows().add(from(group21, AccountingDto.Type.CASH_FLOW_GROUP));
            cashFlowDto.getRows().add(from(group23, AccountingDto.Type.CASH_FLOW_GROUP));
            cashFlowDto.getRows().add(from(group22, AccountingDto.Type.CASH_FLOW_GROUP));

            AccountingDto.Row cashFlowRow = new AccountingDto.Row(AccountingDto.Type.CASH_FLOW_SUMMARY, "Cash Flow", "cf");
            cashFlowRow.setInitial(group20.getInitialValue() + group21.getInitialValue() + group23.getInitialValue() + group22.getInitialValue());
            cashFlowRow.setMonthlyValues(Utils.mergeIntegerArrays(group20.getMonthlyBalance(), group21.getMonthlyBalance(), group23.getMonthlyBalance(), group22.getMonthlyBalance()));
            cashFlowRow.setTotal(group20.getBalance() + group21.getBalance() + group23.getBalance() + group22.getBalance());
            cashFlowDto.getRows().add(cashFlowRow);

            return cashFlowDto;
        });
    }

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{year}/transaction/{accountId}/month/{month}")
    public Response getTransactions(@PathParam String year, @PathParam String accountId, @PathParam String month)
    {
        return Endpoint.process(() -> {
            ParamValidators.validateYear(year);
            ParamValidators.validateSchemaAccountId(accountId);
            ParamValidators.validateMonth(month);
        }, () -> {
            List<Transaction> transactions = service.getSchemaTransactions(year, accountId, month);
            return YearTransactionDto.from(transactions).stream().sorted().collect(Collectors.toList());
        });
    }

    private AccountingDto.Row from(GroupComponent groupComponent, AccountingDto.Type type){
        AccountingDto.Row groupRow = new AccountingDto.Row(type, groupComponent.getName(), groupComponent.getSchemaId());
        if (type == AccountingDto.Type.CASH_FLOW_GROUP) groupRow.setInitial(groupComponent.getInitialValue());
        groupRow.setMonthlyValues(groupComponent.getMonthlyBalance());
        groupRow.setTotal(groupComponent.getBalance());
        for (GroupComponent.AccountComponent accountComponent : groupComponent.getAccounts()){
            AccountingDto.Type accountType = null;
            if (type == AccountingDto.Type.INCOME_GROUP) accountType = AccountingDto.Type.INCOME_ACCOUNT;
            if (type == AccountingDto.Type.EXPENSE_GROUP) accountType = AccountingDto.Type.EXPENSE_ACCOUNT;
            if (type == AccountingDto.Type.CASH_FLOW_GROUP) accountType = AccountingDto.Type.CASH_FLOW_ACCOUNT;
            AccountingDto.Row accountRow = new AccountingDto.Row(accountType, accountComponent.getName(), accountComponent.getSchemaId());
            if (type == AccountingDto.Type.CASH_FLOW_GROUP) accountRow.setInitial(accountComponent.getInitialValue());
            accountRow.setMonthlyValues(accountComponent.getMonthlyBalance());
            accountRow.setTotal(accountComponent.getBalance());
            groupRow.getAccounts().add(accountRow);
        }
        return groupRow;
    }
}
