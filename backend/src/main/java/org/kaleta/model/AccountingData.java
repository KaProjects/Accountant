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
        groupComponent.setName(schemaClass.getGroup(groupId).getName());

        for (String schemaAccountSuffix : accountIdSuffixes)
        {
            SchemaClass.Group.Account schemaAccount = schemaClass.getGroup(groupId).getAccountBySuffix(schemaAccountSuffix);

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
        return getGroupComponent(groupId, schemaClass.getGroup(groupId).getAccountSuffixes().toArray(new String[]{}));
    }

    public ClassComponent getClassComponent(String... groupIdSuffixes)
    {
        ClassComponent classComponent = new ClassComponent();
        classComponent.setSchemaId(schemaClass.getId());
        classComponent.setName(schemaClass.getName());
        classComponent.setSchemaId(schemaClass.getId());

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

        if (initTransactions.size() == 1){
            return initTransactions.get(0).getAmount();
        } else {
            if (account.getFullId().equals(Constants.Account.ACCUMULATED_EARNINGS_ACC_ID) && initTransactions.size() == 2)
            {
                return initTransactions.get(0).getAmount() + initTransactions.get(1).getAmount();
            }
            throw new IllegalStateException("Illegal number of initial transactions '" + initTransactions.size() + "' for account '" + account.getFullId() + "'");
        }
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
