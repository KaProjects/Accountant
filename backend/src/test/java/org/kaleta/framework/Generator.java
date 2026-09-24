package org.kaleta.framework;

import org.kaleta.entity.Account;
import org.kaleta.entity.AccountId;
import org.kaleta.entity.Budgeting;
import org.kaleta.entity.Schema;
import org.kaleta.entity.Transaction;
import org.kaleta.entity.YearId;

import java.util.Random;

/**
 * Builders for the entities the accounting logic operates on, so tests can state
 * only the values they care about.
 */
public class Generator
{
    private static final Random RANDOM = new Random();

    /**
     * @param date day and month, in the DDMM form the Transaction.date column uses
     */
    public static Transaction transaction(String year, String date, Integer amount, String debit, String credit)
    {
        return transaction(year, date, amount, debit, credit, "");
    }

    public static Transaction transaction(String year, String date, Integer amount, String debit, String credit, String description)
    {
        Transaction transaction = new Transaction();
        transaction.setYear(year);
        transaction.setDate(date);
        transaction.setAmount(amount);
        transaction.setDebit(debit);
        transaction.setCredit(credit);
        transaction.setDescription(description);
        return transaction;
    }

    /**
     * @param fullId account id in the "schemaId.semanticId" form, for example "230.1"
     */
    public static Account account(String year, String fullId)
    {
        return account(year, fullId, "account " + fullId, "");
    }

    public static Account account(String year, String fullId, String name, String metadata)
    {
        Account account = new Account();
        account.setAccountId(accountId(year, fullId));
        account.setName(name);
        account.setMetadata(metadata);
        return account;
    }

    public static AccountId accountId(String year, String fullId)
    {
        AccountId accountId = new AccountId();
        accountId.setYear(year);
        accountId.setSchemaId(fullId.split("\\.")[0]);
        accountId.setSemanticId(fullId.contains(".") ? fullId.split("\\.")[1] : "");
        return accountId;
    }

    public static Schema schema(String year, String id, String name, String type)
    {
        Schema schema = new Schema();
        YearId yearId = new YearId();
        yearId.setYear(year);
        yearId.setId(id);
        schema.setYearId(yearId);
        schema.setName(name);
        schema.setType(type);
        return schema;
    }

    public static Budgeting budgeting(String year, String id, String name)
    {
        Budgeting budgeting = new Budgeting();
        YearId yearId = new YearId();
        yearId.setYear(year);
        yearId.setId(id);
        budgeting.setYearId(yearId);
        budgeting.setName(name);
        return budgeting;
    }

    /**
     * A budgeting row carrying only a planning value, as group rows that aggregate
     * their sub-rows do.
     */
    public static Budgeting budgetingPlan(String year, String id, String planning)
    {
        Budgeting budgeting = budgeting(year, id, "row " + id);
        budgeting.setPlanning(planning);
        return budgeting;
    }

    /**
     * A budgeting row bound to accounts, which is what actual monthly figures are
     * computed from.
     */
    public static Budgeting budgetingRow(String year, String id, String debit, String credit, String description, String planning)
    {
        Budgeting budgeting = budgeting(year, id, "row " + id);
        budgeting.setDebit(debit);
        budgeting.setCredit(credit);
        budgeting.setDescription(description);
        budgeting.setPlanning(planning);
        return budgeting;
    }

    public static Integer[] monthly(int... values)
    {
        Integer[] result = new Integer[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[i];
        }
        return result;
    }

    public static Integer[] randomMonthlyValues(int bound)
    {
        Integer[] result = new Integer[12];
        for (int i = 0; i < 12; i++) {
            result[i] = RANDOM.nextInt(bound);
        }
        return result;
    }
}
