package org.kaleta.model;

import org.kaleta.Constants;
import org.kaleta.entity.Account;
import org.kaleta.entity.Transaction;

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
                accountComponent.addMonthlyBalance(getMonthlyBalance(account, isDebit(schemaAccount)));
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
        List<Transaction> initTransactions = transactions.stream()
                .filter(transaction ->
                        isAsset ? transaction.getDebit().equals(account.getFullId()) && transaction.getCredit().equals(Constants.Account.INIT_ACC_ID)
                                : transaction.getDebit().equals(Constants.Account.INIT_ACC_ID) && transaction.getCredit().equals(account.getFullId())).collect(Collectors.toList());

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
        return initTransactions.stream().mapToInt(Transaction::getAmount).sum();
    }

    private Integer[] getMonthlyBalance(Account account, boolean isDebit){
        Integer[] monthlySums = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (Transaction transaction : transactions.stream()
                .filter(transaction -> transaction.getDebit().equals(account.getFullId()) || transaction.getCredit().equals(account.getFullId()))
                .filter(transaction -> !transaction.getDebit().startsWith("7") && !transaction.getCredit().startsWith("7"))
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
