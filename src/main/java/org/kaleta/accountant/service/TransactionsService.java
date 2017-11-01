package org.kaleta.accountant.service;

import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.manager.TransactionsManager;
import org.kaleta.accountant.backend.model.TransactionsModel;
import org.kaleta.accountant.common.ErrorHandler;
import org.kaleta.accountant.frontend.Initializer;
import org.kaleta.accountant.frontend.common.AccountPairModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides access to data source which is related to transactions.
 */
public class TransactionsService {

    TransactionsService(){
        // package-private
    }

    /**
     * Adds transaction for specified values.
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
            //  TODO 1.0 : LOG
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns transactions for specified debit and credit.
     * Use 'null' for debit/credit if you want to list transaction only for credit/debit
     */
    public List<TransactionsModel.Transaction> getTransactions(String year, String debit, String credit) {
        try {
            TransactionsModel model = new TransactionsManager(year).retrieve();
            List<TransactionsModel.Transaction> transactionList = new ArrayList<>();
            for (TransactionsModel.Transaction transaction : model.getTransaction()){
                if (debit == null && credit != null){
                    if (transaction.getCredit().equals(credit)){
                        transactionList.add(transaction);
                    }
                }
                if (debit != null && credit == null){
                    if (transaction.getDebit().equals(debit)){
                        transactionList.add(transaction);
                    }
                }
                if (debit != null && credit != null){
                    if (transaction.getDebit().equals(debit) && transaction.getCredit().equals(credit)){
                        transactionList.add(transaction);
                    }
                }
                if (debit == null && credit == null){
                    transactionList.add(transaction);
                }
            }
            return transactionList;
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns list of descriptions mapped by account pair.
     */
    public Map<AccountPairModel, List<String>> getAccountPairDescriptions(String year){
        try {
            TransactionsModel model = new TransactionsManager(year).retrieve();
            Map<AccountPairModel, List<String>> accountPairDescriptionMap = new HashMap<>();
            for (TransactionsModel.Transaction transaction : model.getTransaction()){
                AccountPairModel accountPair = new AccountPairModel(transaction.getDebit(), transaction.getCredit());
                accountPairDescriptionMap.computeIfAbsent(accountPair, k -> new ArrayList<>());
                accountPairDescriptionMap.get(accountPair).add(transaction.getDescription());
            }
            return accountPairDescriptionMap;
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }
}
