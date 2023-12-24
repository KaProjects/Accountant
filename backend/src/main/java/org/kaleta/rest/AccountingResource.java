package org.kaleta.rest;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.Utils;
import org.kaleta.dto.AccountingDto;
import org.kaleta.dto.YearTransactionDto;
import org.kaleta.entity.Transaction;
import org.kaleta.model.AccountingData;
import org.kaleta.model.AccountingYearlyData;
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
    @Path("/profit/{year}")
    public Response getProfit(@PathParam String year)
    {
        return Endpoint.process(() -> {
            ParamValidators.validateYear(year);
        }, () -> {
            AccountingData profitExpensesData = service.getProfitExpensesData(year);
            AccountingData profitRevenuesData = service.getProfitRevenuesData(year);

            GroupComponent group60 = profitRevenuesData.getGroupComponent(year, "60");
            GroupComponent group55a = profitExpensesData.getGroupComponent(year, "55", "0", "1", "2");
            GroupComponent group63a = profitRevenuesData.getGroupComponent(year, "63", "1", "2", "3");

            GroupComponent group51 = profitExpensesData.getGroupComponent(year, "51");
            GroupComponent group52 = profitExpensesData.getGroupComponent(year, "52");
            GroupComponent group53 = profitExpensesData.getGroupComponent(year, "53");

            GroupComponent group50 = profitExpensesData.getGroupComponent(year, "50");
            GroupComponent group61 = profitRevenuesData.getGroupComponent(year, "61");
            GroupComponent group56 = profitExpensesData.getGroupComponent(year, "56");
            GroupComponent group62 = profitRevenuesData.getGroupComponent(year, "62");
            GroupComponent group54 = profitExpensesData.getGroupComponent(year, "54");
            GroupComponent group63b = profitRevenuesData.getGroupComponent(year, "63", "0");
            GroupComponent group55b = profitExpensesData.getGroupComponent(year, "55", "3", "4", "5");

            AccountingDto profitDto = new AccountingDto(year, AccountingDto.Type.PROFIT_SUMMARY);

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
            profitDto.getRows().add(from(group56, AccountingDto.Type.EXPENSE_GROUP));
            profitDto.getRows().add(from(group62, AccountingDto.Type.INCOME_GROUP));
            profitDto.getRows().add(from(group54, AccountingDto.Type.EXPENSE_GROUP));
            profitDto.getRows().add(from(group63b, AccountingDto.Type.INCOME_GROUP));
            profitDto.getRows().add(from(group55b, AccountingDto.Type.EXPENSE_GROUP));

            AccountingDto.Row netProfitRow = new AccountingDto.Row(AccountingDto.Type.PROFIT_SUMMARY, "Net Profit", "np");
            Integer[] np1 = Utils.subtractIntegerArrays(operatingProfitRow.getMonthlyValues(), group50.getMonthlyBalance());
            Integer[] np2 = Utils.subtractIntegerArrays(group61.getMonthlyBalance(), group56.getMonthlyBalance());
            Integer[] np3 = Utils.subtractIntegerArrays(group62.getMonthlyBalance(), group54.getMonthlyBalance());
            Integer[] np4 = Utils.subtractIntegerArrays(group63b.getMonthlyBalance(), group55b.getMonthlyBalance());
            netProfitRow.setMonthlyValues(Utils.mergeIntegerArrays(np1, np2, np3, np4));
            netProfitRow.setTotal(operatingProfitRow.getTotal() - group50.getBalance() + group61.getBalance() - group56.getBalance()
                    + group62.getBalance() - group54.getBalance() + group63b.getBalance() - group55b.getBalance());
            profitDto.getRows().add(netProfitRow);

            return profitDto;
        });
    }

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profit")
    public Response getOverallProfit()
    {
        return Endpoint.process(() -> {}, () ->
        {
            AccountingYearlyData yearlyData = service.getYearlyProfitData();

            String[] years = yearlyData.getYears();

            AccountingData profitExpensesData = service.getProfitExpensesData(years[years.length - 1]);
            AccountingData profitRevenuesData = service.getProfitRevenuesData(years[years.length - 1]);

            GroupComponent group60 = profitRevenuesData.getGroupComponent(years[years.length - 1], "60");
            GroupComponent group55a = profitExpensesData.getGroupComponent(years[years.length - 1], "55", "0", "1", "2");
            GroupComponent group63a = profitRevenuesData.getGroupComponent(years[years.length - 1], "63", "1", "2", "3");

            GroupComponent group51 = profitExpensesData.getGroupComponent(years[years.length - 1], "51");
            GroupComponent group52 = profitExpensesData.getGroupComponent(years[years.length - 1], "52");
            GroupComponent group53 = profitExpensesData.getGroupComponent(years[years.length - 1], "53");

            GroupComponent group50 = profitExpensesData.getGroupComponent(years[years.length - 1], "50");
            GroupComponent group61 = profitRevenuesData.getGroupComponent(years[years.length - 1], "61");
            GroupComponent group56 = profitExpensesData.getGroupComponent(years[years.length - 1], "56");
            GroupComponent group62 = profitRevenuesData.getGroupComponent(years[years.length - 1], "62");
            GroupComponent group54 = profitExpensesData.getGroupComponent(years[years.length - 1], "54");
            GroupComponent group63b = profitRevenuesData.getGroupComponent(years[years.length - 1], "63", "0");
            GroupComponent group55b = profitExpensesData.getGroupComponent(years[years.length - 1], "55", "3", "4", "5");

            AccountingDto profitDto = new AccountingDto(yearlyData.getYears(), AccountingDto.Type.PROFIT_SUMMARY);

            AccountingDto.Row row60 = from(group60, AccountingDto.Type.INCOME_GROUP, yearlyData.getYearlyValuesFor("60"));
            profitDto.getRows().add(row60);
            AccountingDto.Row row55a = from(group55a, AccountingDto.Type.EXPENSE_GROUP, yearlyData.getYearlyValuesFor("55", "0", "1", "2"));
            profitDto.getRows().add(row55a);
            AccountingDto.Row row63a = from(group63a, AccountingDto.Type.INCOME_GROUP, yearlyData.getYearlyValuesFor("63", "1", "2", "3"));
            profitDto.getRows().add(row63a);

            AccountingDto.Row netIncomeRow = new AccountingDto.Row(AccountingDto.Type.PROFIT_SUMMARY, "Net Income", "ni");
            netIncomeRow.setYearlyValues(Utils.addIntegerArrays(Utils.subtractIntegerArrays(row60.getYearlyValues(), row55a.getYearlyValues()), row63a.getYearlyValues()));
            netIncomeRow.setTotal(Utils.sumArray(netIncomeRow.getYearlyValues()));
            profitDto.getRows().add(netIncomeRow);

            AccountingDto.Row row51 = from(group51, AccountingDto.Type.EXPENSE_GROUP, yearlyData.getYearlyValuesFor("51"));
            profitDto.getRows().add(row51);
            AccountingDto.Row row52 = from(group52, AccountingDto.Type.EXPENSE_GROUP, yearlyData.getYearlyValuesFor("52"));
            profitDto.getRows().add(row52);
            AccountingDto.Row row53 = from(group53, AccountingDto.Type.EXPENSE_GROUP, yearlyData.getYearlyValuesFor("53"));
            profitDto.getRows().add(row53);

            AccountingDto.Row operatingProfitRow = new AccountingDto.Row(AccountingDto.Type.PROFIT_SUMMARY, "Operating Profit", "op");
            operatingProfitRow.setYearlyValues(Utils.subtractIntegerArrays(Utils.subtractIntegerArrays(Utils.subtractIntegerArrays(netIncomeRow.getYearlyValues(), row51.getYearlyValues()), row52.getYearlyValues()), row53.getYearlyValues()));
            operatingProfitRow.setTotal(Utils.sumArray(operatingProfitRow.getYearlyValues()));
            profitDto.getRows().add(operatingProfitRow);

            AccountingDto.Row row50 = from(group50, AccountingDto.Type.EXPENSE_GROUP, yearlyData.getYearlyValuesFor("50"));
            profitDto.getRows().add(row50);
            AccountingDto.Row row61 = from(group61, AccountingDto.Type.INCOME_GROUP, yearlyData.getYearlyValuesFor("61"));
            profitDto.getRows().add(row61);
            AccountingDto.Row row56 = from(group56, AccountingDto.Type.EXPENSE_GROUP, yearlyData.getYearlyValuesFor("56"));
            profitDto.getRows().add(row56);
            AccountingDto.Row row62 = from(group62, AccountingDto.Type.INCOME_GROUP, yearlyData.getYearlyValuesFor("62"));
            profitDto.getRows().add(row62);
            AccountingDto.Row row54 = from(group54, AccountingDto.Type.EXPENSE_GROUP, yearlyData.getYearlyValuesFor("54"));
            profitDto.getRows().add(row54);
            AccountingDto.Row row63b = from(group63b, AccountingDto.Type.INCOME_GROUP, yearlyData.getYearlyValuesFor("63", "0"));
            profitDto.getRows().add(row63b);
            AccountingDto.Row row55b = from(group55b, AccountingDto.Type.EXPENSE_GROUP, yearlyData.getYearlyValuesFor("55", "3", "4", "5"));
            profitDto.getRows().add(row55b);

            AccountingDto.Row netProfitRow = new AccountingDto.Row(AccountingDto.Type.PROFIT_SUMMARY, "Net Profit", "np");
            Integer[] np1 = Utils.subtractIntegerArrays(operatingProfitRow.getYearlyValues(), row50.getYearlyValues());
            Integer[] np2 = Utils.subtractIntegerArrays(row61.getYearlyValues(), row56.getYearlyValues());
            Integer[] np3 = Utils.subtractIntegerArrays(row62.getYearlyValues(), row54.getYearlyValues());
            Integer[] np4 = Utils.subtractIntegerArrays(row63b.getYearlyValues(), row55b.getYearlyValues());
            netProfitRow.setYearlyValues(Utils.mergeIntegerArrays(np1, np2, np3, np4));
            netProfitRow.setTotal(Utils.sumArray(netProfitRow.getYearlyValues()));
            profitDto.getRows().add(netProfitRow);

            return profitDto;
        });
    }

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/cashflow/{year}")
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
    @Path("/cashflow")
    public Response getOverallCashFlow()
    {
        return Endpoint.process(() -> {}, () -> {
            AccountingYearlyData yearlyData = service.getYearlyClosingData();

            String[] years = yearlyData.getYears();

            AccountingData cashFlowData = service.getCashFlowData(years[years.length - 1]);
            GroupComponent group20 = cashFlowData.getGroupComponent(years[years.length - 1], "20");
            GroupComponent group21 = cashFlowData.getGroupComponent(years[years.length - 1], "21");
            GroupComponent group23 = cashFlowData.getGroupComponent(years[years.length - 1], "23");
            GroupComponent group22 = cashFlowData.getGroupComponent(years[years.length - 1], "22").inverted();

            AccountingDto cashFlowDto = new AccountingDto(yearlyData.getYears(), AccountingDto.Type.CASH_FLOW_SUMMARY);

            AccountingDto.Row row20 = from(group20, AccountingDto.Type.CASH_FLOW_GROUP, yearlyData.getYearlyValuesFor("20"));
            cashFlowDto.getRows().add(row20);

            AccountingDto.Row row21 = from(group21, AccountingDto.Type.CASH_FLOW_GROUP, yearlyData.getYearlyValuesFor("21"));
            cashFlowDto.getRows().add(row21);

            AccountingDto.Row row23 = from(group23, AccountingDto.Type.CASH_FLOW_GROUP, yearlyData.getYearlyValuesFor("23"));
            cashFlowDto.getRows().add(row23);

            Integer[] group22Yearly = yearlyData.getYearlyValuesFor("22");
            for (int i=0;i<group22Yearly.length;i++) group22Yearly[i] = -group22Yearly[i];
            AccountingDto.Row row22 = from(group22, AccountingDto.Type.CASH_FLOW_GROUP, group22Yearly);
            cashFlowDto.getRows().add(row22);

            AccountingDto.Row cashFlowRow = new AccountingDto.Row(AccountingDto.Type.CASH_FLOW_SUMMARY, "Cash Flow", "cf");
            cashFlowRow.setYearlyValues(Utils.mergeIntegerArrays(row20.getYearlyValues(), row21.getYearlyValues(), row23.getYearlyValues(), row22.getYearlyValues()));
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

    private AccountingDto.Row from(GroupComponent groupComponent, AccountingDto.Type type, Integer[] yearly)
    {
        AccountingDto.Row row = new AccountingDto.Row(type, groupComponent.getName(), groupComponent.getSchemaId());
        yearly[yearly.length - 1] = groupComponent.getBalance();
        row.setYearlyValues(yearly);
        row.setTotal(Utils.sumArray(yearly));
        return row;
    }

    private AccountingDto.Row from(GroupComponent groupComponent, AccountingDto.Type type)
    {
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
