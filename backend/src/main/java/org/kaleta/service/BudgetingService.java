package org.kaleta.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.kaleta.AccountUtils;
import org.kaleta.Constants;
import org.kaleta.Utils;
import org.kaleta.dto.BudgetDto;
import org.kaleta.dto.BudgetSchemaDto;
import org.kaleta.model.BudgetComponent;
import org.kaleta.model.BudgetingData;
import org.kaleta.persistence.api.BudgetingDao;
import org.kaleta.persistence.entity.Account;
import org.kaleta.persistence.entity.Budgeting;
import org.kaleta.persistence.entity.Transaction;
import org.kaleta.persistence.entity.YearId;
import org.kaleta.rest.error.InvalidDataException;
import org.kaleta.rest.error.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.kaleta.Constants.Budget.FIN_ASSET;
import static org.kaleta.Constants.Budget.VACATION_LABEL;
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
    /** The ids of the totals of the parts of the budget whose transactions can be listed together. */
    private static final Set<String> SUMMED_PART_IDS = Set.of("i", "me");
    /** A part of the budget - income, mandatory expenses, expenses, off-budget - and a number. */
    private static final String ROW_ID = "(i|me|e|of)\\d+(\\.\\d+)*";
    /** An account: its schema id, a dot and its number - and for an asset's, a dash and another. */
    private static final String ACCOUNT = "\\d{3}\\.\\d+(-\\d+)?";
    /** The beginning of an account, and a % for every account it begins. */
    private static final String ACCOUNT_PREFIX = "(\\d{1,3}|\\d{3}\\.(\\d+(-\\d*)?)?)%";

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final SchemaService schemaService;
    private final BudgetingDao budgetingDao;

    @Inject
    public BudgetingService(TransactionService transactionService, AccountService accountService, SchemaService schemaService,
                            BudgetingDao budgetingDao)
    {
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.schemaService = schemaService;
        this.budgetingDao = budgetingDao;
    }

    /**
     * @return budget data for specified year
     */
    public BudgetingData getBudgetData(String year)
    {
        List<Transaction> transactions = transactionService.getBudgetTransactions(year);
        return new BudgetingData(getSchema(year), transactions);
    }

    /**
     * The rows of the specified year's budget as they are stored, for the budget editor, with the
     * fixed rows it lacks as an earlier year has them - each marked as fixed.
     */
    public List<BudgetSchemaDto> getStoredSchema(String year)
    {
        return storedSchema(year).stream()
                .map(row -> BudgetSchemaDto.from(row, FixedRow.of(row).isPresent()))
                .toList();
    }

    /**
     * The stored rows in the order of their ids, number by number - e1.2 before e1.10, which as
     * text would come after it.
     */
    private List<Budgeting> storedSchema(String year)
    {
        List<Budgeting> rows = withFixedRows(year, budgetingDao.getSchema(year));
        rows.sort(Comparator.comparing(row -> row.getYearId().getId(), BudgetingService::compareIds));
        return rows;
    }

    /**
     * The rows every year has from the first that has them on: what was spent on vacations, and
     * what was invested in financial assets. They are told by what they count, never by their
     * names, which are only in the stored budget.
     */
    public enum FixedRow
    {
        /** Every expense labelled as a vacation's. */
        VACATIONS,
        /** Every financial asset, a sub row each. */
        INVESTMENTS;

        static Optional<FixedRow> of(Budgeting row)
        {
            if (row.getYearId().getId().contains(".")) return Optional.empty();
            if (VACATION_LABEL.equals(row.getDescription())) return Optional.of(VACATIONS);
            if (isEveryFinancialAsset(row)) return Optional.of(INVESTMENTS);
            return Optional.empty();
        }
    }

    /**
     * The stored rows of a year, and any fixed row they lack, as the latest earlier year has it -
     * named and counting the same, with nothing planned yet - so that every year after the first
     * that had one has it too. It takes its id from that year, unless the id is taken.
     */
    private List<Budgeting> withFixedRows(String year, List<Budgeting> stored)
    {
        List<Budgeting> rows = new ArrayList<>(stored);
        for (FixedRow fixed : FixedRow.values())
        {
            if (stored.stream().anyMatch(row -> FixedRow.of(row).orElse(null) == fixed)) continue;
            latestEarlier(year, fixed).ifPresent(template -> {
                Budgeting row = row(year, freeId(template.getYearId().getId(), rows), template.getName(), "all=0");
                row.setDebit(template.getDebit());
                row.setCredit(template.getCredit());
                row.setDescription(template.getDescription());
                rows.add(row);
            });
        }
        return rows;
    }

    private Optional<Budgeting> latestEarlier(String year, FixedRow fixed)
    {
        return schemaService.getYears().stream()
                .filter(earlier -> earlier.compareTo(year) < 0)
                .sorted(Comparator.reverseOrder())
                .map(earlier -> budgetingDao.getSchema(earlier).stream()
                        .filter(row -> FixedRow.of(row).orElse(null) == fixed)
                        .findFirst())
                .flatMap(Optional::stream)
                .findFirst();
    }

    /** The given id if no row has it, else the next number of its part after the highest there. */
    private static String freeId(String id, List<Budgeting> rows)
    {
        if (rows.stream().noneMatch(row -> row.getYearId().getId().equals(id))) return id;
        String prefix = id.replaceAll("\\d.*", "");
        int highest = rows.stream().map(row -> row.getYearId().getId())
                .filter(other -> !other.contains(".") && other.matches(prefix + "\\d+"))
                .mapToInt(other -> Integer.parseInt(other.substring(prefix.length())))
                .max().orElse(0);
        return prefix + (highest + 1);
    }

    static int compareIds(String one, String other)
    {
        String[] ones = one.split("(?<=\\D)(?=\\d)|\\.");
        String[] others = other.split("(?<=\\D)(?=\\d)|\\.");
        for (int i = 0; i < Math.min(ones.length, others.length); i++)
        {
            boolean numbers = ones[i].matches("\\d+") && others[i].matches("\\d+");
            int compared = numbers ? Integer.compare(Integer.parseInt(ones[i]), Integer.parseInt(others[i])) : ones[i].compareTo(others[i]);
            if (compared != 0) return compared;
        }
        return Integer.compare(ones.length, others.length);
    }

    /**
     * Replaces the stored rows of the specified year's budget with the given ones, once they are
     * all found to make a budget: each id once, of a part of the budget and numbered - i1, me2.1 -
     * each sub row under a row of its own, accounts given on both sides or on neither, and a
     * planning the budget can read - on a row only, as a sub row has none - no accounts on a row
     * with sub rows, which is made of theirs, and a debit and credit that each name an account of
     * the year, or the beginning of at least one followed by %.
     */
    @Transactional
    public void saveSchema(String year, List<BudgetSchemaDto> rows)
    {
        Set<String> ids = new HashSet<>();
        for (BudgetSchemaDto row : rows)
        {
            String id = row.getId().trim();
            if (!id.matches(ROW_ID)) {
                throw new InvalidDataException("Row '" + id + "' has no valid id: it has to start with i, me, e or of and a number, as i1 or me2.1.");
            }
            if (!ids.add(id)) {
                throw new InvalidDataException("Row '" + id + "' is there more than once.");
            }
            if (id.contains(".") && !isBlank(row.getPlanning())) {
                throw new InvalidDataException("Row '" + id + "' is a sub row, and only a row has a planning.");
            }
            if (isBlank(row.getDebit()) != isBlank(row.getCredit())) {
                throw new InvalidDataException("Row '" + id + "' has to have both its debit and its credit, or neither.");
            }
            try {
                BudgetingData.parsePlanning(isBlank(row.getPlanning()) ? null : row.getPlanning().trim());
            } catch (IllegalArgumentException e) {
                throw new InvalidDataException("Row '" + id + "' has a planning that is neither all=amount nor twelve amounts separated by |.");
            }
        }
        List<String> accountIds = accountService.list(year).stream().map(Account::getFullId).toList();
        for (BudgetSchemaDto row : rows)
        {
            for (String side : List.of("debit", "credit"))
            {
                String pattern = side.equals("debit") ? row.getDebit() : row.getCredit();
                if (isBlank(pattern)) continue;
                String problem = accountPatternProblem(pattern.trim(), accountIds);
                if (problem != null) {
                    throw new InvalidDataException("Row '" + row.getId().trim() + "' has the " + side + " '" + pattern.trim() + "', which " + problem + ".");
                }
            }
        }
        for (String id : ids)
        {
            if (id.contains(".") && !ids.contains(id.substring(0, id.indexOf('.')))) {
                throw new InvalidDataException("Row '" + id + "' is a sub row of '" + id.substring(0, id.indexOf('.')) + "', which is not there.");
            }
        }
        for (BudgetSchemaDto row : rows)
        {
            String id = row.getId().trim();
            boolean hasSubRows = !id.contains(".") && ids.stream().anyMatch(other -> other.startsWith(id + "."));
            if (hasSubRows && !(isBlank(row.getDebit()) && isBlank(row.getCredit()) && isBlank(row.getDescription()))) {
                throw new InvalidDataException("Row '" + id + "' has sub rows, and is made of theirs: it has no debit, credit or description of its own.");
            }
        }

        budgetingDao.replaceSchema(year, rows.stream().map(row -> row.toBudgeting(year)).toList());
    }

    /**
     * What is wrong with a row's debit or credit for the given accounts of its year, or null. It is
     * either an account - its schema id, a dot and its number, and a dash and another number for an
     * account of an asset: 210.0, 549.0-10 - which has to be one of them; or the beginning of one
     * followed by %, meaning every account it begins - 5%, 510%, 511.0-% - of which there has to be
     * at least one.
     */
    static String accountPatternProblem(String pattern, List<String> accountIds)
    {
        if (pattern.matches(ACCOUNT)) {
            return accountIds.contains(pattern) ? null : "is no account of the year";
        }
        if (pattern.matches(ACCOUNT_PREFIX)) {
            String prefix = pattern.substring(0, pattern.length() - 1);
            return accountIds.stream().anyMatch(id -> id.startsWith(prefix)) ? null : "begins no account of the year";
        }
        return "is neither an account, as 210.0 or 549.0-10, nor the beginning of one followed by %, as 510% or 511.0-%";
    }

    private static boolean isBlank(String value)
    {
        return value == null || value.isBlank();
    }

    /**
     * The rows of the budget of the specified year, as stored, but for the rows of every financial
     * asset: a row of the financial assets whose accounts are given by a prefix, as credit 23%, is
     * expanded into a sub row for each asset of the year whose account it covers - named as the
     * account, debit its creation, credit the asset - so that no asset has to be stored, and none is
     * missed. The stored row stays as the group of those sub rows, with its name and planning.
     * Only a main row is expanded, as a sub row cannot have sub rows of its own.
     */
    public List<Budgeting> getSchema(String year)
    {
        List<Budgeting> schema = new ArrayList<>();
        for (Budgeting stored : storedSchema(year))
        {
            if (!isEveryFinancialAsset(stored)) {
                schema.add(stored);
                continue;
            }
            String id = stored.getYearId().getId();
            String assetPrefix = stored.getCredit().substring(0, stored.getCredit().length() - 1);
            schema.add(row(year, id, stored.getName(), stored.getPlanning()));
            accountService.getFinancialAssetAccounts(year).values().stream()
                    .flatMap(List::stream)
                    .filter(asset -> asset.getFullId().startsWith(assetPrefix))
                    .sorted(Comparator.comparing((Account asset) -> asset.getAccountId().getSchemaId())
                            .thenComparing(asset -> Integer.parseInt(asset.getAccountId().getSemanticId())))
                    .forEach(asset -> {
                        Budgeting row = row(year, id + "." + asset.getFullId(), asset.getName(), null);
                        row.setDebit(AccountUtils.getFinCreationAccountId(asset));
                        row.setCredit(asset.getFullId());
                        row.setDescription(FIN_ASSET);
                        schema.add(row);
                    });
        }
        return schema;
    }

    private static boolean isEveryFinancialAsset(Budgeting row)
    {
        return FIN_ASSET.equals(row.getDescription())
                && row.getCredit() != null && row.getCredit().endsWith("%")
                && !row.getYearId().getId().contains(".");
    }

    /** A row not stored: built for the request, never persisted. */
    private static Budgeting row(String year, String id, String name, String planning)
    {
        YearId yearId = new YearId();
        yearId.setYear(year);
        yearId.setId(id);
        Budgeting row = new Budgeting();
        row.setYearId(yearId);
        row.setName(name);
        row.setPlanning(planning);
        return row;
    }

    /**
     * @return list of transactions for specified ID, year and month - of a row, its sub rows, or for
     * the total of the income or the mandatory expenses, every row of that part
     */
    public List<Transaction> getBudgetTransactions(String year, String budgetId, String month)
    {
        List<Budgeting> rows = getSchema(year);
        boolean total = SUMMED_PART_IDS.contains(budgetId);
        if (!total && rows.stream().noneMatch(row -> row.getYearId().getId().equals(budgetId))) {
            throw new ResourceNotFoundException("The budget of " + year + " has no row '" + budgetId + "'.");
        }

        // what the row counts itself; or, for a row made of sub rows, what they count; or, for the
        // total of a part, what every row of the part counts - each transaction once
        List<Budgeting> covered = rows.stream().filter(row -> {
            String id = row.getYearId().getId();
            return total ? id.matches(budgetId + "\\d.*") : id.equals(budgetId) || id.startsWith(budgetId + ".");
        }).toList();
        List<Budgeting> counting = covered.stream()
                .filter(row -> covered.stream().noneMatch(other -> other.getYearId().getId().startsWith(row.getYearId().getId() + ".")))
                .filter(row -> !total || row.getDebit() != null || row.getCredit() != null)
                .toList();
        List<Transaction> transactions = counting.stream()
                .flatMap(row -> transactionsOf(year, row, month).stream())
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));

        Map<String, String> accountNames = accountService.getAccountNamesMap(year);
        transactions.forEach(transaction -> {
            transaction.setDebit(accountNames.get(transaction.getDebit()));
            transaction.setCredit(accountNames.get(transaction.getCredit()));
        });

        return transactions;
    }

    /** The transactions a row with accounts of its own counts in the given month, by account id. */
    private List<Transaction> transactionsOf(String year, Budgeting schema, String month)
    {
        String budgetId = schema.getYearId().getId();
        if (schema.getDebit() == null && schema.getCredit() == null){
            throw new IllegalArgumentException("Budget schema specified by budgetId='" + budgetId + "' doesn't have debit/credit accounts specified.");
        }

        List<Transaction> transactions = new ArrayList<>();
        if (schema.getDebit().equals(schema.getCredit())) {
            transactions.addAll(transactionService.getTransactionsMatching(year, schema.getDebit(), "", schema.getDescription()));
            List<Transaction> creditTransactions = transactionService.getTransactionsMatching(year, "", schema.getCredit(), schema.getDescription());
            creditTransactions.forEach(transaction -> transaction.setAmount(-transaction.getAmount()));
            transactions.addAll(creditTransactions);
        } else if (FIN_ASSET.equals(schema.getDescription())) {
            // as the row counts them: see BudgetingData
            String assetId = schema.getCredit();
            String gainsId = AccountUtils.finAccountId(assetId, Constants.Schema.FIN_REV_REVALUATION_ID);
            String lossesId = AccountUtils.finAccountId(assetId, Constants.Schema.FIN_EXP_REVALUATION_ID);
            transactions.addAll(transactionService.getTransactionsMatching(year, schema.getDebit(), "", ""));
            transactions.addAll(transactionService.getTransactionsMatching(year, assetId, "", "").stream()
                    .filter(transaction -> !transaction.getCredit().equals(gainsId)).toList());
            List<Transaction> creditTransactions = transactionService.getTransactionsMatching(year, "", assetId, "").stream()
                    .filter(transaction -> !transaction.getDebit().equals(lossesId)).toList();
            creditTransactions.forEach(transaction -> transaction.setAmount(-transaction.getAmount()));
            transactions.addAll(creditTransactions);
            transactions.removeIf(transaction -> transaction.getDebit().startsWith("7") || transaction.getCredit().startsWith("7"));
        } else {
            transactions.addAll(transactionService.getTransactionsMatching(year, schema.getDebit(), schema.getCredit(), schema.getDescription()));
        }

        transactions.removeIf(transaction -> !transaction.getDate().endsWith(month.length() == 1 ? "0" + month : month));

        // filter correcting transactions between same schema account
        transactions.removeIf(transaction -> transaction.getDebit().substring(0,3).equals(transaction.getCredit().substring(0,3)));

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
