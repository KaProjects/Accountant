package org.kaleta.accountant.service;

import org.kaleta.accountant.backend.entity.Journal;
import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.manager.jaxb.JournalManager;
import org.kaleta.accountant.frontend.Initializer;
import org.kaleta.accountant.frontend.common.ErrorDialog;

import java.util.List;

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
     * todo doc
     */
    public List<Integer> getYears(){
        // TODO: 5/23/16 load children of journal dir
        return null;
    }

    /**
     * todo doc
     */
    public void createJournal(int year){
        try {
            new JournalManager().create(year);
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo doc
     */
    public Journal getYournal(int year){
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
    public void addTransaction(Journal.Transaction transaction, int year){
        try {
            JournalManager manager = new JournalManager();
            Journal journal = manager.retrieve(year);
            journal.getTransaction().add(transaction);
            manager.update(journal);
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }
}
