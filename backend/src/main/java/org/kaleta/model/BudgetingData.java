package org.kaleta.model;

import org.kaleta.Utils;
import org.kaleta.entity.Budgeting;
import org.kaleta.entity.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BudgetingData
{
    private List<Budgeting> schema;
    private List<Transaction> transactions;

    public BudgetingData(List<Budgeting> schema, List<Transaction> transactions)
    {
        this.schema = schema;
        this.transactions = transactions;
    }

    public BudgetComponent getBudgetComponent(String id, String name)
    {
        BudgetComponent budgetComponent = new BudgetComponent();
        budgetComponent.setName(name);

        for (Budgeting schemaRow : schema.stream().filter(schema -> isMainRow(schema, id)).collect(Collectors.toList()))
        {
            BudgetComponent.Row row = budgetComponent.createRow();
            row.setName(schemaRow.getName());
            row.setId(schemaRow.getYearId().getId());
            row.setMonthsPlanned(parsePlanning(schemaRow.getPlanning()));

            List<Budgeting> schemaSubRows = schema.stream().filter(schema -> isSubRow(schema, schemaRow.getYearId().getId())).collect(Collectors.toList());

            if (!schemaSubRows.isEmpty())
            {
                for (Budgeting schemaSubRow: schemaSubRows)
                {
                    BudgetComponent.Row subRow = row.createSubRow();
                    subRow.setName(schemaSubRow.getName());
                    subRow.setId(schemaSubRow.getYearId().getId());
                    subRow.setMonthsPlanned(parsePlanning(schemaSubRow.getPlanning()));
                    subRow.setMonthsActual(getMonthlyBalanceForBudgetingRow(schemaSubRow));
                }
            } else {
                row.setMonthsActual(getMonthlyBalanceForBudgetingRow(schemaRow));
            }
        }

        return budgetComponent;
    }

    private boolean isMainRow(Budgeting schema, String id){
        return schema.getYearId().getId().startsWith(id) && !schema.getYearId().getId().contains(".");
    }

    private boolean isSubRow(Budgeting schema, String id){
        return schema.getYearId().getId().startsWith(id) && schema.getYearId().getId().contains(".");
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

    private Integer[] getMonthlyBalanceForBudgetingRow(Budgeting row)
    {
        try
        {
            String debit = row.getDebit().replace("%", "");

        } catch (NullPointerException e) {
            System.out.println(row);
            throw e;
        }
        String debit = row.getDebit().replace("%", "");
        String credit = row.getCredit().replace("%", "");
        String description = row.getDescription() == null ? "" : row.getDescription();

        if (debit.equals(credit))
        {
            List<Transaction> debitTransactions = filterTransactions(debit, "", description);
            List<Transaction> creditTransactions = filterTransactions("", credit, description);
            return Utils.subtractIntegerArrays(monthlyBalanceOf(debitTransactions), monthlyBalanceOf(creditTransactions));
        } else if (description != null && description.equals("finXasset"))
        {
            List<Transaction> debitTransactions = filterTransactions(debit, "", "");
            List<Transaction> creditTransactions = filterTransactions("", credit, "Sale of ");
            return Utils.subtractIntegerArrays(monthlyBalanceOf(debitTransactions), monthlyBalanceOf(creditTransactions));
        } else
        {
            return monthlyBalanceOf(filterTransactions(debit, credit, description));
        }
    }

    private List<Transaction> filterTransactions(String debit, String credit, String description) {
        List<Transaction> filteredTransactions = new ArrayList<>();
        for (Transaction transaction : transactions)
        {
            boolean debitCheck = debit.contains(".") ? transaction.getDebit().equals(debit) : transaction.getDebit().startsWith(debit);
            boolean creditCheck = credit.contains(".") ? transaction.getCredit().equals(credit) : transaction.getCredit().startsWith(credit);
            boolean descriptionCheck = description.startsWith("!")
                    ? !transaction.getDescription().contains(description.replace("!", ""))
                    : transaction.getDescription().contains(description);
            if (debitCheck && creditCheck && descriptionCheck) filteredTransactions.add(transaction);
        }
        return filteredTransactions;
    }

    private Integer[] monthlyBalanceOf(List<Transaction> transactions)
    {
        Integer[] monthlyBalance = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (Transaction transaction : transactions) {
            int month = Integer.parseInt(transaction.getDate().substring(2, 4));
            monthlyBalance[month - 1] += transaction.getAmount();
        }
        return monthlyBalance;
    }
}
