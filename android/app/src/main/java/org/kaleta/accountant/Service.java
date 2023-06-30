package org.kaleta.accountant;

import com.google.firebase.database.DatabaseReference;

import org.kaleta.accountant.data.Account;
import org.kaleta.accountant.data.DataSource;
import org.kaleta.accountant.data.Template;
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

    @Deprecated
    public static void addAccount(Account account, boolean isDebit) {
        DatabaseReference reference = isDebit
                ? DataSource.getInstance().getDebitRef()
                : DataSource.getInstance().getCreditRef();
        reference.child(account.getId().replace(".","a")).setValue(account.getName());
    }

    public static void addTemplate(Template template) {
        DatabaseReference reference = DataSource.getInstance().getTemplatesRef();
        String name = template.getName();
        template.setName(null);
        reference.child(name).setValue(template);
    }

    public static List<Template> getTemplates() {
        return DataSource.getInstance().getTemplatesList();
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
