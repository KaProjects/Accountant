package org.kaleta.accountant.service;

import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.backend.manager.FirebaseStore;
import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.manager.RealtimeFirebaseStore;
import org.kaleta.accountant.backend.model.FirebaseTransactionModel;

import java.util.List;

public class FirebaseService {

    /**
     * Selects the {@link FirebaseStore} implementation: "real" (default) talks to the Firebase
     * database, "fake" uses the local JSON snapshot so development needs no network and no
     * service key. Set it with -Dfirebase.mode=fake.
     */
    private static final String MODE_PROPERTY = "firebase.mode";
    private static final String MODE_FAKE = "fake";
    private static final String IN_MEMORY_STORE = "org.kaleta.accountant.backend.manager.InMemoryFirebaseStore";

    private final FirebaseStore store;

    FirebaseService() {
        // package-private
        try {
            store = MODE_FAKE.equals(System.getProperty(MODE_PROPERTY)) ? createFakeStore() : RealtimeFirebaseStore.getInstance();
        } catch (ManagerException e) {
            throw new ServiceFailureException("Error while initializing Firebase manager" + e);
        }
    }

    /**
     * The fake lives in src/dev/java and is absent from production builds, so it is resolved
     * reflectively instead of being referenced directly.
     */
    private static FirebaseStore createFakeStore() throws ManagerException {
        try {
            FirebaseStore fake = (FirebaseStore) Class.forName(IN_MEMORY_STORE).getDeclaredConstructor().newInstance();
            Initializer.LOG.info("Firebase: using the in-memory store (-D" + MODE_PROPERTY + "=" + MODE_FAKE + ")");
            return fake;
        } catch (ReflectiveOperationException e) {
            throw new ManagerException(new IllegalStateException(
                    "'" + MODE_PROPERTY + "=" + MODE_FAKE + "' requires the dev sources; build with the 'dev' profile.", e));
        }
    }

    public List<FirebaseTransactionModel> loadTransactions() {
        return store.getTransactionList();
    }

    /**
     * Note: assuming all transactions have been loaded and committed, therefore could be removed from firebase database.
     */
    public void clearLoadedTransactions() {
        store.clearTransactions();
    }

    public void addAccountsIfMissing(String year, String debit, String credit) {
        if (!store.hasAccount(debit, true)) {
            String name = Service.ACCOUNT.getAccountAndGroupName(year, debit);
            store.pushAccount(debit, name, true);
        }
        if (!store.hasAccount(credit, false)) {
            String name = Service.ACCOUNT.getAccountAndGroupName(year, credit);
            store.pushAccount(credit, name, false);
        }
    }
}
