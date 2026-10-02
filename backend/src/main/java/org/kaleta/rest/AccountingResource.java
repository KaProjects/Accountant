package org.kaleta.rest;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.Utils;
import org.kaleta.dto.AccountingDto;
import org.kaleta.dto.YearTransactionDto;
import org.kaleta.entity.Transaction;
import org.kaleta.model.AccountingData;
import org.kaleta.model.AccountingYearlyData;
import org.kaleta.model.ClassComponent;
import org.kaleta.model.GroupComponent;
import org.kaleta.service.AccountingService;
import org.kaleta.service.TransactionService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.kaleta.Constants.Label.ASSETS;
import static org.kaleta.Constants.Label.CASH_FLOW;
import static org.kaleta.Constants.Label.LIABILITIES;
import static org.kaleta.Constants.Label.NET_INCOME;
import static org.kaleta.Constants.Label.NET_PROFIT;
import static org.kaleta.Constants.Label.OPERATING_PROFIT;
import static org.kaleta.Constants.Label.PROFIT;

@Path("/accounting")
public class AccountingResource
{
    @Inject
    AccountingService service;

    @Inject
    TransactionService transactionService;

    @GET
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

            GroupComponent group60 = profitRevenuesData.getGroupComponent("60");
            GroupComponent group55a = profitExpensesData.getGroupComponent("55", "0", "1", "2");
            GroupComponent group63a = profitRevenuesData.getGroupComponent("63", "1", "2", "3", "4");

            GroupComponent group51 = profitExpensesData.getGroupComponent("51");
            GroupComponent group52 = profitExpensesData.getGroupComponent("52");
            GroupComponent group53 = profitExpensesData.getGroupComponent("53");

            GroupComponent group50 = profitExpensesData.getGroupComponent("50");
            GroupComponent group61 = profitRevenuesData.getGroupComponent("61");
            GroupComponent group56 = profitExpensesData.getGroupComponent("56");
            GroupComponent group62 = profitRevenuesData.getGroupComponent("62");
            GroupComponent group54 = profitExpensesData.getGroupComponent("54");
            GroupComponent group63b = profitRevenuesData.getGroupComponent("63", "0", "5");
            GroupComponent group55b = profitExpensesData.getGroupComponent("55", "3", "4", "5", "6");

            AccountingDto profitDto = new AccountingDto(AccountingDto.Type.PROFIT_SUMMARY);

            profitDto.getRows().add(from(group60, AccountingDto.Type.INCOME_GROUP));
            profitDto.getRows().add(from(group55a, AccountingDto.Type.EXPENSE_GROUP));
            profitDto.getRows().add(from(group63a, AccountingDto.Type.INCOME_GROUP));

            AccountingDto.Row netIncomeRow = new AccountingDto.Row(AccountingDto.Type.PROFIT_SUMMARY, NET_INCOME, "ni");
            netIncomeRow.setMonthlyValues(Utils.addIntegerArrays(Utils.subtractIntegerArrays(group60.getMonthlyBalance(), group55a.getMonthlyBalance()), group63a.getMonthlyBalance()));
            netIncomeRow.setTotal(group60.getBalance() - group55a.getBalance() + group63a.getBalance());
            profitDto.getRows().add(netIncomeRow);

            profitDto.getRows().add(from(group51, AccountingDto.Type.EXPENSE_GROUP));
            profitDto.getRows().add(from(group52, AccountingDto.Type.EXPENSE_GROUP));
            profitDto.getRows().add(from(group53, AccountingDto.Type.EXPENSE_GROUP));

            AccountingDto.Row operatingProfitRow = new AccountingDto.Row(AccountingDto.Type.PROFIT_SUMMARY, OPERATING_PROFIT, "op");
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

            AccountingDto.Row netProfitRow = new AccountingDto.Row(AccountingDto.Type.PROFIT_SUMMARY, NET_PROFIT, "np");
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
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profit")
    public Response getOverallProfit()
    {
        return Endpoint.process(() -> {}, () ->
        {
            AccountingYearlyData yearlyData = service.getYearlyProfitData();

            String[] years = yearlyData.getYears();

            // No years means nothing has been synced yet. The statement is then simply empty,
            // rather than an attempt to read the last of no years at all.
            if (years.length == 0) return new AccountingDto(years, AccountingDto.Type.PROFIT_SUMMARY);

            AccountingData profitExpensesData = service.getProfitExpensesData(years[years.length - 1]);
            AccountingData profitRevenuesData = service.getProfitRevenuesData(years[years.length - 1]);

            GroupComponent group60 = profitRevenuesData.getGroupComponent("60");
            GroupComponent group55a = profitExpensesData.getGroupComponent("55", "0", "1", "2");
            GroupComponent group63a = profitRevenuesData.getGroupComponent("63", "1", "2", "3", "4");

            GroupComponent group51 = profitExpensesData.getGroupComponent("51");
            GroupComponent group52 = profitExpensesData.getGroupComponent("52");
            GroupComponent group53 = profitExpensesData.getGroupComponent("53");

            GroupComponent group50 = profitExpensesData.getGroupComponent("50");
            GroupComponent group61 = profitRevenuesData.getGroupComponent("61");
            GroupComponent group56 = profitExpensesData.getGroupComponent("56");
            GroupComponent group62 = profitRevenuesData.getGroupComponent("62");
            GroupComponent group54 = profitExpensesData.getGroupComponent("54");
            GroupComponent group63b = profitRevenuesData.getGroupComponent("63", "0", "5");
            GroupComponent group55b = profitExpensesData.getGroupComponent("55", "3", "4", "5", "6");

            AccountingDto profitDto = new AccountingDto(years, AccountingDto.Type.PROFIT_SUMMARY);

            AccountingDto.Row row60 = from(group60, AccountingDto.Type.INCOME_GROUP, yearlyData, yearlyData.getYearlyGroupValues("60"));
            profitDto.getRows().add(row60);
            AccountingDto.Row row55a = from(group55a, AccountingDto.Type.EXPENSE_GROUP, yearlyData, yearlyData.getYearlyGroupValues("55", "0", "1", "2"));
            profitDto.getRows().add(row55a);
            AccountingDto.Row row63a = from(group63a, AccountingDto.Type.INCOME_GROUP, yearlyData, yearlyData.getYearlyGroupValues("63", "1", "2", "3", "4"));
            profitDto.getRows().add(row63a);

            AccountingDto.Row netIncomeRow = new AccountingDto.Row(AccountingDto.Type.PROFIT_SUMMARY, NET_INCOME, "ni");
            netIncomeRow.setYearlyValues(Utils.addIntegerArrays(Utils.subtractIntegerArrays(row60.getYearlyValues(), row55a.getYearlyValues()), row63a.getYearlyValues()));
            netIncomeRow.setTotal(Utils.sumArray(netIncomeRow.getYearlyValues()));
            profitDto.getRows().add(netIncomeRow);

            AccountingDto.Row row51 = from(group51, AccountingDto.Type.EXPENSE_GROUP, yearlyData, yearlyData.getYearlyGroupValues("51"));
            profitDto.getRows().add(row51);
            AccountingDto.Row row52 = from(group52, AccountingDto.Type.EXPENSE_GROUP, yearlyData, yearlyData.getYearlyGroupValues("52"));
            profitDto.getRows().add(row52);
            AccountingDto.Row row53 = from(group53, AccountingDto.Type.EXPENSE_GROUP, yearlyData, yearlyData.getYearlyGroupValues("53"));
            profitDto.getRows().add(row53);

            AccountingDto.Row operatingProfitRow = new AccountingDto.Row(AccountingDto.Type.PROFIT_SUMMARY, OPERATING_PROFIT, "op");
            operatingProfitRow.setYearlyValues(Utils.subtractIntegerArrays(Utils.subtractIntegerArrays(Utils.subtractIntegerArrays(netIncomeRow.getYearlyValues(), row51.getYearlyValues()), row52.getYearlyValues()), row53.getYearlyValues()));
            operatingProfitRow.setTotal(Utils.sumArray(operatingProfitRow.getYearlyValues()));
            profitDto.getRows().add(operatingProfitRow);

            AccountingDto.Row row50 = from(group50, AccountingDto.Type.EXPENSE_GROUP, yearlyData, yearlyData.getYearlyGroupValues("50"));
            profitDto.getRows().add(row50);
            AccountingDto.Row row61 = from(group61, AccountingDto.Type.INCOME_GROUP, yearlyData, yearlyData.getYearlyGroupValues("61"));
            profitDto.getRows().add(row61);
            AccountingDto.Row row56 = from(group56, AccountingDto.Type.EXPENSE_GROUP, yearlyData, yearlyData.getYearlyGroupValues("56"));
            profitDto.getRows().add(row56);
            AccountingDto.Row row62 = from(group62, AccountingDto.Type.INCOME_GROUP, yearlyData, yearlyData.getYearlyGroupValues("62"));
            profitDto.getRows().add(row62);
            AccountingDto.Row row54 = from(group54, AccountingDto.Type.EXPENSE_GROUP, yearlyData, yearlyData.getYearlyGroupValues("54"));
            profitDto.getRows().add(row54);
            AccountingDto.Row row63b = from(group63b, AccountingDto.Type.INCOME_GROUP, yearlyData, yearlyData.getYearlyGroupValues("63", "0", "5"));
            profitDto.getRows().add(row63b);
            AccountingDto.Row row55b = from(group55b, AccountingDto.Type.EXPENSE_GROUP, yearlyData, yearlyData.getYearlyGroupValues("55", "3", "4", "5", "6"));
            profitDto.getRows().add(row55b);

            AccountingDto.Row netProfitRow = new AccountingDto.Row(AccountingDto.Type.PROFIT_SUMMARY, NET_PROFIT, "np");
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
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/cashflow/{year}")
    public Response getCashFlow(@PathParam String year)
    {
        return Endpoint.process(() -> {
            ParamValidators.validateYear(year);
        }, () -> {
            AccountingData cashFlowData = service.getCashFlowData(year);

            GroupComponent group20 = cashFlowData.getGroupComponent("20");
            GroupComponent group21 = cashFlowData.getGroupComponent("21");
            GroupComponent group23 = cashFlowData.getGroupComponent("23");
            GroupComponent group22 = cashFlowData.getGroupComponent("22").inverted();

            AccountingDto cashFlowDto = new AccountingDto(AccountingDto.Type.CASH_FLOW_SUMMARY);

            cashFlowDto.getRows().add(from(group20, AccountingDto.Type.CASH_FLOW_GROUP));
            cashFlowDto.getRows().add(from(group21, AccountingDto.Type.CASH_FLOW_GROUP));
            cashFlowDto.getRows().add(from(group23, AccountingDto.Type.CASH_FLOW_GROUP));
            cashFlowDto.getRows().add(from(group22, AccountingDto.Type.CASH_FLOW_GROUP));

            AccountingDto.Row cashFlowRow = new AccountingDto.Row(AccountingDto.Type.CASH_FLOW_SUMMARY, CASH_FLOW, "cf");
            cashFlowRow.setInitial(group20.getInitialValue() + group21.getInitialValue() + group23.getInitialValue() + group22.getInitialValue());
            cashFlowRow.setMonthlyValues(Utils.mergeIntegerArrays(group20.getMonthlyBalance(), group21.getMonthlyBalance(), group23.getMonthlyBalance(), group22.getMonthlyBalance()));
            cashFlowRow.setTotal(group20.getBalance() + group21.getBalance() + group23.getBalance() + group22.getBalance());
            cashFlowDto.getRows().add(cashFlowRow);

            return cashFlowDto;
        });
    }

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/cashflow")
    public Response getOverallCashFlow()
    {
        return Endpoint.process(() -> {}, () -> {
            AccountingYearlyData yearlyData = service.getYearlyClosingData();

            String[] years = yearlyData.getYears();

            // No years means nothing has been synced yet. The statement is then simply empty,
            // rather than an attempt to read the last of no years at all.
            if (years.length == 0) return new AccountingDto(years, AccountingDto.Type.CASH_FLOW_SUMMARY);

            AccountingData cashFlowData = service.getCashFlowData(years[years.length - 1]);
            GroupComponent group20 = cashFlowData.getGroupComponent("20");
            GroupComponent group21 = cashFlowData.getGroupComponent("21");
            GroupComponent group23 = cashFlowData.getGroupComponent("23");
            GroupComponent group22 = cashFlowData.getGroupComponent("22").inverted();

            AccountingDto cashFlowDto = new AccountingDto(years, AccountingDto.Type.CASH_FLOW_SUMMARY);

            AccountingDto.Row row20 = from(group20, AccountingDto.Type.CASH_FLOW_GROUP, yearlyData, yearlyData.getYearlyGroupValues("20"));
            cashFlowDto.getRows().add(row20);

            AccountingDto.Row row21 = from(group21, AccountingDto.Type.CASH_FLOW_GROUP, yearlyData, yearlyData.getYearlyGroupValues("21"));
            cashFlowDto.getRows().add(row21);

            AccountingDto.Row row23 = from(group23, AccountingDto.Type.CASH_FLOW_GROUP, yearlyData, yearlyData.getYearlyGroupValues("23"));
            cashFlowDto.getRows().add(row23);

            Integer[] group22Yearly = Utils.invertValues(yearlyData.getYearlyGroupValues("22"));
            AccountingDto.Row row22 = from(group22, AccountingDto.Type.CASH_FLOW_GROUP, yearlyData, group22Yearly, true);
            cashFlowDto.getRows().add(row22);

            AccountingDto.Row cashFlowRow = new AccountingDto.Row(AccountingDto.Type.CASH_FLOW_SUMMARY, CASH_FLOW, "cf");
            cashFlowRow.setYearlyValues(Utils.mergeIntegerArrays(row20.getYearlyValues(), row21.getYearlyValues(), row23.getYearlyValues(), row22.getYearlyValues()));
            cashFlowDto.getRows().add(cashFlowRow);

           return cashFlowDto;
        });
    }

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/balance/{year}")
    public Response getBalanceSheet(@PathParam String year)
    {
        return Endpoint.process(() -> {
            ParamValidators.validateYear(year);
        }, () -> {
            Map<String, AccountingData> balanceData = service.getBalanceData(year);

            ClassComponent class0 = balanceData.get("0").getClassComponent();
            ClassComponent class1 = balanceData.get("1").getClassComponent();
            ClassComponent class2a = balanceData.get("2").getClassComponent("0", "1", "3");
            ClassComponent class3a = balanceData.get("3").getClassComponent("0");

            ClassComponent class2l = balanceData.get("2").getClassComponent("2");
            ClassComponent class3l = balanceData.get("3").getClassComponent("1");
            ClassComponent class4 = balanceData.get("4").getClassComponent();

            AccountingDto balanceSheetDto = new AccountingDto(AccountingDto.Type.BALANCE_SUMMARY);

            AccountingDto.Row rowClass0 = from(class0);
            AccountingDto.Row rowClass1 = from(class1);
            AccountingDto.Row rowClass2a = from(class2a);
            AccountingDto.Row rowClass3a = from(class3a);

            AccountingDto.Row assetsRow = new AccountingDto.Row(AccountingDto.Type.BALANCE_SUMMARY, ASSETS.toUpperCase(), "a");
            assetsRow.setInitial(rowClass0.getInitial() + rowClass1.getInitial() + rowClass2a.getInitial() + rowClass3a.getInitial());
            assetsRow.setMonthlyValues(Utils.mergeIntegerArrays(rowClass0.getMonthlyValues(), rowClass1.getMonthlyValues(), rowClass2a.getMonthlyValues(), rowClass3a.getMonthlyValues()));
            assetsRow.setTotal(rowClass0.getTotal() + rowClass1.getTotal() + rowClass2a.getTotal() + rowClass3a.getTotal());
            balanceSheetDto.getRows().add(assetsRow);

            balanceSheetDto.getRows().add(rowClass0);
            balanceSheetDto.getRows().add(rowClass1);
            balanceSheetDto.getRows().add(rowClass2a);
            balanceSheetDto.getRows().add(rowClass3a);

            AccountingDto.Row rowClass2l = from(class2l);
            AccountingDto.Row rowClass3l = from(class3l);
            AccountingDto.Row rowClass4 = from(class4);
            AccountingDto.Row rowProfit = new AccountingDto.Row(AccountingDto.Type.BALANCE_CLASS, PROFIT, "p");
            rowProfit.setInitial(0);
            rowProfit.setMonthlyValues(transactionService.getMonthlyProfit(year));
            rowProfit.setTotal(Utils.sumArray(rowProfit.getMonthlyValues()));

            AccountingDto.Row liabilitiesRow = new AccountingDto.Row(AccountingDto.Type.BALANCE_SUMMARY, LIABILITIES.toUpperCase(), "l");
            liabilitiesRow.setInitial(rowClass2l.getInitial() + rowClass3l.getInitial() + rowClass4.getInitial());
            liabilitiesRow.setMonthlyValues(Utils.mergeIntegerArrays(rowClass2l.getMonthlyValues(), rowClass3l.getMonthlyValues(), rowClass4.getMonthlyValues(), rowProfit.getMonthlyValues()));
            liabilitiesRow.setTotal(rowClass2l.getTotal() + rowClass3l.getTotal() + rowClass4.getTotal() + rowProfit.getTotal());
            balanceSheetDto.getRows().add(liabilitiesRow);

            balanceSheetDto.getRows().add(rowClass2l);
            balanceSheetDto.getRows().add(rowClass3l);
            balanceSheetDto.getRows().add(rowClass4);
            balanceSheetDto.getRows().add(rowProfit);

            return balanceSheetDto;
        });
    }

    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/balance")
    public Response getOverallBalanceSheet()
    {
        return Endpoint.process(() -> {}, () ->
        {
            AccountingYearlyData yearlyClosingData = service.getYearlyClosingData();
            AccountingYearlyData yearlyProfitData = service.getYearlyProfitData();

            String[] years = yearlyClosingData.getYears();

            // No years means nothing has been synced yet. The statement is then simply empty,
            // rather than an attempt to read the last of no years at all.
            if (years.length == 0) return new AccountingDto(years, AccountingDto.Type.BALANCE_SUMMARY);

            Map<String, AccountingData> balanceData = service.getBalanceData(years[years.length - 1]);

            ClassComponent class0 = balanceData.get("0").getClassComponent();
            ClassComponent class1 = balanceData.get("1").getClassComponent();
            ClassComponent class2a = balanceData.get("2").getClassComponent("0", "1", "3");
            ClassComponent class3a = balanceData.get("3").getClassComponent("0");

            ClassComponent class2l = balanceData.get("2").getClassComponent("2");
            ClassComponent class3l = balanceData.get("3").getClassComponent("1");
            ClassComponent class4 = balanceData.get("4").getClassComponent();

            AccountingDto balanceSheetDto = new AccountingDto(years, AccountingDto.Type.BALANCE_SUMMARY);

            AccountingDto.Row rowClass0 = from(class0, yearlyClosingData, yearlyClosingData.getYearlyClassValues("0"));
            AccountingDto.Row rowClass1 = from(class1, yearlyClosingData, yearlyClosingData.getYearlyClassValues("1"));
            AccountingDto.Row rowClass2a = from(class2a, yearlyClosingData, yearlyClosingData.getYearlyClassValues("2", "0", "1", "3"));
            AccountingDto.Row rowClass3a = from(class3a, yearlyClosingData, yearlyClosingData.getYearlyClassValues("3", "0"));

            AccountingDto.Row assetsRow = new AccountingDto.Row(AccountingDto.Type.BALANCE_SUMMARY, ASSETS.toUpperCase(), "a");
            assetsRow.setYearlyValues(Utils.mergeIntegerArrays(rowClass0.getYearlyValues(), rowClass1.getYearlyValues(), rowClass2a.getYearlyValues(), rowClass3a.getYearlyValues()));
            balanceSheetDto.getRows().add(assetsRow);

            balanceSheetDto.getRows().add(rowClass0);
            balanceSheetDto.getRows().add(rowClass1);
            balanceSheetDto.getRows().add(rowClass2a);
            balanceSheetDto.getRows().add(rowClass3a);

            AccountingDto.Row rowClass2l = from(class2l, yearlyClosingData, yearlyClosingData.getYearlyClassValues("2", "2"));
            AccountingDto.Row rowClass3l = from(class3l, yearlyClosingData, yearlyClosingData.getYearlyClassValues("3", "1"));
            AccountingDto.Row rowClass4 = from(class4, yearlyClosingData, yearlyClosingData.getYearlyClassValues("4"));
            AccountingDto.Row rowProfit = new AccountingDto.Row(AccountingDto.Type.BALANCE_CLASS, PROFIT, "p");
            Integer[] yearlyProfit = yearlyProfitData.getYearlyOverallValues();
            yearlyProfit[years.length - 1] = Utils.sumArray(transactionService.getMonthlyProfit(years[years.length - 1]));
            rowProfit.setYearlyValues(yearlyProfit);

            AccountingDto.Row liabilitiesRow = new AccountingDto.Row(AccountingDto.Type.BALANCE_SUMMARY, LIABILITIES.toUpperCase(), "l");
            liabilitiesRow.setYearlyValues(Utils.mergeIntegerArrays(rowClass2l.getYearlyValues(), rowClass3l.getYearlyValues(), rowClass4.getYearlyValues(), rowProfit.getYearlyValues()));
            balanceSheetDto.getRows().add(liabilitiesRow);

            balanceSheetDto.getRows().add(rowClass2l);
            balanceSheetDto.getRows().add(rowClass3l);
            balanceSheetDto.getRows().add(rowClass4);
            balanceSheetDto.getRows().add(rowProfit);

            return balanceSheetDto;
        });
    }

    @GET
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

    /**
     * An overall balance sheet class, with the groups and schema accounts it expands into.
     * <p>
     * The yearly view expands the same three levels; the overall one can now do it too, because
     * a schema account keeps its id from year to year, so one branch of the schema can be
     * followed across all of them.
     */
    private AccountingDto.Row from(ClassComponent classComponent, AccountingYearlyData yearlyData, Integer[] yearly)
    {
        AccountingDto.Row row = new AccountingDto.Row(AccountingDto.Type.BALANCE_CLASS, classComponent.getName(), classComponent.getSchemaId());
        row.setYearlyValues(withRunningYear(yearly, classComponent.getBalance()));

        for (GroupComponent groupComponent : classComponent.getGroups())
        {
            AccountingDto.Row groupRow = new AccountingDto.Row(AccountingDto.Type.BALANCE_GROUP, groupComponent.getName(), groupComponent.getSchemaId());
            groupRow.setYearlyValues(withRunningYear(yearlyData.getYearlyValues(groupComponent.getSchemaId()), groupComponent.getBalance()));

            for (GroupComponent.AccountComponent accountComponent : groupComponent.getAccounts())
            {
                AccountingDto.Row accountRow = new AccountingDto.Row(AccountingDto.Type.BALANCE_ACCOUNT, accountComponent.getName(), accountComponent.getSchemaId());
                accountRow.setYearlyValues(withRunningYear(yearlyData.getYearlyValues(accountComponent.getSchemaId()), accountComponent.getBalance()));
                groupRow.getChildren().add(accountRow);
            }
            row.getChildren().add(groupRow);
        }
        return row;
    }

    /**
     * The last column is the year that is still running: it has not been closed, so there is no
     * closing transaction to read it from and its value is the balance of that year's own data.
     */
    private Integer[] withRunningYear(Integer[] yearly, Integer balance)
    {
        yearly[yearly.length - 1] = balance;
        return yearly;
    }

    private AccountingDto.Row from(ClassComponent classComponent)
    {
        AccountingDto.Row classRow = new AccountingDto.Row(AccountingDto.Type.BALANCE_CLASS, classComponent.getName(), classComponent.getSchemaId());
        classRow.setInitial(classComponent.getInitialValue());
        classRow.setMonthlyValues(classComponent.getMonthlyBalance());
        classRow.setTotal(classComponent.getBalance());
        for (GroupComponent groupComponent : classComponent.getGroups())
        {
            AccountingDto.Row groupRow = new AccountingDto.Row(AccountingDto.Type.BALANCE_GROUP, groupComponent.getName(), groupComponent.getSchemaId());
            groupRow.setInitial(groupComponent.getInitialValue());
            groupRow.setMonthlyValues(groupComponent.getMonthlyBalance());
            groupRow.setTotal(groupComponent.getBalance());
            for (GroupComponent.AccountComponent accountComponent : groupComponent.getAccounts())
            {
                AccountingDto.Row accountRow = new AccountingDto.Row(AccountingDto.Type.BALANCE_ACCOUNT, accountComponent.getName(), accountComponent.getSchemaId());
                accountRow.setInitial(accountComponent.getInitialValue());
                accountRow.setMonthlyValues(accountComponent.getMonthlyBalance());
                accountRow.setTotal(accountComponent.getBalance());
                groupRow.getChildren().add(accountRow);
            }
            classRow.getChildren().add(groupRow);
        }
        return classRow;
    }

    private AccountingDto.Row from(GroupComponent groupComponent, AccountingDto.Type type, AccountingYearlyData yearlyData, Integer[] yearly)
    {
        return from(groupComponent, type, yearlyData, yearly, false);
    }

    /**
     * An overall income statement or cash flow group, with the schema accounts it expands into.
     * <p>
     * {@code inverted} is for the cash flow's liabilities group, whose component has already been
     * inverted: the closing years of its accounts have to be flipped the same way, so that a row
     * and the rows under it carry the same sign.
     */
    private AccountingDto.Row from(GroupComponent groupComponent, AccountingDto.Type type, AccountingYearlyData yearlyData, Integer[] yearly, boolean inverted)
    {
        AccountingDto.Row row = new AccountingDto.Row(type, groupComponent.getName(), groupComponent.getSchemaId());
        row.setYearlyValues(withRunningYear(yearly, groupComponent.getBalance()));
        row.setTotal(Utils.sumArray(row.getYearlyValues()));

        for (GroupComponent.AccountComponent accountComponent : groupComponent.getAccounts())
        {
            AccountingDto.Row accountRow = new AccountingDto.Row(accountType(type), accountComponent.getName(), accountComponent.getSchemaId());
            Integer[] accountYearly = yearlyData.getYearlyValues(accountComponent.getSchemaId());
            if (inverted) accountYearly = Utils.invertValues(accountYearly);
            accountRow.setYearlyValues(withRunningYear(accountYearly, accountComponent.getBalance()));
            accountRow.setTotal(Utils.sumArray(accountRow.getYearlyValues()));
            row.getChildren().add(accountRow);
        }
        return row;
    }

    /** The row type the accounts of a group are reported under. */
    private AccountingDto.Type accountType(AccountingDto.Type groupType)
    {
        if (groupType == AccountingDto.Type.INCOME_GROUP) return AccountingDto.Type.INCOME_ACCOUNT;
        if (groupType == AccountingDto.Type.EXPENSE_GROUP) return AccountingDto.Type.EXPENSE_ACCOUNT;
        if (groupType == AccountingDto.Type.CASH_FLOW_GROUP) return AccountingDto.Type.CASH_FLOW_ACCOUNT;
        return null;
    }

    private AccountingDto.Row from(GroupComponent groupComponent, AccountingDto.Type type)
    {
        AccountingDto.Row groupRow = new AccountingDto.Row(type, groupComponent.getName(), groupComponent.getSchemaId());
        if (type == AccountingDto.Type.CASH_FLOW_GROUP) groupRow.setInitial(groupComponent.getInitialValue());
        groupRow.setMonthlyValues(groupComponent.getMonthlyBalance());
        groupRow.setTotal(groupComponent.getBalance());
        for (GroupComponent.AccountComponent accountComponent : groupComponent.getAccounts()){
            AccountingDto.Row accountRow = new AccountingDto.Row(accountType(type), accountComponent.getName(), accountComponent.getSchemaId());
            if (type == AccountingDto.Type.CASH_FLOW_GROUP) accountRow.setInitial(accountComponent.getInitialValue());
            accountRow.setMonthlyValues(accountComponent.getMonthlyBalance());
            accountRow.setTotal(accountComponent.getBalance());
            groupRow.getChildren().add(accountRow);
        }
        return groupRow;
    }
}
