package org.kaleta.accountant.backend.manager;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import com.google.firebase.internal.NonNull;
import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.backend.model.FirebaseAccountModel;
import org.kaleta.accountant.backend.model.FirebaseTransactionModel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RealtimeFirebaseStore implements FirebaseStore {

    private static RealtimeFirebaseStore instance;

    /** The remote store's access, kept encrypted in the repository and named after nothing. */
    private static final String SERVICE_KEY = "/remote-store.json";
    /** Set this to point the app at another database - a region, or a second project. */
    public static final String DATABASE_URL_PROPERTY = "accountant.firebase.database-url";

    private final FirebaseDatabase database;

    private final List<FirebaseTransactionModel> transactionList = new ArrayList<>();
    private final List<FirebaseAccountModel> debitList = new ArrayList<>();
    private final List<FirebaseAccountModel> creditList = new ArrayList<>();

    /**
     * Where the database lives, taken from the project named in the service key rather than written
     * out here: the key is the one file that is encrypted in the repository, and naming the project
     * in the source as well would say publicly what the key is careful not to.
     * <p>
     * A database that does not follow the default naming - one pinned to a region, or a second
     * project - is given with the {@value #DATABASE_URL_PROPERTY} system property instead.
     */
    private static String databaseUrl(byte[] serviceKey) throws ManagerException {
        String configured = System.getProperty(DATABASE_URL_PROPERTY);
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        Matcher projectId = Pattern.compile("\"project_id\"\\s*:\\s*\"([^\"]+)\"")
                .matcher(new String(serviceKey, StandardCharsets.UTF_8));
        if (!projectId.find()) {
            throw new ManagerException("Firebase service key names no project, and no "
                    + DATABASE_URL_PROPERTY + " is set");
        }
        return "https://" + projectId.group(1) + ".firebaseio.com/";
    }

    public static RealtimeFirebaseStore getInstance() throws ManagerException {
        if (instance == null) {
            instance = new RealtimeFirebaseStore();
        }
        return instance;
    }

    private RealtimeFirebaseStore() throws ManagerException {
        FirebaseOptions options;
        try (InputStream serviceKeyStream = Initializer.class.getResourceAsStream(SERVICE_KEY)) {
            if (serviceKeyStream == null) {
                throw new ManagerException("Firebase service key '" + SERVICE_KEY + "' is missing");
            }
            // read once and used twice: the credentials, and the project the database belongs to
            byte[] serviceKey = serviceKeyStream.readAllBytes();

            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(serviceKey)))
                    .setDatabaseUrl(databaseUrl(serviceKey))
                    .build();
        } catch (IOException e) {
            throw new ManagerException(e);
        }

        FirebaseApp app = FirebaseApp.initializeApp(options);
        database = FirebaseDatabase.getInstance(app);

        database.getReference("transaction").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                transactionList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    try {
                        FirebaseTransactionModel transaction = postSnapshot.getValue(FirebaseTransactionModel.class);
                        transactionList.add(transaction);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        database.getReference("accounts/credit").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                creditList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    try {
                        FirebaseAccountModel account = new FirebaseAccountModel((String) postSnapshot.getValue(), postSnapshot.getKey().replace("a", "."));
                        creditList.add(account);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        database.getReference("accounts/debit").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                debitList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    try {
                        FirebaseAccountModel account = new FirebaseAccountModel((String) postSnapshot.getValue(), postSnapshot.getKey().replace("a", "."));
                        debitList.add(account);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public List<FirebaseTransactionModel> getTransactionList() {
        return transactionList;
    }

    @Override
    public void clearTransactions() {
        database.getReference("transaction").removeValueAsync();
    }

    @Override
    public boolean hasAccount(String accountId, boolean isDebit){
        for (FirebaseAccountModel account : isDebit ? debitList : creditList){
            if (Objects.equals(account.getId(), accountId)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void pushAccount(String id, String name, boolean isDebit){
        Initializer.LOG.info((isDebit ? "Debit" : "Credit") + " account added to Firebase: id=" + id + " name='" + name + "'");
        database.getReference("accounts/" + (isDebit ? "debit" : "credit") + "/" + id.replace(".", "a"))
                .setValue(name, (databaseError, databaseReference) -> {});

    }
}
