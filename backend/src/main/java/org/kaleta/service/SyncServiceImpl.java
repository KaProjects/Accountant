package org.kaleta.service;


import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.kaleta.dao.AccountDao;
import org.kaleta.dao.SchemaDao;
import org.kaleta.dao.TransactionDao;
import org.kaleta.entity.Account;
import org.kaleta.entity.Transaction;
import org.kaleta.entity.xml.Accounts;
import org.kaleta.entity.xml.Config;
import org.kaleta.entity.xml.Schema;
import org.kaleta.entity.xml.Transactions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.kaleta.Utils.inputStreamToString;

@Service
public class SyncServiceImpl implements SyncService
{
    private final TransactionDao transactionDao;
    private final SchemaDao schemaDao;
    private final AccountDao accountDao;

    @Autowired
    public SyncServiceImpl(TransactionDao transactionDao, SchemaDao schemaDao, AccountDao accountDao)
    {
        this.transactionDao = transactionDao;
        this.schemaDao = schemaDao;
        this.accountDao = accountDao;
    }

    @Override
    @Transactional
    public String sync(String dataSource) throws IOException
    {
        XmlMapper xmlMapper = new XmlMapper();
        StringBuilder sb = new StringBuilder().append("from: " + dataSource + "\n");

        String transactionsXml = inputStreamToString(new FileInputStream(dataSource + "/transactions.xml"));
        Transactions transactions = xmlMapper.readValue(transactionsXml, Transactions.class);
        transactionDao.syncTransactions(transactions);
        sb.append("year " + transactions.getYear() + " transactions synced: " + transactions.getTransaction().size() + "\n");

        String schemaXml = inputStreamToString(new FileInputStream(dataSource + "/schema.xml"));
        Schema schema = xmlMapper.readValue(schemaXml, Schema.class);
        schemaDao.syncSchema(schema);
        sb.append("year " + schema.getYear() + " schema classes synced: " + schema.getClazz().size() + "\n");

        String accountsXml = inputStreamToString(new FileInputStream(dataSource + "/accounts.xml"));
        Accounts accounts = xmlMapper.readValue(accountsXml, Accounts.class);
        accountDao.syncAccounts(accounts);
        sb.append("year " + accounts.getYear() + " accounts synced: " + accounts.getAccount().size() + "\n");

        return sb.toString();
    }

    @Override
    public List<String> getYears(String dataSource) throws IOException
    {
        XmlMapper xmlMapper = new XmlMapper();
        List<String> years = new ArrayList<>();

        String configXml = inputStreamToString(new FileInputStream(dataSource + "config.xml"));
        Config config = xmlMapper.readValue(configXml, Config.class);
        for (Config.Years.Year year : config.getYears().getYear()){
            years.add(year.getName());
        }
        return years;
    }

    @Override
    public boolean isActive(String dataSource, String year) throws IOException
    {
        XmlMapper xmlMapper = new XmlMapper();
        String configXml = inputStreamToString(new FileInputStream(dataSource + "config.xml"));
        Config config = xmlMapper.readValue(configXml, Config.class);
        return config.getYears().getActive().equals(year);
    }

    @Override
    public String validate(String year, boolean isActive)
    {
        List<org.kaleta.entity.Schema> schemas = schemaDao.list(year);
        List<Account> accounts = accountDao.list(year);
        List<Transaction> transactions = transactionDao.list(year);

        // * 1. validate transactions (date, accounts exist)
        for (Transaction transaction : transactions)
        {
            String invalidYear = validateDate(transaction);
            if (invalidYear != null) return invalidYear;

            String invalidDebit = "year " + transaction.getYear() + " transaction " + transaction.getId() + ": debit account not found";
            String invalidCredit = "year " + transaction.getYear() + " transaction " + transaction.getId() + ": credit account not found";
            for (Account account : accounts)
            {
                if (account.getFullId().equals(transaction.getDebit())) invalidDebit = null;
                if (account.getFullId().equals(transaction.getCredit())) invalidCredit = null;
            }
            if (invalidDebit != null) return invalidDebit;
            if (invalidCredit != null) return invalidCredit;
        }

        // * 2. validate accounts (schema exists)
        for (Account account : accounts)
        {
            String invalidSchema = "year " + account.getAccountId().getYear() + " account " + account.getFullId() + ": schema not found";
            for (org.kaleta.entity.Schema schema : schemas)
            {
                if (schema.getYearId().getId().equals(account.getAccountId().getSchemaId())) invalidSchema = null;
            }
            if (invalidSchema != null) return invalidSchema;
        }

        // * 3. validate schema (classes and groups exist for account, type set for accounts)
        List<String> classIds = new ArrayList<>();
        List<String> groupIds = new ArrayList<>();
        List<String> accountIds = new ArrayList<>();
        Map<String, List<String>> accountIdByType = new HashMap<>();
        for (org.kaleta.entity.Schema schema : schemas)
        {
            if (schema.getYearId().getId().length() == 1) {
                if (!schema.getType().isEmpty()) return "year " + schema.getYearId().getYear() + " schema " + schema.getYearId().getId() + ": shouldn't have type assigned";
                classIds.add(schema.getYearId().getId());
            }
            if (schema.getYearId().getId().length() == 2) {
                if (!schema.getType().isEmpty()) return "year " + schema.getYearId().getYear() + " schema " + schema.getYearId().getId() + ": shouldn't have type assigned";
                groupIds.add(schema.getYearId().getId());
            }
            if (schema.getYearId().getId().length() == 3) {
                if (schema.getType().isEmpty()) return "year " + schema.getYearId().getYear() + " schema " + schema.getYearId().getId() + ": should have type assigned";
                if (!accountIdByType.containsKey(schema.getType())) accountIdByType.put(schema.getType(), new ArrayList<>());
                accountIdByType.get(schema.getType()).add(schema.getYearId().getId());
                accountIds.add(schema.getYearId().getId());
            }
        }
        for (String accountId : accountIds)
        {
            if (!classIds.contains(accountId.substring(0,1))) return "year " + year + " schema " + accountId + ": couldn't find schema class";
            if (!groupIds.contains(accountId.substring(0,2))) return "year " + year + " schema " + accountId + ": couldn't find schema group";
        }

        // * 4. for inactive years: for every account (debit sum == credit sum)
        if (!isActive) {
            for (Account account : accounts)
            {
                Integer debitSum = 0;
                Integer creditSum = 0;
                for (Transaction transaction: transactions)
                {
                    if (transaction.getDebit().equals(account.getFullId())) debitSum += transaction.getAmount();
                    if (transaction.getCredit().equals(account.getFullId())) creditSum += transaction.getAmount();
                }
                if (!debitSum.equals(creditSum)) return "year " + account.getAccountId().getYear() + " account " + account.getFullId() + ": debit='" + debitSum + "' != credit='" + creditSum + "'";
            }
        }
        else // * 5. active year: assets balance = liabilities balance + (revenues balance - expenses balance)
        {
            Integer assetsSum = 0;
            Integer liabilitiesSum = 0;
            Integer expensesSum = 0;
            Integer revenuesSum = 0;
            for (Transaction transaction : transactions)
            {
                if (accountIdByType.get("A").contains(transaction.getDebit().substring(0,3))) assetsSum += transaction.getAmount();
                if (accountIdByType.get("A").contains(transaction.getCredit().substring(0,3))) assetsSum -= transaction.getAmount();
                if (accountIdByType.get("L").contains(transaction.getDebit().substring(0,3))) liabilitiesSum -= transaction.getAmount();
                if (accountIdByType.get("L").contains(transaction.getCredit().substring(0,3))) liabilitiesSum += transaction.getAmount();
                if (accountIdByType.get("E").contains(transaction.getDebit().substring(0,3))) expensesSum += transaction.getAmount();
                if (accountIdByType.get("E").contains(transaction.getCredit().substring(0,3))) expensesSum -= transaction.getAmount();
                if (accountIdByType.get("R").contains(transaction.getDebit().substring(0,3))) revenuesSum -= transaction.getAmount();
                if (accountIdByType.get("R").contains(transaction.getCredit().substring(0,3))) revenuesSum += transaction.getAmount();
            }
            if (!assetsSum.equals(liabilitiesSum + revenuesSum - expensesSum))
                return "year " + year + ": assets='" + assetsSum + "' != liabilities='" + liabilitiesSum + "' + revenues='" + revenuesSum + "' - expenses='" + expensesSum + "'";
        }

        return "year " + year + " data valid";
    }

    private String validateDate(Transaction transaction)
    {
        if (transaction.getDate().length() != 4) {
            return "year " + transaction.getYear() + " transaction " + transaction.getId() + ": invalid length";
        }
        Integer day;
        Integer month;
        try {
            day = Integer.valueOf(transaction.getDate().substring(0,2));
            month = Integer.valueOf(transaction.getDate().substring(2,4));
        } catch (NumberFormatException  e) {
            return "year " + transaction.getYear() + " transaction " + transaction.getId() + ": NumberFormatException";
        }
        if (day < 1 || day > 31){
            return "year " + transaction.getYear() + " transaction " + transaction.getId() + ": invalid day number";
        }
        if (month < 1 || month > 12){
            return "year " + transaction.getYear() + " transaction " + transaction.getId() + ": invalid month number";
        }
        return null;
    }
}
