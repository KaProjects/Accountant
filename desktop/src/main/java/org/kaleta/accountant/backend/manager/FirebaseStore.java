package org.kaleta.accountant.backend.manager;

import org.kaleta.accountant.backend.model.FirebaseTransactionModel;

import java.util.List;

/**
 * Access to the transactions and accounts shared with the Android app.
 * <p>
 * Implemented by {@link RealtimeFirebaseStore} against the real Firebase database, and by
 * InMemoryFirebaseStore (src/dev/java) against a local JSON snapshot for development.
 */
public interface FirebaseStore {

    /**
     * Transactions recorded by the Android app and not imported yet.
     */
    List<FirebaseTransactionModel> getTransactionList();

    /**
     * Discards all transactions, assuming they have been imported.
     */
    void clearTransactions();

    /**
     * Whether the account is already offered by the Android app.
     */
    boolean hasAccount(String accountId, boolean isDebit);

    /**
     * Makes the account selectable in the Android app.
     */
    void pushAccount(String id, String name, boolean isDebit);
}
