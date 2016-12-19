package org.kaleta.accountant.service;

import org.kaleta.accountant.backend.entity.Config;
import org.kaleta.accountant.backend.entity.Journal;
import org.kaleta.accountant.backend.entity.Transaction;
import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.manager.jaxb.ConfigManager;
import org.kaleta.accountant.backend.manager.jaxb.JournalManager;
import org.kaleta.accountant.frontend.Initializer;
import org.kaleta.accountant.frontend.common.ErrorDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Stanislav Kaleta on 23.05.2016.
 *
 * todo doc
 */
public class JournalService {

    JournalService(){
        // package-private
    }

    /**
     * Loads all years from configuration data source.
     */
    public List<Integer> getYears(){
        try {
            List<Integer> years = new ArrayList<>();
            for (Config.Years.Year year : new ConfigManager().retrieve().getYears().getYear()){
                years.add(Integer.parseInt(year.getName()));
            }
            return years;
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo doc
     *
     * @throws IllegalArgumentException if specified year already exists
     */
    public void createJournal(int year) throws IllegalArgumentException{
        try {
            ConfigManager configManager = new ConfigManager();
            Config config = configManager.retrieve();
            for (Config.Years.Year usedYear : config.getYears().getYear()){
                if (usedYear.getName().equals(String.valueOf(year))){
                    throw new IllegalArgumentException("year " + year + "already exists!");
                }
            }

            new JournalManager().create(year);

            Config.Years.Year newYear = new Config.Years.Year();
            newYear.setName(String.valueOf(year));
            config.getYears().getYear().add(newYear);
            configManager.update(config);
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo doc
     */
    public Journal getJournal(int year){
        try {
            return new JournalManager().retrieve(year);
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo doc
     */
    public void addTransaction(Transaction transaction, int year){
        try {
            JournalManager manager = new JournalManager();
            Journal journal = manager.retrieve(year);

            int lastId = 0;
            for (Transaction tr : journal.getTransaction()){
                lastId = (Integer.parseInt(tr.getId()) > lastId) ? Integer.parseInt(tr.getId()) : lastId;
            }
            transaction.setId(String.valueOf(lastId + 1));

            journal.getTransaction().add(transaction);
            manager.update(journal);
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo doc
     */
    public List<Transaction> listAccountTransactions(String schemaId, int year){
        try {
            JournalManager manager = new JournalManager();
            return manager.retrieve(year).getTransaction().stream()
                    .filter(transaction -> transaction.getCredit().startsWith(schemaId) || transaction.getDebit().startsWith(schemaId))
                    .collect(Collectors.toList());
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns all used unique descriptions for specified combination of debit and credit accounts.
     */
    public Set<String> listTransactionDescriptions(String debitSchemaId, String creditSchemaId, int year){
        try {
            return new JournalManager().retrieve(year).getTransaction().stream()
                    .filter(transaction -> transaction.getDebit().equals(debitSchemaId) && transaction.getCredit().equals(creditSchemaId))
                    .map(Transaction::getDescription)
                    .collect(Collectors.toSet());
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }
}
