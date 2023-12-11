package org.kaleta.service;

import org.kaleta.Utils;
import org.kaleta.dao.BudgetingDao;
import org.kaleta.dto.YearTransactionDto;
import org.kaleta.entity.Budgeting;
import org.kaleta.model.BudgetComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class BudgetingServiceImpl implements BudgetingService
{
    @Autowired
    TransactionService transactionService;

    @Autowired
    BudgetingDao budgetingDao;

    @Override
    public BudgetComponent getBudgetComponent(String year, String name, String idPrefix)
    {
        BudgetComponent budgetComponent = new BudgetComponent();
        budgetComponent.setName(name);

        List<Budgeting> schema = budgetingDao.getSchemaByIdPrefix(year, idPrefix);
        for (Budgeting schemaRow: schema){
            // only for main rows
            if (!schemaRow.getYearId().getId().contains(".")){
                BudgetComponent.Row row = new BudgetComponent.Row();
                row.setName(schemaRow.getName());
                row.setId(schemaRow.getYearId().getId());

                if (schemaRow.getDebit() == null && schemaRow.getCredit() == null){
                    // find sub rows
                    for (Budgeting schemaSubRow: schema){
                        if (schemaSubRow.getYearId().getId().startsWith(schemaRow.getYearId().getId()+".")){
                            BudgetComponent.Row subRow = new BudgetComponent.Row();
                            subRow.setName(schemaSubRow.getName());
                            subRow.setId(schemaSubRow.getYearId().getId());
                            subRow.setMonthsActual(getMonthlyBalanceForBudgetingRow(year, schemaSubRow));
                            subRow.setMonthsPlanned(parsePlanning(schemaSubRow.getPlanning()));
                            row.getSubRows().add(subRow);
                        }
                    }
                } else {
                    row.setMonthsActual(getMonthlyBalanceForBudgetingRow(year, schemaRow));
                }

                row.setMonthsPlanned(parsePlanning(schemaRow.getPlanning()));

                budgetComponent.getRows().add(row);
            }
        }
        return budgetComponent;
    }

    @Override
    public List<YearTransactionDto> getBudgetTransactions(String year, String budgetId, String month)
    {
        Budgeting schema = budgetingDao.getSchemaById(year, budgetId);

        if (schema.getDebit() == null && schema.getCredit() == null){
            throw new IllegalArgumentException("Budget schema specified by budgetId='" + budgetId + "' doesn't have debit/credit accounts specified.");
        }

        List<YearTransactionDto> transactions = new ArrayList<>();
        if (schema.getDebit().equals(schema.getCredit())) {
            transactions.addAll(transactionService.getTransactionsMatching(year, schema.getDebit(), "", schema.getDescription()));
            transactions.addAll(transactionService.getTransactionsMatching(year, "", schema.getCredit(), schema.getDescription()));
        } else if (schema.getDescription() != null && schema.getDescription().equals("finXasset")) {
            transactions.addAll(transactionService.getTransactionsMatching(year, schema.getDebit(), "", ""));
            transactions.addAll(transactionService.getTransactionsMatching(year, "", schema.getCredit(), "Sale of "));
        } else {
            transactions.addAll(transactionService.getTransactionsMatching(year, schema.getDebit(), schema.getCredit(), schema.getDescription()));
        }
        return filterTransactionsByMonth(transactions, month);
    }

    private List<YearTransactionDto> filterTransactionsByMonth(List<YearTransactionDto> input, String month)
    {
        List<YearTransactionDto> output = new ArrayList<>();

        for (YearTransactionDto transaction : input){
            if (transaction.getDate().endsWith(month.length() == 1 ? "0" + month : month)) {
                output.add(transaction);
            }
        }

        return output;
    }

    private Integer[] parsePlanning(String planning)
    {
        Integer[] output = new Integer[12];
        if (planning == null || planning.isEmpty()) {
            Arrays.fill(output, 0);
        } else if (planning.startsWith("all=")){
            Arrays.fill(output, Integer.parseInt(planning.split("=")[1]));
        } else if (planning.split("\\|").length == 12){
            String[] split = planning.split("\\|");
            for (int i=0;i<split.length;i++){
                output[i] = Integer.parseInt(split[i]);
            }
        } else {
            throw new IllegalArgumentException("unrecognized planning value: " + planning);
        }
        return output;
    }

    private Integer[] getMonthlyBalanceForBudgetingRow(String year, Budgeting row)
    {
        if (row.getDebit().equals(row.getCredit())){
            Integer[] debitMonthlyBalance = transactionService.monthlyBalanceByAccounts(year, row.getDebit(), "", row.getDescription());
            Integer[] creditMonthlyBalance = transactionService.monthlyBalanceByAccounts(year, "", row.getCredit(), row.getDescription());
            return Utils.subtractIntegerArrays(debitMonthlyBalance, creditMonthlyBalance);
        } else if (row.getDescription() != null && row.getDescription().equals("finXasset")) {
            Integer[] creationMonthlyBalance = transactionService.monthlyBalanceByAccounts(year, row.getDebit(), "", "");
            Integer[] saleMonthlyBalance = transactionService.monthlyBalanceByAccounts(year, "", row.getCredit(), "Sale of ");
            return Utils.subtractIntegerArrays(creationMonthlyBalance, saleMonthlyBalance);
        } else {
            return transactionService.monthlyBalanceByAccounts(year, row.getDebit(), row.getCredit(), row.getDescription());
        }
    }
}
