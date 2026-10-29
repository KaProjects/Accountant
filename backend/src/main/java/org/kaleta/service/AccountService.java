package org.kaleta.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.kaleta.AccountUtils;
import org.kaleta.Constants;
import org.kaleta.dto.YearAccountDto;
import org.kaleta.dto.YearAccountOverviewDto;
import org.kaleta.dto.YearAccountTransactionDto;
import org.kaleta.model.SchemaClass;
import org.kaleta.persistence.api.AccountDao;
import org.kaleta.persistence.entity.Account;
import org.kaleta.persistence.entity.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@ApplicationScoped
public class AccountService
{
    private final AccountDao accountDao;
    private final SchemaService schemaService;
    private final TransactionService transactionService;

    @Inject
    public AccountService(AccountDao accountDao, SchemaService schemaService, TransactionService transactionService)
    {
        this.accountDao = accountDao;
        this.schemaService = schemaService;
        this.transactionService = transactionService;
    }

    /**
     * @return map of names of all accounts with full account IDs as keys
     *
     * Note: schema account names are used instead of 'general' or 'general of...' account names
     */
    public Map<String, String> getAccountNamesMap(String year)
    {
        Map<String, String> schemaNames = schemaService.getSchemaNames(year);
        Map<String, String> map = new HashMap<>();
        for (Account account : accountDao.list(year)){
            String fullId = account.getAccountId().getSchemaId() + "." + account.getAccountId().getSemanticId();
            String name = account.getName().contains("general")
                    ? schemaNames.get(account.getAccountId().getSchemaId())
                    : account.getName();

            map.put(fullId, name);
        }
        return map;
    }

    /**
     * @return list of all accounts for specified year
     */
    public List<Account> list(String year)
    {
        return accountDao.list(year);
    }

    /**
     * @return list of accounts matching schema prefix and year
     */
    public List<Account> listBySchema(String year, String schemaPrefix)
    {
        return accountDao.list(year, schemaPrefix);
    }

    /**
     * @return list of accounts matching metadata for specified year
     */
    public List<Account> listMatchingMetadata(String year, String metadata)
    {
        return accountDao.listByMetadata(year, metadata);
    }

    /**
     * @return map of financial asset accounts grouped by schema name for specified year
     */
    public Map<String, List<Account>> getFinancialAssetAccounts(String year)
    {
        List<Account> finAssetAccounts = accountDao.list(year, Constants.Schema.FIN_GROUP_ID);
        Map<String, List<Account>> map = new TreeMap<>();
        for (Account account : finAssetAccounts)
        {
            String schemaId = account.getAccountId().getSchemaId();
            if (!map.containsKey(schemaId)) map.put(schemaId, new ArrayList<>());
            map.get(schemaId).add(account);
        }
        return map;
    }

    /**
     * @return all accounts of the specified year, each with the names of the schema class, group
     * and account it belongs to
     */
    public List<YearAccountDto> getYearAccounts(String year)
    {
        List<YearAccountDto> accounts = YearAccountDto.from(list(year));
        Map<String, SchemaClass> schema = schemaService.getSchema(year);
        accounts.forEach(account -> {
            String schemaId = account.getSchemaId();
            SchemaClass clazz = schema.get(schemaId.substring(0,1));
            account.setClazz(clazz.getName());
            SchemaClass.Group group = clazz.getGroup(schemaId.substring(0,2));
            account.setGroup(group.getName());
            account.setAccount(group.getAccount(schemaId).getName());
        });
        return accounts;
    }

    /**
     * @return the accounts of the specified schema account and year, each with its initial value,
     * turnover and balance
     */
    public List<YearAccountOverviewDto> getAccountsOverview(String year, String schemaId)
    {
        Constants.AccountType accountType = schemaService.getAccountType(year, schemaId);
        List<Account> accounts = listBySchema(year, schemaId);
        List<Transaction> transactions = transactionService.getTransactionsMatching(year, schemaId);
        List<YearAccountOverviewDto> dtoList = new ArrayList<>();
        for (Account account : accounts)
        {
            YearAccountOverviewDto dto = new YearAccountOverviewDto();
            dto.setId(account.getFullId());
            dto.setName(account.getName());
            Integer debit = 0;
            Integer credit = 0;
            for (Transaction transaction : transactions)
            {
                if (transaction.getDebit().equals(account.getFullId()) && transaction.getCredit().equals(Constants.Account.INIT_ACC_ID))
                    dto.setInitial(transaction.getAmount());
                if (transaction.getCredit().equals(account.getFullId()) && transaction.getDebit().equals(Constants.Account.INIT_ACC_ID))
                    dto.setInitial(dto.getInitial() + transaction.getAmount());
                if (transaction.getDebit().equals(account.getFullId()) && !transaction.getCredit().equals(Constants.Account.CLOSING_ACC_ID))
                    debit += transaction.getAmount();
                if (transaction.getCredit().equals(account.getFullId()) && !transaction.getDebit().equals(Constants.Account.CLOSING_ACC_ID))
                    credit += transaction.getAmount();
            }
            if (accountType == Constants.AccountType.X) {
                // Off-balance accounts are neither debit nor credit, so neither a
                // turnover nor a balance can be stated for them. They are reported
                // as absent rather than as a misleading zero.
                dto.setTurnover(null);
                dto.setBalance(null);
            } else if (AccountUtils.isDebit(accountType)) {
                dto.setTurnover(debit);
                dto.setBalance(debit - credit);
            } else {
                dto.setTurnover(credit);
                dto.setBalance(credit - debit);
            }
            dtoList.add(dto);
        }
        return dtoList;
    }

    /**
     * @return the transactions of the specified account and year, each as a debit or credit of it
     * paired with the account on its other side, sorted
     */
    public List<YearAccountTransactionDto> getAccountTransactions(String year, String accountId)
    {
        List<YearAccountTransactionDto> dtoList = new ArrayList<>();
        Map<String, String> accountNames = getAccountNamesMap(year);
        for (Transaction transaction : transactionService.getTransactionsMatching(year, accountId))
        {
            YearAccountTransactionDto dto = new YearAccountTransactionDto();
            dto.setDate(transaction.getDate());
            if (transaction.getDebit().equals(accountId)){
                dto.setDebit(String.valueOf(transaction.getAmount()));
                dto.setPair(transaction.getCredit() + " " + accountNames.get(transaction.getCredit()));
            }
            if (transaction.getCredit().equals(accountId)){
                dto.setCredit(String.valueOf(transaction.getAmount()));
                dto.setPair(transaction.getDebit() + " " + accountNames.get(transaction.getDebit()));
            }
            dto.setDescription(transaction.getDescription());

            dtoList.add(dto);
        }
        return dtoList.stream().sorted().collect(Collectors.toList());
    }
}
