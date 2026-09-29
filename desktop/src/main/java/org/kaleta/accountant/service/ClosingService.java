package org.kaleta.accountant.service;

import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.TransactionsModel;
import org.kaleta.accountant.common.Constants;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Closing a year and opening the next one.
 * <p>
 * The whole of it is worked out before anything is written: the closing transactions are composed
 * in memory, the books are checked to balance with them, and only then - once the user has said which
 * accounts to carry over - are the files touched. A year half-closed is far worse than one not
 * closed at all.
 */
public class ClosingService {

    /**
     * How many days before the end of a year it may be closed.
     * <p>
     * A year is closed once everything that happened in it has been entered, which is normally once
     * it is over - but the books may well be finished a few days early, and then there is no reason
     * to wait for January. Anything earlier than that is not a question of judgement: transactions
     * of the year are still to come, and closing would leave them nowhere to go.
     */
    private static final int DAYS_BEFORE_THE_END = 3;

    /**
     * Two ways to close a year whenever, which is how the procedure is tried out without waiting for
     * December: this system property, or a file of this name in the data directory - the file needs
     * no restart, which is the point of it. Either lifts the calendar check and nothing else.
     */
    public static final String ANY_TIME = "accountant.closing.anytime";
    public static final String ANY_TIME_FILE = "close-anytime";

    /** Everything closing a year would do, and everything that is wrong with doing it. */
    public static class Plan {
        private final String year;
        private final String closureDate;
        private final List<String[]> closingTransactions = new ArrayList<>();
        private final Map<String, Integer> closingValue = new LinkedHashMap<>();
        private final List<Candidate> accounts = new ArrayList<>();
        private final List<String> problems = new ArrayList<>();
        private final List<AccountsModel.Account> writtenOffAssets = new ArrayList<>();
        private int profit;
        private boolean balanced = true;

        Plan(String year, String closureDate) {
            this.year = year;
            this.closureDate = closureDate;
        }

        public String getYear() {
            return year;
        }

        /** The transactions that closing would book: date, amount, debit, credit, description. */
        public List<String[]> getClosingTransactions() {
            return closingTransactions;
        }

        /** What each account is worth at closing, which is what the next year would open it with. */
        public int getClosingValue(String fullId) {
            return closingValue.getOrDefault(fullId, 0);
        }

        /**
         * The accounts the user chooses from.
         * <p>
         * A fixed asset is not among them: it comes over while it is worth something and is written
         * off when it is not, and neither is a decision. Nor is anything derived from another
         * account - a depreciation, a consumption, the accounts a financial asset is booked through
         * - because those go wherever the account they belong to goes. The capital accounts are not
         * there either: the books stand on them, and every year has them.
         * <p>
         * Everything else is offered, whatever it ended the year holding: a bank account that
         * happens to be empty on the 31st is still an account that is kept.
         */
        public List<Candidate> getCandidates() {
            List<Candidate> candidates = new ArrayList<>();
            for (Candidate candidate : accounts) {
                if (!candidate.isDerived() && !candidate.getAccount().getSchemaId().startsWith("0")
                        && !isCapital(candidate)) {
                    candidates.add(candidate);
                }
            }
            return candidates;
        }

        /** Every account the year holds, whether it is a choice or not. */
        public List<Candidate> getAccounts() {
            return accounts;
        }

        /** 40x: the capital and what has accumulated on it, which every year is built on. */
        private boolean isCapital(Candidate candidate) {
            return candidate.getAccount().getSchemaId().startsWith("4" + Constants.Schema.CAPITAL_GROUP_ID);
        }

        /**
         * The fixed assets that end the year worth nothing - sold, written off, or excluded. They
         * are not carried into the new year, and neither are the accounts derived from them.
         */
        public List<AccountsModel.Account> getWrittenOffAssets() {
            return writtenOffAssets;
        }

        /** What stops the year being closed; empty when it can be. */
        public List<String> getProblems() {
            return problems;
        }

        public int getProfit() {
            return profit;
        }

        /** Whether the closing transactions balance; a plan can be right about that and still be refused. */
        public boolean isBalanced() {
            return balanced;
        }

        public boolean isClosable() {
            return problems.isEmpty();
        }
    }

    /** One account the next year could start with, and why it is or is not offered as a choice. */
    public static class Candidate {
        private final AccountsModel.Account account;
        private final String type;
        private final int closingValue;
        private final boolean used;
        private final String ownerId;

        Candidate(AccountsModel.Account account, String type, int closingValue, boolean used, String ownerId) {
            this.account = account;
            this.type = type;
            this.closingValue = closingValue;
            this.used = used;
            this.ownerId = ownerId;
        }

        /**
         * The account this one exists for, when it is one the app writes by itself - the
         * depreciation of an asset, the consumption of a resource, the creation of a financial
         * asset. Such an account goes where its owner goes, and is not a choice of its own.
         */
        public String getOwnerId() {
            return ownerId;
        }

        public boolean isDerived() {
            return ownerId != null;
        }

        public AccountsModel.Account getAccount() {
            return account;
        }

        public String getType() {
            return type;
        }

        public int getClosingValue() {
            return closingValue;
        }

        /** Whether anything was booked on it during the year that is being closed. */
        public boolean isUsed() {
            return used;
        }

        /**
         * An asset or a liability holding a balance has to come over, or the balance would simply
         * vanish and the new year would not open in balance. Those are shown, but not offered as a
         * choice. An expense or a revenue ends every year at nothing, so it is always a choice.
         */
        public boolean isRequired() {
            return account.getFullId().equals(Constants.Account.ACCUMULATED_EARNINGS_ACC_ID)
                    || (closingValue != 0 && (type.equals(Constants.AccountType.ASSET)
                            || type.equals(Constants.AccountType.LIABILITY)));
        }
    }

    ClosingService() {
        // package-private
    }

    /**
     * Works out what closing this year would book, and what it would leave for the next one.
     * Nothing is written.
     */
    public Plan prepare(String year, String newYear) {
        return prepare(year, newYear, LocalDate.now());
    }

    /** The same, as of a given day, which is what makes the "is it time yet" check testable. */
    public Plan prepare(String year, String newYear, LocalDate today) {
        Plan plan = new Plan(year, "3112");

        Map<String, Integer> balances = balances(year);
        Set<String> used = new HashSet<>(balances.keySet());

        int profit = 0;
        for (AccountsModel.Account account : Service.ACCOUNT.getAllAccounts(year)) {
            String type = Service.SCHEMA.getSchemaAccountType(year, account.getSchemaId());
            if (type.equals(Constants.AccountType.OFF_BALANCE)) {
                continue;
            }
            // a balance is read in the account's own sign: what a liability or a revenue holds is
            // what it was credited with, and that is the amount the closing entry carries
            int raw = balances.getOrDefault(account.getFullId(), 0);
            int balance = type.equals(Constants.AccountType.ASSET) || type.equals(Constants.AccountType.EXPENSE)
                    ? raw : -raw;
            String amount = String.valueOf(balance);

            switch (type) {
                case Constants.AccountType.ASSET:
                    plan.closingTransactions.add(entry(plan, amount, Constants.Account.CLOSING_ACC_ID, account.getFullId()));
                    break;
                case Constants.AccountType.LIABILITY:
                    plan.closingTransactions.add(entry(plan, amount, account.getFullId(), Constants.Account.CLOSING_ACC_ID));
                    break;
                case Constants.AccountType.EXPENSE:
                    plan.closingTransactions.add(entry(plan, amount, Constants.Account.PROFIT_ACC_ID, account.getFullId()));
                    profit -= balance;
                    break;
                case Constants.AccountType.REVENUE:
                    plan.closingTransactions.add(entry(plan, amount, account.getFullId(), Constants.Account.PROFIT_ACC_ID));
                    profit += balance;
                    break;
                default:
                    break;
            }
            plan.closingValue.put(account.getFullId(), balance);

            // a fixed asset worth nothing at the end of the year has been sold, written off or
            // excluded; it does not come over, and its depreciation accounts go with it
            if (balance == 0 && account.getSchemaId().startsWith("0")
                    && !account.getSchemaId().startsWith("0" + Constants.Schema.ACCUMULATED_DEP_GROUP_ID)) {
                plan.writtenOffAssets.add(account);
            }

            plan.accounts.add(new Candidate(account, type, balance,
                    used.contains(account.getFullId()), ownerOf(account)));
        }

        plan.profit = profit;
        plan.closingTransactions.add(entry(plan, String.valueOf(profit),
                Constants.Account.PROFIT_ACC_ID, Constants.Account.CLOSING_ACC_ID));

        checkClosing(plan);
        checkTheNewYearIsFree(newYear, plan);
        checkTheProfitHasSomewhereToGo(year, plan);
        checkItIsTimeToClose(year, today, plan);
        return plan;
    }

    /**
     * Closes the year and opens the next one with the accounts that were chosen.
     * <p>
     * The order matters: the closing transactions go in first, then the new year is created, then it is
     * filled. Everything that could refuse to happen - a year that already exists, a plan that does
     * not balance - has been checked before any of it starts.
     *
     * @param carried full ids of the accounts the new year starts with
     * @return what was done, one line per step, for the dialog to show
     */
    public List<String> close(Plan plan, String newYear, Collection<String> carried) {
        if (!plan.isClosable()) {
            throw new IllegalArgumentException("The year cannot be closed: " + String.join("; ", plan.getProblems()));
        }
        List<String> done = new ArrayList<>();

        Service.TRANSACTIONS.addTransactions(plan.getYear(), plan.getClosingTransactions());
        done.add(plan.getClosingTransactions().size() + " closing transactions booked into " + plan.getYear());

        Service.CONFIG.initYearData(newYear);
        done.add("year " + newYear + " created");

        Set<String> carriedIds = carriedAccounts(plan, carried);
        List<String[]> openingTransactions = new ArrayList<>();
        int opened = 0;
        for (Candidate candidate : plan.getAccounts()) {
            if (!carriedIds.contains(candidate.getAccount().getFullId())) {
                continue;
            }
            AccountsModel.Account account = candidate.getAccount();
            Service.ACCOUNT.createAccount(newYear, account.getName(), account.getSchemaId(),
                    account.getSemanticId(), account.getMetadata());

            // every asset and liability is opened against the initial account, whether it carries
            // anything or not: the app reads an account's initial value from that transaction, and
            // one that was never opened has no value to read, only an error
            int value = candidate.getClosingValue();
            boolean opensWithABalance = candidate.getType().equals(Constants.AccountType.ASSET)
                    || candidate.getType().equals(Constants.AccountType.LIABILITY);
            if (opensWithABalance) {
                openingTransactions.add(candidate.getType().equals(Constants.AccountType.ASSET)
                        ? new String[]{"0101", String.valueOf(value), account.getFullId(), Constants.Account.INIT_ACC_ID, Constants.Transaction.OPEN_DESCRIPTION}
                        : new String[]{"0101", String.valueOf(value), Constants.Account.INIT_ACC_ID, account.getFullId(), Constants.Transaction.OPEN_DESCRIPTION});
                if (value != 0) {
                    opened++;
                }
            }
        }
        openingTransactions.add(new String[]{"0101", String.valueOf(plan.getProfit()), Constants.Account.INIT_ACC_ID,
                Constants.Account.ACCUMULATED_EARNINGS_ACC_ID, "profit from " + plan.getYear()});
        Service.TRANSACTIONS.addTransactions(newYear, openingTransactions);
        done.add(carriedIds.size() + " accounts carried over, " + opened + " of them opened with a balance");
        done.add("profit of " + plan.getProfit() + " booked onto " + Constants.Account.ACCUMULATED_EARNINGS_ACC_ID);

        List<String> fixed = Service.PROCEDURES.removeBookingsOf(droppedAccounts(plan, carriedIds));
        done.addAll(fixed);

        Service.CONFIG.setActiveYear(newYear);
        done.add("year " + newYear + " is now the active one");

        Initializer.LOG.info("Year " + plan.getYear() + " closed, " + newYear + " opened: " + String.join("; ", done));
        return done;
    }

    /**
     * Everything the new year starts with: what was chosen, what has to come over because it holds
     * a balance, and everything derived from either - a depreciation account goes where its asset
     * goes, without being asked about.
     */
    public Set<String> carriedAccounts(Plan plan, Collection<String> chosen) {
        Set<String> carried = new HashSet<>(chosen);
        for (Candidate candidate : plan.getAccounts()) {
            if (candidate.isRequired()) {
                carried.add(candidate.getAccount().getFullId());
            }
        }
        for (Candidate candidate : plan.getAccounts()) {
            if (candidate.isDerived() && carried.contains(candidate.getOwnerId())) {
                carried.add(candidate.getAccount().getFullId());
            }
        }
        return carried;
    }

    /** The accounts a procedure may no longer book, because the new year does not have them. */
    public Set<String> droppedAccounts(Plan plan, Collection<String> chosen) {
        Set<String> dropped = new HashSet<>(plan.closingValue.keySet());
        dropped.removeAll(carriedAccounts(plan, chosen));
        return dropped;
    }

    /**
     * Reads an id backwards to the account it was made for: the accumulated depreciation and the
     * depreciation of an asset, the consumption of a resource, and the three accounts a long-term
     * financial asset is booked through. Everything else stands on its own and has no owner.
     */
    public String ownerOf(AccountsModel.Account account) {
        String schemaId = account.getSchemaId();
        String semanticId = account.getSemanticId();
        int dash = semanticId.indexOf('-');
        if (dash < 1) {
            return null; // an account of its own has a plain semantic id, a derived one carries both
        }
        String ownerAccountId = semanticId.substring(0, dash);
        String ownerSemanticId = semanticId.substring(dash + 1);

        if (schemaId.startsWith("0" + Constants.Schema.ACCUMULATED_DEP_GROUP_ID)) {
            return "0" + schemaId.charAt(2) + ownerAccountId + "." + ownerSemanticId;
        }
        if (schemaId.startsWith("5" + Constants.Schema.DEPRECIATION_GROUP_ID)) {
            return "0" + schemaId.charAt(2) + ownerAccountId + "." + ownerSemanticId;
        }
        if (schemaId.startsWith("5" + Constants.Schema.CONSUMPTION_GROUP_ID)) {
            return "1" + schemaId.charAt(2) + ownerAccountId + "." + ownerSemanticId;
        }
        if (schemaId.equals(Constants.Schema.FIN_CREATION_FULL_ID)
                || schemaId.equals(Constants.Schema.FIN_EXP_REVALUATION_FULL_ID)
                || schemaId.equals(Constants.Schema.FIN_REV_REVALUATION_FULL_ID)) {
            return Constants.Schema.FIN_ASSET_SCHEMA_PREFIX + ownerAccountId + "." + ownerSemanticId;
        }
        return null;
    }

    private String[] entry(Plan plan, String amount, String debit, String credit) {
        return new String[]{plan.closureDate, amount, debit, credit, Constants.Transaction.CLOSE_DESCRIPTION};
    }

    /** What every account is worth in that year, in one pass over its transactions. */
    private Map<String, Integer> balances(String year) {
        Map<String, Integer> balances = new LinkedHashMap<>();
        for (TransactionsModel.Transaction transaction : Service.TRANSACTIONS.getTransactions(year, null, null)) {
            int amount = Integer.parseInt(transaction.getAmount());
            balances.merge(transaction.getDebit(), amount, Integer::sum);
            balances.merge(transaction.getCredit(), -amount, Integer::sum);
        }
        return balances;
    }

    /**
     * The books close when what the closing account is charged with equals what it is credited with,
     * the profit included. If they do not, something is booked against an account the schema does
     * not know, and closing would quietly lose it.
     */
    private void checkClosing(Plan plan) {
        int debit = 0;
        int credit = 0;
        for (String[] transaction : plan.closingTransactions) {
            int amount = Integer.parseInt(transaction[1]);
            if (transaction[2].equals(Constants.Account.CLOSING_ACC_ID)) {
                debit += amount;
            }
            if (transaction[3].equals(Constants.Account.CLOSING_ACC_ID)) {
                credit += amount;
            }
        }
        if (debit != credit) {
            plan.balanced = false;
            plan.problems.add("The books do not close: " + debit + " charged to the closing account against "
                    + credit + " credited, a difference of " + (debit - credit) + ".");
        }
    }

    /** The profit of the year lands on the accumulated earnings, so that account has to exist. */
    private void checkTheProfitHasSomewhereToGo(String year, Plan plan) {
        if (!Service.ACCOUNT.checkAccountExists(year, Constants.Account.ACCUMULATED_EARNINGS_ACC_ID)) {
            plan.problems.add("There is no accumulated earnings account ("
                    + Constants.Account.ACCUMULATED_EARNINGS_ACC_ID + ") to book the profit onto.");
        }
    }

    /**
     * A year may be closed once all of its transactions are in: within its last few days, or at any
     * time after it has ended.
     */
    private void checkItIsTimeToClose(String year, LocalDate today, Plan plan) {
        if (calendarSetAside()) {
            return;
        }
        int closing;
        try {
            closing = Integer.parseInt(year);
        } catch (NumberFormatException e) {
            return; // a year that is not a number is not on any calendar, so there is nothing to say
        }
        if (today.getYear() > closing) {
            return;
        }
        LocalDate lastDay = LocalDate.of(closing, 12, 31);
        LocalDate from = lastDay.minusDays(DAYS_BEFORE_THE_END - 1L);
        if (today.getYear() < closing || today.isBefore(from)) {
            plan.problems.add(year + " is closed once all of its transactions are entered. Enabled from "
                    + from.getDayOfMonth() + "." + from.getMonthValue() + "." + from.getYear() + ".");
        }
    }

    private boolean calendarSetAside() {
        return Boolean.getBoolean(ANY_TIME) || new File(Initializer.getDataSource() + ANY_TIME_FILE).exists();
    }

    private void checkTheNewYearIsFree(String newYear, Plan plan) {
        if (newYear == null || newYear.trim().isEmpty()) {
            plan.problems.add("No name for the new year.");
            return;
        }
        if (Service.CONFIG.getYears().contains(newYear)) {
            plan.problems.add("The year " + newYear + " already exists.");
        }
    }
}
