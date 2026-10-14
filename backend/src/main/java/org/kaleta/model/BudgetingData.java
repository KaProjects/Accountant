package org.kaleta.model;

import org.kaleta.AccountUtils;
import org.kaleta.Constants;
import org.kaleta.Utils;
import org.kaleta.persistence.entity.Budgeting;
import org.kaleta.persistence.entity.Transaction;

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

    /**
     * The twelve monthly amounts a row's planning stands for: none, all=amount for the same every
     * month, or twelve amounts separated by |.
     *
     * @throws IllegalArgumentException for a planning in neither form
     */
    public static Integer[] parsePlanning(String planning)
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
        // a group row with nothing in it - the investments of a year no asset was held in
        if (row.getDebit() == null && row.getCredit() == null) return Utils.initialMonthlyBalance();

        String debit = row.getDebit();
        String credit = row.getCredit();
        String description = row.getDescription() == null ? "" : row.getDescription();

        if (debit.equals(credit))
        {
            List<Transaction> debitTransactions = filterTransactions(debit, "", description);
            List<Transaction> creditTransactions = filterTransactions("", credit, description);
            return Utils.subtractIntegerArrays(monthlyBalanceOf(debitTransactions), monthlyBalanceOf(creditTransactions));
        } else if (description.equals(Constants.Budget.FIN_ASSET))
        {
            return investedIn(debit, credit);
        } else
        {
            return monthlyBalanceOf(filterTransactions(debit, credit, description));
        }
    }

    /**
     * What was put into a financial asset, less what was taken out of it, month by month - the
     * deposits and withdrawals the financial assets view shows for it: whatever is booked to its
     * creation account or straight to the asset, and whatever leaves the asset, except the
     * revaluations of what it is worth and the opening and closing of the year.
     */
    private Integer[] investedIn(String creationId, String assetId)
    {
        String gainsId = AccountUtils.finAccountId(assetId, Constants.Schema.FIN_REV_REVALUATION_ID);
        String lossesId = AccountUtils.finAccountId(assetId, Constants.Schema.FIN_EXP_REVALUATION_ID);
        List<Transaction> in = new ArrayList<>();
        List<Transaction> out = new ArrayList<>();
        for (Transaction transaction : transactions)
        {
            boolean ofTheYear = !transaction.getDebit().startsWith("7") && !transaction.getCredit().startsWith("7");
            if (!ofTheYear) continue;
            if (transaction.getDebit().equals(creationId)
                    || (transaction.getDebit().equals(assetId) && !transaction.getCredit().equals(gainsId))) {
                in.add(transaction);
            }
            if (transaction.getCredit().equals(assetId) && !transaction.getDebit().equals(lossesId)) {
                out.add(transaction);
            }
        }
        return Utils.subtractIntegerArrays(monthlyBalanceOf(in), monthlyBalanceOf(out));
    }

    private List<Transaction> filterTransactions(String debit, String credit, String description) {
        List<Transaction> filteredTransactions = new ArrayList<>();
        for (Transaction transaction : transactions)
        {
            boolean debitCheck = matchesAccount(transaction.getDebit(), debit);
            boolean creditCheck = matchesAccount(transaction.getCredit(), credit);
            boolean descriptionCheck = description.startsWith("!")
                    ? !transaction.getDescription().contains(description.replace("!", ""))
                    : transaction.getDescription().contains(description);
            if (debitCheck && creditCheck && descriptionCheck) filteredTransactions.add(transaction);
        }
        return filteredTransactions;
    }

    /**
     * An account against a budget row's account pattern: one ending in % is a prefix, as in the
     * transactions the row lists - "511.0-%" is every account of the group 511.0, though it names
     * no account itself. Without it, an account (with a dot) has to be the very one, and anything
     * shorter - a schema id such as "510" - is again a prefix.
     */
    private static boolean matchesAccount(String account, String pattern)
    {
        if (pattern.endsWith("%")) return account.startsWith(pattern.substring(0, pattern.length() - 1));
        return pattern.contains(".") ? account.equals(pattern) : account.startsWith(pattern);
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
