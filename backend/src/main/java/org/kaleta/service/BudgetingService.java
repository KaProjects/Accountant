package org.kaleta.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.kaleta.Utils;
import org.kaleta.dto.BudgetDto;
import org.kaleta.model.BudgetComponent;
import org.kaleta.model.BudgetingData;
import org.kaleta.persistence.api.BudgetingDao;
import org.kaleta.persistence.entity.Budgeting;
import org.kaleta.persistence.entity.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.kaleta.dto.BudgetDto.Row.Type.BALANCE;
import static org.kaleta.dto.BudgetDto.Row.Type.EXPENSE;
import static org.kaleta.dto.BudgetDto.Row.Type.EXPENSE_SUM;
import static org.kaleta.dto.BudgetDto.Row.Type.INCOME;
import static org.kaleta.dto.BudgetDto.Row.Type.INCOME_SUM;
import static org.kaleta.dto.BudgetDto.Row.Type.OF_BUDGET;
import static org.kaleta.dto.BudgetDto.Row.Type.OF_BUDGET_BALANCE;

@ApplicationScoped
public class BudgetingService
{
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final BudgetingDao budgetingDao;

    @Inject
    public BudgetingService(TransactionService transactionService, AccountService accountService, BudgetingDao budgetingDao)
    {
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.budgetingDao = budgetingDao;
    }

    /**
     * @return budget data for specified year
     */
    public BudgetingData getBudgetData(String year)
    {
        List<Budgeting> schema = budgetingDao.getSchema(year);
        List<Transaction> transactions = transactionService.getBudgetTransactions(year);
        return new BudgetingData(schema, transactions);
    }

    /**
     * @return list of transactions for specified ID, year and month
     */
    public List<Transaction> getBudgetTransactions(String year, String budgetId, String month)
    {
        Budgeting schema = budgetingDao.getSchemaById(year, budgetId);

        if (schema.getDebit() == null && schema.getCredit() == null){
            throw new IllegalArgumentException("Budget schema specified by budgetId='" + budgetId + "' doesn't have debit/credit accounts specified.");
        }

        List<Transaction> transactions = new ArrayList<>();
        if (schema.getDebit().equals(schema.getCredit())) {
            transactions.addAll(transactionService.getTransactionsMatching(year, schema.getDebit(), "", schema.getDescription()));
            List<Transaction> creditTransactions = transactionService.getTransactionsMatching(year, "", schema.getCredit(), schema.getDescription());
            creditTransactions.forEach(transaction -> transaction.setAmount(-transaction.getAmount()));
            transactions.addAll(creditTransactions);
        } else if (schema.getDescription() != null && schema.getDescription().equals("finXasset")) {
            transactions.addAll(transactionService.getTransactionsMatching(year, schema.getDebit(), "", ""));
            List<Transaction> creditTransactions = transactionService.getTransactionsMatching(year, "", schema.getCredit(), "Sale of ");
            creditTransactions.forEach(transaction -> transaction.setAmount(-transaction.getAmount()));
            transactions.addAll(creditTransactions);
        } else {
            transactions.addAll(transactionService.getTransactionsMatching(year, schema.getDebit(), schema.getCredit(), schema.getDescription()));
        }

        transactions.removeIf(transaction -> !transaction.getDate().endsWith(month.length() == 1 ? "0" + month : month));

        // filter correcting transactions between same schema account
        transactions.removeIf(transaction -> transaction.getDebit().substring(0,3).equals(transaction.getCredit().substring(0,3)));

        Map<String, String> accountNames = accountService.getAccountNamesMap(year);
        transactions.forEach(transaction -> {
            transaction.setDebit(accountNames.get(transaction.getDebit()));
            transaction.setCredit(accountNames.get(transaction.getCredit()));
        });

        return transactions;
    }

    /**
     * @return the budget of the specified year: its income, mandatory and other expenses and
     * off-budget rows, actual and planned month by month, with the balances computed from them
     */
    public BudgetDto getBudget(String year)
    {
        BudgetingData budgetData = getBudgetData(year);

        BudgetComponent incomeComponent = budgetData.getBudgetComponent("i", "Income");
        BudgetComponent mandatoryExpensesComponent = budgetData.getBudgetComponent("me", "Total Mandatory Expenses");
        BudgetComponent expensesComponent = budgetData.getBudgetComponent("e", "Total Expenses");
        BudgetComponent ofBudgetComponent = budgetData.getBudgetComponent("of", "Desired CF");

        BudgetDto budgetDto = new BudgetDto(year, computeLastFilledMonth(List.of(incomeComponent, mandatoryExpensesComponent, expensesComponent)));

        constructBudgetComponentDtoRows(incomeComponent, budgetDto, INCOME);

        budgetDto.addRow(INCOME_SUM, incomeComponent.getName(), "i", incomeComponent.getActualMonths(), incomeComponent.getPlannedMonths());

        constructBudgetComponentDtoRows(mandatoryExpensesComponent, budgetDto, EXPENSE);

        budgetDto.addRow(EXPENSE_SUM, mandatoryExpensesComponent.getName(), "me", mandatoryExpensesComponent.getActualMonths(), mandatoryExpensesComponent.getPlannedMonths());

        Integer[] netAfterTme = Utils.subtractIntegerArrays(incomeComponent.getActualMonths(), mandatoryExpensesComponent.getActualMonths());
        Integer[] netAfterTmePlanned = Utils.subtractIntegerArrays(incomeComponent.getPlannedMonths(), mandatoryExpensesComponent.getPlannedMonths());
        budgetDto.addRow(BALANCE, "Net after TME", "ntme", netAfterTme, netAfterTmePlanned);

        constructBudgetComponentDtoRows(expensesComponent, budgetDto, EXPENSE);

        Integer[] budgetCf = Utils.subtractIntegerArrays(netAfterTme, expensesComponent.getActualMonths());
        Integer[] budgetCfPlanned = Utils.subtractIntegerArrays(netAfterTmePlanned, expensesComponent.getPlannedMonths());
        budgetDto.addRow(BALANCE, "Budget CF", "bcf", budgetCf, budgetCfPlanned);

        constructBudgetComponentDtoRows(ofBudgetComponent, budgetDto, OF_BUDGET);
        budgetDto.addRow(OF_BUDGET_BALANCE, ofBudgetComponent.getName(), "dcf", ofBudgetComponent.getActualMonths(), ofBudgetComponent.getPlannedMonths());

        return budgetDto;
    }

    private Integer computeLastFilledMonth(List<BudgetComponent> components)
    {
        Boolean[] flags = new Boolean[]{false,false,false,false,false,false,false,false,false,false,false,false};
        for (BudgetComponent component: components){
            Integer[] months = component.getActualMonths();
            for(int m=0;m<months.length;m++){
                if (!flags[m] && months[m] != 0){
                    flags[m] = true;
                }
            }
        }
        int lastFilledMonth = 0;
        for (int m=0;m<12;m++){
            if (flags[m]){
                lastFilledMonth = m + 1;
            }
        }
        return lastFilledMonth;
    }

    private void constructBudgetComponentDtoRows(BudgetComponent component, BudgetDto dto, BudgetDto.Row.Type type)
    {
        for (BudgetComponent.Row row : component.getRows()){
            BudgetDto.Row rowDto = dto.addRow(type, row.getName(), row.getId(), row.getActualMonths(), row.getPlannedMonths());
            for (BudgetComponent.Row subRows : row.getSubRows()){
                rowDto.addSubRow(subRows.getName(), subRows.getId(), subRows.getActualMonths(), subRows.getMonthsPlanned());
            }
        }
    }
}
