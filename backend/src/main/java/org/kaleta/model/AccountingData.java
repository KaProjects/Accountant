package org.kaleta.model;

import org.kaleta.Constants;
import org.kaleta.persistence.entity.Account;
import org.kaleta.persistence.entity.Transaction;

import java.util.List;
import java.util.stream.Collectors;

public class AccountingData
{
    private final List<Transaction> transactions;
    private final List<Account> accounts;
    private final SchemaClass schemaClass;

    public AccountingData(List<Transaction> transactions, List<Account> accounts, SchemaClass schemaClass){
        this.transactions = transactions;
        this.accounts = accounts;
        this.schemaClass = schemaClass;
    }

    public GroupComponent getGroupComponent(String groupId, String... accountIdSuffixes){
        GroupComponent groupComponent = new GroupComponent();
        groupComponent.setSchemaId(groupId);

        SchemaClass.Group schemaGroup = schemaClass.getGroup(groupId);
        if (schemaGroup == null) return groupComponent; // a year might not have this group at all

        groupComponent.setName(schemaGroup.getName());

        for (String schemaAccountSuffix : accountIdSuffixes)
        {
            SchemaClass.Group.Account schemaAccount = schemaGroup.getAccountBySuffix(schemaAccountSuffix);

            if (schemaAccount == null) continue; // older years might not have some new schema accounts

            GroupComponent.AccountComponent accountComponent = new GroupComponent.AccountComponent();
            accountComponent.setSchemaId(schemaAccount.getId());
            accountComponent.setName(schemaAccount.getName());

            for (Account account : accounts.stream()
                    .filter(account -> account.getAccountId().getSchemaId().equals(schemaAccount.getId()))
                    .collect(Collectors.toList()))
            {
                if (hasInitialValue(schemaAccount)) {
                    accountComponent.addInitialValue(getInitialValue(account, isAsset(schemaAccount)));
                }
                accountComponent.addMonthlyBalance(getMonthlyBalance(account, isDebit(schemaAccount), hasInitialValue(schemaAccount)));
            }
            groupComponent.getAccounts().add(accountComponent);
        }

        return groupComponent;
    }
    public GroupComponent getGroupComponent(String groupId){
        SchemaClass.Group schemaGroup = schemaClass.getGroup(groupId);
        if (schemaGroup == null) return getGroupComponent(groupId, new String[]{});
        return getGroupComponent(groupId, schemaGroup.getAccountSuffixes().toArray(new String[]{}));
    }

    public ClassComponent getClassComponent(String... groupIdSuffixes)
    {
        ClassComponent classComponent = new ClassComponent();
        classComponent.setSchemaId(schemaClass.getId());
        classComponent.setName(schemaClass.getName());

        // A year whose schema has not been synced has no classes at all, so there is nothing to
        // total up. That is an empty statement rather than a fault: an empty database is what
        // production looks like before its first sync, and asking for a statement then used to
        // fail with a 500 and a stack trace instead of showing an empty one.
        if (schemaClass.getId() == null) return classComponent;

        for (String groupIdSuffix : groupIdSuffixes)
        {
            GroupComponent groupComponent = getGroupComponent(schemaClass.getId() + groupIdSuffix);
            if (schemaClass.getId().equals("0") && groupIdSuffix.equals("9")) groupComponent.inverted();
            classComponent.getGroups().add(groupComponent);
        }
        return classComponent;
    }

    public ClassComponent getClassComponent()
    {
        return getClassComponent(schemaClass.getGroupSuffixes().toArray(new String[]{}));
    }

    private Integer getInitialValue(Account account, boolean isAsset){
        // An account with no initiation opens at nothing. That is ordinary: an account declared in
        // the chart of accounts but not used that year has none, and so does one created part way
        // through the year, because there was nothing to carry in. This used to be treated as a
        // fault, which failed the whole statement with a 500 - the 2019 balance sheet could not be
        // shown at all because one account had been declared and never posted to.
        //
        // An account's initial value is otherwise the sum of its initiation transactions. Usually
        // there is exactly one, but accumulated earnings are initiated twice (the opening
        // balance and the previous year's profit), and the desktop app also happens to
        // write duplicate zero-amount initiations for some accounts.
        //
        // Both directions count. An account that opens below zero is written the other way round
        // with a positive amount - 2017 opens a bank account with "700.0 -> 210.0 zostatok" - and
        // reading only the usual direction lost it, so the year no longer added up to its closing.
        int initialValue = 0;
        for (Transaction transaction : transactions)
        {
            if (transaction.getDebit().equals(account.getFullId()) && transaction.getCredit().equals(Constants.Account.INIT_ACC_ID)) {
                initialValue += isAsset ? transaction.getAmount() : -transaction.getAmount();
            }
            if (transaction.getDebit().equals(Constants.Account.INIT_ACC_ID) && transaction.getCredit().equals(account.getFullId())) {
                initialValue += isAsset ? -transaction.getAmount() : transaction.getAmount();
            }
        }
        return initialValue;
    }

    /**
     * How the account moved in each month: every transaction it takes part in, except the ones
     * the statement shows elsewhere or not at all.
     * <ul>
     * <li>The year-end closing to 701.0 and 710.0 is not a movement, it is the year's result.</li>
     * <li>An account with an opening balance has its 700.0 transactions in that opening balance.
     * An expense or revenue account has none, so a correction booked against 700.0 is an
     * ordinary movement in the month it is dated - the desktop app closes it into the year the
     * same way.</li>
     * <li>A transaction from an account to itself moves nothing. It used to be counted once, on
     * the debit side, so a reposting such as "626.0 -> 626.0 preuctovanie" shifted the year.</li>
     * </ul>
     */
    private Integer[] getMonthlyBalance(Account account, boolean isDebit, boolean hasInitialValue){
        Integer[] monthlySums = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (Transaction transaction : transactions.stream()
                .filter(transaction -> transaction.getDebit().equals(account.getFullId()) || transaction.getCredit().equals(account.getFullId()))
                .filter(transaction -> !transaction.getDebit().equals(transaction.getCredit()))
                .filter(transaction -> !isClosing(transaction))
                .filter(transaction -> !(hasInitialValue && isInitiation(transaction)))
                .collect(Collectors.toList()))
        {
            int month = Integer.parseInt(transaction.getDate().substring(2, 4));

            if (transaction.getDebit().equals(account.getFullId())) {
                monthlySums[month - 1] +=  isDebit ? transaction.getAmount() : -transaction.getAmount();
            } else {
                monthlySums[month - 1] +=  isDebit ? -transaction.getAmount() : transaction.getAmount();
            }
        }
        return monthlySums;
    }

    private static boolean isClosing(Transaction transaction){
        return List.of(Constants.Account.CLOSING_ACC_ID, Constants.Account.PROFIT_ACC_ID).contains(transaction.getDebit())
                || List.of(Constants.Account.CLOSING_ACC_ID, Constants.Account.PROFIT_ACC_ID).contains(transaction.getCredit());
    }

    private static boolean isInitiation(Transaction transaction){
        return transaction.getDebit().equals(Constants.Account.INIT_ACC_ID) || transaction.getCredit().equals(Constants.Account.INIT_ACC_ID);
    }

    private boolean hasInitialValue(SchemaClass.Group.Account schemaAccount){
        return schemaAccount.getType().equals(Constants.AccountType.A) || schemaAccount.getType().equals(Constants.AccountType.L);
    }

    private boolean isAsset(SchemaClass.Group.Account schemaAccount){
        return schemaAccount.getType().equals(Constants.AccountType.A);
    }

    private boolean isDebit(SchemaClass.Group.Account schemaAccount){
        return schemaAccount.getType().equals(Constants.AccountType.A) || schemaAccount.getType().equals(Constants.AccountType.E);
    }
}
