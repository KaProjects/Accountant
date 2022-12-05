package org.kaleta.accountant;

import com.google.firebase.database.DatabaseReference;

import org.kaleta.accountant.data.Account;
import org.kaleta.accountant.data.DataSource;
import org.kaleta.accountant.data.Transaction;

import java.util.List;

public class Service {

    private Service() {
    }

    public static void addTransaction(Transaction transaction) {
        DatabaseReference reference = DataSource.getInstance().getTransactionRef();
        String id = reference.push().getKey();
        reference.child(id).setValue(transaction);
    }

    private static void prepareAccounts() {
        DatabaseReference reference = DataSource.getInstance().getDebitRef();
        reference.child("530a12").setValue("Restauracie");
        reference.child("531a6").setValue("FastFood");
        reference.child("532a7").setValue("Kaviarne");
        reference.child("533a0").setValue("Bary");

        reference.child("521a0").setValue("Masaz");
        reference.child("521a1").setValue("Wellness");
        reference.child("521a2").setValue("Fitko");
        reference.child("521a3").setValue("Kupko/Bazen");
        reference.child("521a7").setValue("Pece o zuby");

        reference = DataSource.getInstance().getCreditRef();
        reference.child("200a0").setValue("Hotovost - EUR");
        reference.child("200a1").setValue("Hotovost - CZK");
        reference.child("201a0").setValue("Stravenky");
        reference.child("601a3").setValue("Multi Sport - vstupy");
    }

    public static List<Account> getDebitAccounts() {
        return DataSource.getInstance().getDebitAccountList();
    }

    public static Account getDebitAccount(String id) {
        for (Account account : DataSource.getInstance().getDebitAccountList()) {
            if (account.getId().equals(id)) return account;
        }
        throw new IllegalArgumentException("Account with id=" + id + " not found!");
    }

    public static List<Account> getCreditAccounts() {
        return DataSource.getInstance().getCreditAccountList();
    }

    public static Account getCreditAccount(String id) {
        for (Account account : DataSource.getInstance().getCreditAccountList()) {
            if (account.getId().equals(id)) return account;
        }
        throw new IllegalArgumentException("Account with id=" + id + " not found!");
    }

}
