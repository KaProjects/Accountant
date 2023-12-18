package org.kaleta.service;

import org.kaleta.entity.Account;
import org.kaleta.entity.Schema;
import org.kaleta.entity.Transaction;
import org.kaleta.model.GroupComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AccountingServiceImpl implements AccountingService
{
    @Autowired
    TransactionService transactionService;
    @Autowired
    SchemaService schemaService;
    @Autowired
    AccountService accountService;

    @Override
    public GroupComponent getGroupComponent(String year, String groupId)
    {
        List<String> accountIdSuffixes = new ArrayList<>();
        for (Schema schemaAccount : schemaService.getSchemaAccountsByGroup(year, groupId)) {
            accountIdSuffixes.add(schemaAccount.getYearId().getId().substring(2,3));
        }
        return getGroupComponent(year, groupId, accountIdSuffixes.toArray(new String[]{}));
    }

    @Override
    public GroupComponent getGroupComponent(String year, String groupId, String... accountIdSuffixes)
    {
        GroupComponent groupComponent = new GroupComponent();
        groupComponent.setSchemaId(groupId);
        groupComponent.setName(schemaService.getGroupName(year, groupId));

        for (String accountIdSuffix : accountIdSuffixes) {
            String accountId = groupId + accountIdSuffix;
            GroupComponent.AccountComponent accountComponent = new GroupComponent.AccountComponent();
            accountComponent.setSchemaId(accountId);
            accountComponent.setName(schemaService.getAccountName(year, accountId));

            for (Account account : accountService.listBySchemaId(year, accountId)){
                if (!groupId.startsWith("5") && !groupId.startsWith("6")) {
                    accountComponent.addInitialValue(transactionService.getInitialValue(account));
                }
                accountComponent.addMonthlyBalance(transactionService.monthlyBalanceByAccount(account));
            }
            groupComponent.getAccounts().add(accountComponent);
        }
        return groupComponent;
    }

    @Override
    public List<Transaction> getSchemaTransactions(String year, String schemaId, String month)
    {
        List<Transaction> transactions = transactionService.getSchemaTransactions(year, schemaId, month);

        // filter correcting transactions between same schema account
        transactions.removeIf(transaction -> transaction.getDebit().substring(0,3).equals(transaction.getCredit().substring(0,3)));

        transactions.forEach(transaction -> {
            if ((transaction.getCredit().startsWith(schemaId) && schemaService.isDebitType(year, schemaId))
                || (transaction.getDebit().startsWith(schemaId) && schemaService.isCreditType(year, schemaId)))
            {
                transaction.setAmount(-transaction.getAmount());
            }
        });

        Map<String, String> accountNames = accountService.getAccountNamesMap(year);
        transactions.forEach(transaction -> {
            transaction.setDebit(accountNames.get(transaction.getDebit()));
            transaction.setCredit(accountNames.get(transaction.getCredit()));
        });

        return transactions;
    }
}
