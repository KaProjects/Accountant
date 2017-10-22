package org.kaleta.accountant.service;

import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.manager.TransactionsManager;
import org.kaleta.accountant.backend.model.TransactionsModel;
import org.kaleta.accountant.common.ErrorHandler;
import org.kaleta.accountant.frontend.Initializer;

/**
 * Provides access to data source which is related to transactions.
 */
public class TransactionsService {

    TransactionsService(){
        // package-private
    }

    /**
     * todo = maybe marge with accounts service
     */


//    /**
//     * todo
//     */
//    public String getNextTransactionId(String year){
//        try {
//            return String.valueOf(new TransactionsManager(year).retrieve().getTransaction().size());
//        } catch (ManagerException e){
//            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
//            throw new ServiceFailureException(e);
//        }
//    }

    /**
     * todo
     */
    public void addTransaction(String year, String date, String amount, String debit, String credit, String description){
        try {
            TransactionsManager manager = new TransactionsManager(year);
            TransactionsModel model = manager.retrieve();

            TransactionsModel.Transaction transaction = new TransactionsModel.Transaction();
            transaction.setId(String.valueOf(model.getTransaction().size()));
            transaction.setDate(date);
            transaction.setAmount(amount);
            transaction.setDebit(debit);
            transaction.setCredit(credit);
            transaction.setDescription(description);
            model.getTransaction().add(transaction);

            manager.update(model);
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

}
