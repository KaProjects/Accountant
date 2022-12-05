package org.kaleta.accountant.service;

import org.kaleta.accountant.backend.manager.FirebaseManager;
import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.model.FirebaseTransactionModel;

import java.util.List;

public class FirebaseService {

    private final FirebaseManager manager;

    FirebaseService() {
        // package-private
        try {
            manager = FirebaseManager.getInstance();
        } catch (ManagerException e) {
            throw new ServiceFailureException("Error while initializing Firebase manager" + e);
        }

    }

    public List<FirebaseTransactionModel> loadTransactions() {
        return manager.getTransactionList();
    }

    /**
     * Note: assuming all transactions have been loaded and committed, therefore could be removed from firebase database.
     */
    public void clearLoadedTransactions() {
        manager.clearTransactions();
    }
}
