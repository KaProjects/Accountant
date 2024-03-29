package org.kaleta.accountant.backend.manager;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import com.google.firebase.internal.NonNull;
import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.backend.model.FirebaseAccountModel;
import org.kaleta.accountant.backend.model.FirebaseTransactionModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FirebaseManager {

    private static FirebaseManager instance;

    private final String serviceKey = "/accountant-andr-servicekey.json";
    private final String databaseUrl = "https://accountant-andr.firebaseio.com/";

    private final FirebaseDatabase database;

    private final List<FirebaseTransactionModel> transactionList = new ArrayList<>();
    private final List<FirebaseAccountModel> debitList = new ArrayList<>();
    private final List<FirebaseAccountModel> creditList = new ArrayList<>();

    public static FirebaseManager getInstance() throws ManagerException {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    private FirebaseManager() throws ManagerException {
        FirebaseOptions options;
        try {
            InputStream serviceKeyStream = Initializer.class.getResourceAsStream(serviceKey);

            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceKeyStream))
                    .setDatabaseUrl(databaseUrl)
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

    public List<FirebaseTransactionModel> getTransactionList() {
        return transactionList;
    }

    public void clearTransactions() {
        database.getReference("transaction").removeValueAsync();
    }

    public boolean hasAccount(String accountId, boolean isDebit){
        for (FirebaseAccountModel account : isDebit ? debitList : creditList){
            if (Objects.equals(account.getId(), accountId)){
                return true;
            }
        }
        return false;
    }

    public void pushAccount(String id, String name, boolean isDebit){
        Initializer.LOG.info((isDebit ? "Debit" : "Credit") + " account added to Firebase: id=" + id + " name='" + name + "'");
        database.getReference("accounts/" + (isDebit ? "debit" : "credit") + "/" + id.replace(".", "a"))
                .setValue(name, (databaseError, databaseReference) -> {});

    }
}
