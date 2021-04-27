package org.kaleta.accountant.data;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataSource {

    private static DataSource instance;

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    private final DatabaseReference transactionRef = database.getReference("transaction");
    private final DatabaseReference debitRef = database.getReference("accounts/debit");
    private final DatabaseReference creditRef = database.getReference("accounts/credit");

    private final List<Transaction> transactionList = new ArrayList<>();
    private final List<Account> debitAccountList = new ArrayList<>();
    private final List<Account> creditAccountList = new ArrayList<>();

    public static DataSource getInstance() {
        if (instance == null) {
            instance = new DataSource();
        }
        return instance;
    }

    private DataSource() {
        transactionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                transactionList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Transaction transaction = postSnapshot.getValue(Transaction.class);
                    transactionList.add(transaction);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        debitRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                debitAccountList.clear();
                Map<String, String> tempMap = (Map<String, String>) dataSnapshot.getValue();
                for (String key : tempMap.keySet()) {
                    debitAccountList.add(new Account(key.replaceAll("a", "."), tempMap.get(key)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        creditRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                creditAccountList.clear();
                Map<String, String> tempMap = (Map<String, String>) dataSnapshot.getValue();
                for (String key : tempMap.keySet()) {
                    creditAccountList.add(new Account(key.replaceAll("a", "."), tempMap.get(key)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public FirebaseDatabase getDatabase() {
        return database;
    }

    public DatabaseReference getTransactionRef() {
        return transactionRef;
    }

    public DatabaseReference getDebitRef() {
        return debitRef;
    }

    public DatabaseReference getCreditRef() {
        return creditRef;
    }

    public List<Transaction> getTransactionList() {
        return transactionList;
    }

    public List<Account> getDebitAccountList() {
        return debitAccountList;
    }

    public List<Account> getCreditAccountList() {
        return creditAccountList;
    }
}
