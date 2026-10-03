package org.kaleta.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.kaleta.datasource.Accounts;
import org.kaleta.datasource.Config;
import org.kaleta.datasource.Schema;
import org.kaleta.datasource.Transactions;
import org.kaleta.persistence.api.AccountDao;
import org.kaleta.persistence.api.SchemaDao;
import org.kaleta.persistence.api.TransactionDao;
import org.kaleta.persistence.entity.Account;
import org.kaleta.persistence.entity.Transaction;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.kaleta.Utils.inputStreamToString;

@ApplicationScoped
public class SyncService
{
    private final TransactionDao transactionDao;
    private final SchemaDao schemaDao;
    private final AccountDao accountDao;

    @Inject
    public SyncService(TransactionDao transactionDao, SchemaDao schemaDao, AccountDao accountDao)
    {
        this.transactionDao = transactionDao;
        this.schemaDao = schemaDao;
        this.accountDao = accountDao;
    }

    /**
     * Syncs the data of a single year from the desktop app datasource to the backend database.
     * <p>
     * Transactions and accounts are read from the year's own sub-directory, while the schema is
     * a single file shared by all years, kept in the root of the datasource.
     *
     * @param dataSource full path to the data directory that contains config.xml and schema.xml
     * @param year the year to sync, also the name of its sub-directory
     * @return result log of the sync
     * @throws IOException if a data file I/O error occurs
     */
    @Transactional
    public String sync(String dataSource, String year) throws IOException
    {
        XmlMapper xmlMapper = new XmlMapper();
        String yearSource = dataSource + year;
        StringBuilder sb = new StringBuilder().append("from: " + yearSource + "\n");

        String transactionsXml = inputStreamToString(new FileInputStream(yearSource + "/transactions.xml"));
        Transactions transactions = xmlMapper.readValue(transactionsXml, Transactions.class);
        transactionDao.syncTransactions(transactions);
        sb.append("year " + transactions.getYear() + " transactions synced: " + transactions.getTransaction().size() + "\n");

        // The schema is shared by all years and lives in the root of the datasource,
        // but it is still stored per year so that every year can be queried on its own.
        String schemaXml = inputStreamToString(new FileInputStream(dataSource + "schema.xml"));
        Schema schema = xmlMapper.readValue(schemaXml, Schema.class);
        schemaDao.syncSchema(year, schema);
        sb.append("year " + year + " schema classes synced: " + schema.getClazz().size() + "\n");

        String accountsXml = inputStreamToString(new FileInputStream(yearSource + "/accounts.xml"));
        Accounts accounts = xmlMapper.readValue(accountsXml, Accounts.class);
        accountDao.syncAccounts(accounts);
        sb.append("year " + accounts.getYear() + " accounts synced: " + accounts.getAccount().size() + "\n");

        return sb.toString();
    }

    /**
     * @param dataSource full path to the data directory that contains config.xml file
     * @return list of years from config file
     * @throws IOException if a data file I/O error occurs
     */
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

    /**
     *
     * @param dataSource full path to the data directory that contains config.xml file
     * @param year a year to check
     * @return true if the year is active, false otherwise
     * @throws IOException if a data file I/O error occurs
     */
    public boolean isActive(String dataSource, String year) throws IOException
    {
        XmlMapper xmlMapper = new XmlMapper();
        String configXml = inputStreamToString(new FileInputStream(dataSource + "config.xml"));
        Config config = xmlMapper.readValue(configXml, Config.class);
        return config.getYears().getActive().equals(year);
    }

    /**
     * Validates data for specified year.
     * 1. validate transactions (date, accounts exist)
     * 2. validate accounts (schema exists)
     * 3. validate schema (classes and groups exist for account, type set for accounts)
     * 4. for inactive years: for every account (debit sum == credit sum)
     * 5. active year: assets balance = liabilities balance + (revenues balance - expenses balance)
     *
     * @return message from validator
     */
    public String validate(String year, boolean isActive)
    {
        List<org.kaleta.persistence.entity.Schema> schemas = schemaDao.list(year);
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
            for (org.kaleta.persistence.entity.Schema schema : schemas)
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
        for (org.kaleta.persistence.entity.Schema schema : schemas)
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

    /**
     * Syncs every year of the specified data source.
     *
     * @return a report of what was synced, year by year
     */
    public String syncAll(String dataSource) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        for (String year : getYears(dataSource)){
            sb.append(sync(dataSource, year));
        }
        return sb.toString();
    }

    /**
     * Syncs every year of the specified data source and validates each once it is synced.
     *
     * @return a report of what was synced and of each year's validation, and whether every year
     * passed it
     */
    public SyncReport syncAndValidateAll(String dataSource) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        boolean hasError = false;
        for (String year : getYears(dataSource)){
            sb.append(sync(dataSource, year));
            String message = validate(year, isActive(dataSource, year));
            if (!message.contains("data valid")) hasError = true;
            sb.append(message).append("\n");
        }
        return new SyncReport(sb.toString(), !hasError);
    }

    /** What a sync did: a report of it to read, and whether all the data it synced was valid. */
    public record SyncReport(String text, boolean valid) {}
}
