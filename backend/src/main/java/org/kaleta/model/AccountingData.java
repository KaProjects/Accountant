package org.kaleta.model;

import org.kaleta.Constants;
import org.kaleta.entity.Account;
import org.kaleta.entity.Transaction;

import java.util.List;
import java.util.stream.Collectors;

public class AccountingData
{
    private List<Transaction> transactions;
    private List<Account> accounts;
    private SchemaClass schemaClass;

    public AccountingData(List<Transaction> transactions, List<Account> accounts, SchemaClass schemaClass){
        this.transactions = transactions;
        this.accounts = accounts;
        this.schemaClass = schemaClass;
    }

    public GroupComponent getGroupComponent(String year, String groupId, String... accountIdSuffixes){
        GroupComponent groupComponent = new GroupComponent();
        groupComponent.setSchemaId(groupId);
        groupComponent.setName(schemaClass.getGroup(groupId).getName());

        for (String schemaAccountSuffix : accountIdSuffixes)
        {
            SchemaClass.Group.Account schemaAccount = schemaClass.getGroup(groupId).getAccount(schemaAccountSuffix);

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
    public GroupComponent getGroupComponent(String year, String groupId){
        return getGroupComponent(year, groupId, schemaClass.getGroup(groupId).getAccountSuffixes().toArray(new String[]{}));
    }

    private Integer getInitialValue(Account account, boolean isAsset){
        return transactions.stream()
                .filter(transaction ->
                        isAsset ? transaction.getDebit().equals(account.getFullId()) && transaction.getCredit().equals(Constants.Account.INIT_ACC_ID)
                                : transaction.getDebit().equals(Constants.Account.INIT_ACC_ID) && transaction.getCredit().equals(account.getFullId())
                )
                .findFirst().get().getAmount();
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
        return schemaAccount.getType().equals("A") || schemaAccount.getType().equals("L");
    }

    private boolean isAsset(SchemaClass.Group.Account schemaAccount){
        return schemaAccount.getType().equals("A");
    }

    private boolean isDebit(SchemaClass.Group.Account schemaAccount){
        return schemaAccount.getType().equals("A") || schemaAccount.getType().equals("E");
    }


}
