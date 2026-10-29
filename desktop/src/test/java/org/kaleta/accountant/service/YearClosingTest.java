package org.kaleta.accountant.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.backend.model.TransactionsModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.core.TestParent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Closing a year: what it books, what it refuses to do, and what the next year starts with.
 * <p>
 * The books are kept in one place here: a bank account that ends the year with money on it, an
 * expense that was used, an expense that was not, and a revenue - which between them cover every
 * rule the closing has.
 */
public class YearClosingTest extends TestParent {
    private static final String NEXT = "next";
    private static final String BANK = "210.0";
    private static final String EXPENSE_USED = "520.0";
    private static final String EXPENSE_UNUSED = "520.1";
    private static final String REVENUE = "600.0";

    @Before
    public void keepBooks() {
        // the test workspace starts with classes 3, 4 and 7 only, so the books need a bank account,
        // an expense and a revenue - the accumulated earnings the profit goes onto comes with the
        // first year, as it does in a freshly initialised app
        Service.SCHEMA.createGroup(YEAR, "2", "1", "bank accounts");
        Service.SCHEMA.createAccount(YEAR, "2", "1", "0", "current accounts", Constants.AccountType.ASSET);
        Service.SCHEMA.createGroup(YEAR, "5", "2", "services");
        Service.SCHEMA.createAccount(YEAR, "5", "2", "0", "rent", Constants.AccountType.EXPENSE);
        Service.SCHEMA.createAccount(YEAR, "5", "2", "1", "never used", Constants.AccountType.EXPENSE);
        Service.SCHEMA.createGroup(YEAR, "6", "0", "work");
        Service.SCHEMA.createAccount(YEAR, "6", "0", "0", "salary", Constants.AccountType.REVENUE);

        create(BANK, "bank");
        create(EXPENSE_USED, "rent");
        create(EXPENSE_UNUSED, "never used");
        create(REVENUE, "salary");

        Service.TRANSACTIONS.addTransaction(YEAR, "0101", "0", BANK, Constants.Account.INIT_ACC_ID, "initiation");
        Service.TRANSACTIONS.addTransaction(YEAR, "1501", "1000", BANK, REVENUE, "salary");
        Service.TRANSACTIONS.addTransaction(YEAR, "2001", "400", EXPENSE_USED, BANK, "rent");
    }

    private void create(String fullId, String name) {
        Service.ACCOUNT.createAccount(YEAR, name, fullId.split("\\.")[0], fullId.split("\\.")[1], "");
    }

    @Test
    public void theProfitIsWhatWasEarnedLessWhatWasSpent() {
        ClosingService.Plan plan = Service.CLOSING.prepare(YEAR, NEXT);

        Assert.assertTrue(String.join("; ", plan.getProblems()), plan.isClosable());
        Assert.assertEquals(600, plan.getProfit());
        Assert.assertEquals(600, plan.getClosingValue(BANK));
    }

    /**
     * Every account is offered, and an account holding a balance is offered but cannot be dropped:
     * the balance has to go somewhere. Only what is not a decision is left out.
     */
    @Test
    public void everyAccountIsOfferedAndOnlyABalanceIsBinding() {
        create("210.1", "an empty bank account");

        ClosingService.Plan plan = Service.CLOSING.prepare(YEAR, NEXT);

        Assert.assertTrue("it holds money, so it must come over", required(plan).contains(BANK));
        Assert.assertTrue("a bank account that happens to be empty is still kept",
                optional(plan).contains("210.1"));
        Assert.assertTrue(optional(plan).contains(EXPENSE_USED));
        Assert.assertTrue(optional(plan).contains(EXPENSE_UNUSED));
        Assert.assertTrue(optional(plan).contains(REVENUE));
        Assert.assertFalse("the capital the books stand on is not a decision",
                optional(plan).contains(Constants.Account.ACCUMULATED_EARNINGS_ACC_ID));
    }

    /** Nothing is written by working the closing out. */
    @Test
    public void preparingWritesNothing() {
        int transactions = Service.TRANSACTIONS.getTransactions(YEAR, null, null).size();

        Service.CLOSING.prepare(YEAR, NEXT);

        Assert.assertEquals(transactions, Service.TRANSACTIONS.getTransactions(YEAR, null, null).size());
        Assert.assertFalse(Service.CONFIG.getYears().contains(NEXT));
    }

    /**
     * A year is closed once everything that happened in it has been entered, which cannot be in
     * June: transactions of that year are still to come.
     */
    @Test
    public void aYearIsNotClosedWhileItIsStillBeingLivedIn() {
        Service.CONFIG.initYearData("2026");

        List<String> problems = Service.CLOSING.prepare("2026", "2027", LocalDate.of(2026, 6, 30)).getProblems();

        Assert.assertTrue(problems.toString(), problems.stream().anyMatch(problem ->
                problem.contains("2026 is closed once all of its transactions are entered")
                        && problem.contains("Enabled from 29.12.2026.")));
    }

    @Test
    public void theLastFewDaysOfTheYearAreLateEnough() {
        Assert.assertTrue(noCalendarProblem(LocalDate.of(2026, 12, 29)));
        Assert.assertTrue(noCalendarProblem(LocalDate.of(2026, 12, 31)));
        Assert.assertFalse(noCalendarProblem(LocalDate.of(2026, 12, 28)));
    }

    /** The calendar check can be lifted, which is how the procedure is tried out in September. */
    @Test
    public void theCalendarCanBeSetAsideByAProperty() {
        Assert.assertFalse(noCalendarProblem(LocalDate.of(2026, 6, 30)));

        System.setProperty(ClosingService.ANY_TIME, "true");
        try {
            Assert.assertTrue(noCalendarProblem(LocalDate.of(2026, 6, 30)));
        } finally {
            System.clearProperty(ClosingService.ANY_TIME);
        }
    }

    /** ... or by a file in the data directory, which a running app picks up without a restart. */
    @Test
    public void theCalendarCanBeSetAsideByAFile() throws java.io.IOException {
        Assert.assertFalse(noCalendarProblem(LocalDate.of(2026, 6, 30)));

        java.io.File marker = new java.io.File(org.kaleta.accountant.Initializer.getDataSource()
                + ClosingService.ANY_TIME_FILE);
        Assert.assertTrue(marker.createNewFile());
        try {
            Assert.assertTrue(noCalendarProblem(LocalDate.of(2026, 6, 30)));
        } finally {
            Assert.assertTrue(marker.delete());
        }
    }

    @Test
    public void onceTheYearIsOverItCanAlwaysBeClosed() {
        Assert.assertTrue(noCalendarProblem(LocalDate.of(2027, 3, 1)));
        Assert.assertFalse("a year that has not started yet is not closed either",
                noCalendarProblem(LocalDate.of(2025, 12, 31)));
    }

    private boolean noCalendarProblem(LocalDate today) {
        if (!Service.CONFIG.getYears().contains("2026")) {
            Service.CONFIG.initYearData("2026");
        }
        return Service.CLOSING.prepare("2026", "2027", today).getProblems().stream()
                .noneMatch(problem -> problem.contains("all of its transactions are entered"));
    }

    @Test
    public void aYearThatAlreadyExistsIsRefused() {
        ClosingService.Plan plan = Service.CLOSING.prepare(YEAR, YEAR);

        Assert.assertFalse(plan.isClosable());
        Assert.assertTrue(plan.getProblems().get(0).contains("already exists"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void aYearThatCannotBeClosedIsNotClosed() {
        Service.CLOSING.close(Service.CLOSING.prepare(YEAR, YEAR), YEAR, Collections.emptyList());
    }

    @Test
    public void theNewYearOpensWithWhatWasCarried() {
        ClosingService.Plan plan = Service.CLOSING.prepare(YEAR, NEXT);

        Service.CLOSING.close(plan, NEXT, List.of(BANK, EXPENSE_USED, REVENUE));

        List<String> carried = Service.ACCOUNT.getAllAccounts(NEXT).stream()
                .map(AccountsModel.Account::getFullId).collect(Collectors.toList());
        Assert.assertTrue(carried.contains(BANK));
        Assert.assertTrue(carried.contains(EXPENSE_USED));
        Assert.assertFalse("the unused expense was left behind", carried.contains(EXPENSE_UNUSED));

        Assert.assertEquals("the bank account opens with what it closed with",
                Integer.valueOf(600), Service.TRANSACTIONS.getAccountBalance(NEXT, Service.ACCOUNT.getAccount(NEXT, BANK)));
        // a liability reads what it was credited with, so the profit shows as a positive balance
        Assert.assertEquals("the profit is on the accumulated earnings", Integer.valueOf(600),
                Service.TRANSACTIONS.getAccountBalance(NEXT, Service.ACCOUNT.getAccount(NEXT, Constants.Account.ACCUMULATED_EARNINGS_ACC_ID)));
        Assert.assertEquals(NEXT, Service.CONFIG.getActiveYear());
    }

    /**
     * Every asset and liability is opened, balance or not: the app reads an account's initial value
     * from that transaction, and one that was never opened has no value to read.
     */
    @Test
    public void everyAssetAndLiabilityIsOpenedEvenAtNothing() {
        create("210.1", "an empty bank account");
        ClosingService.Plan plan = Service.CLOSING.prepare(YEAR, NEXT);

        Service.CLOSING.close(plan, NEXT, List.of(BANK, "210.1", EXPENSE_USED, REVENUE));

        Assert.assertEquals("it opens at nothing, but it opens", Integer.valueOf(0),
                Service.TRANSACTIONS.getAccountInitialValue(NEXT, Service.ACCOUNT.getAccount(NEXT, "210.1")));
        Assert.assertEquals(Integer.valueOf(600),
                Service.TRANSACTIONS.getAccountInitialValue(NEXT, Service.ACCOUNT.getAccount(NEXT, BANK)));
    }

    /**
     * The year that was closed holds its closing entries: every account is booked out to the
     * closing or the profit account, which is what the balance of a closed account then reads as.
     */
    @Test
    public void theClosedYearIsBookedOut() {
        ClosingService.Plan plan = Service.CLOSING.prepare(YEAR, NEXT);

        Service.CLOSING.close(plan, NEXT, List.of(BANK));

        List<TransactionsModel.Transaction> closingOfTheBank =
                Service.TRANSACTIONS.getTransactions(YEAR, Constants.Account.CLOSING_ACC_ID, BANK);
        Assert.assertEquals(1, closingOfTheBank.size());
        Assert.assertEquals("600", closingOfTheBank.get(0).getAmount());
        Assert.assertEquals("3112", closingOfTheBank.get(0).getDate());

        List<TransactionsModel.Transaction> theProfit = Service.TRANSACTIONS.getTransactions(
                YEAR, Constants.Account.PROFIT_ACC_ID, Constants.Account.CLOSING_ACC_ID);
        Assert.assertEquals(1, theProfit.size());
        Assert.assertEquals("600", theProfit.get(0).getAmount());
    }

    /** A procedure that books an account which is not carried over is corrected, not left broken. */
    @Test
    public void aProcedureBookingAnAccountThatIsLeftBehindIsFixed() {
        ProceduresModel.Group.Procedure.Transaction booked = new ProceduresModel.Group.Procedure.Transaction();
        booked.setDescription("rent");
        booked.setAmount("400");
        booked.setDebit(EXPENSE_UNUSED);
        booked.setCredit(BANK);
        Service.PROCEDURES.createProcedure(YEAR, "the rent", "household", Collections.singletonList(booked));

        Assert.assertEquals(List.of("the rent"), Service.PROCEDURES.getProceduresBooking(YEAR, EXPENSE_UNUSED));

        ClosingService.Plan plan = Service.CLOSING.prepare(YEAR, NEXT);
        Service.CLOSING.close(plan, NEXT, List.of(BANK, EXPENSE_USED, REVENUE));

        Assert.assertTrue("the procedure booked nothing else, so it went with the account",
                Service.PROCEDURES.getProcedureGroupList(NEXT).stream()
                        .flatMap(group -> group.getProcedure().stream()).findAny().isEmpty());
    }

    /**
     * A procedure the app wrote for an account goes whole when that account does, the way it was
     * created with it - and the closing says so before it happens.
     */
    @Test
    public void aDerivedProcedureGoesWithTheAccountItWasWrittenFor() {
        ProceduresModel.Group.Procedure.Transaction booked = new ProceduresModel.Group.Procedure.Transaction();
        booked.setDescription("repayment of the loan");
        booked.setAmount("400");
        booked.setDebit(EXPENSE_UNUSED);
        booked.setCredit(BANK);
        Service.PROCEDURES.createManagedProcedure(YEAR, "repayment of the loan",
                Constants.Procedure.REPAYMENT_GROUP_NAME, Collections.singletonList(booked));

        Assert.assertTrue("what the app wrote for an account is not something to warn about",
                Service.PROCEDURES.getProceduresBooking(YEAR, EXPENSE_UNUSED).isEmpty());

        List<String> preview = Service.PROCEDURES.previewRemovalOf(List.of(EXPENSE_UNUSED));
        Assert.assertEquals(2, preview.size());
        Assert.assertTrue(preview.get(0), preview.get(0).contains("the account it was written for is not carried over"));
        Assert.assertTrue("previewing changes nothing",
                Service.PROCEDURES.getProcedureGroupList(YEAR).stream()
                        .anyMatch(group -> group.getName().equals(Constants.Procedure.REPAYMENT_GROUP_NAME)));

        ClosingService.Plan plan = Service.CLOSING.prepare(YEAR, NEXT);
        Service.CLOSING.close(plan, NEXT, List.of(BANK, EXPENSE_USED, REVENUE));

        Assert.assertTrue("the procedure and its group are gone",
                Service.PROCEDURES.getProcedureGroupList(NEXT).isEmpty());
    }

    private List<String> required(ClosingService.Plan plan) {
        List<String> ids = new ArrayList<>();
        plan.getCandidates().stream().filter(ClosingService.Candidate::isRequired)
                .forEach(candidate -> ids.add(candidate.getAccount().getFullId()));
        return ids;
    }

    private List<String> optional(ClosingService.Plan plan) {
        List<String> ids = new ArrayList<>();
        plan.getCandidates().stream().filter(candidate -> !candidate.isRequired())
                .forEach(candidate -> ids.add(candidate.getAccount().getFullId()));
        return ids;
    }
}
