package org.kaleta.accountant.frontend.component.year.model;

import org.kaleta.accountant.frontend.common.constants.AccountType;
import org.kaleta.accountant.frontend.common.constants.DefaultSchemaId;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Stanislav Kaleta on 14.02.2017.
 */
public class AccountModel {
    private List<Account> accounts = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();

    public AccountModel(){
    }

    public String getAccInitState(Account account) {
        String type = account.getType();
        String id = account.getSchemaId() + "." + account.getSemanticId();
        for (Transaction tr : transactions) {
            if (type.equals(AccountType.ASSET) || type.equals(AccountType.EXPENSE)) {
                if (tr.getDebit().equals(id)
                        && tr.getCredit().startsWith(DefaultSchemaId.INIT_ACC)) {
                    return tr.getAmount();
                }
            }
            if (type.equals(AccountType.LIABILITY) || type.equals(AccountType.REVENUE)) {
                if (tr.getCredit().equals(id)
                        && tr.getDebit().startsWith(DefaultSchemaId.INIT_ACC)) {
                    return tr.getAmount();
                }
            }
        }
        throw new IllegalArgumentException("Account has no initial state");
    }

    public String getAccFinalState(Account account){
        String type = account.getType();
        String id = account.getSchemaId() + "." + account.getSemanticId();
        for (Transaction tr : transactions){
            if (type.equals(AccountType.ASSET) || type.equals(AccountType.EXPENSE)) {
                if (tr.getCredit().equals(id)
                        &&tr.getDebit().startsWith(DefaultSchemaId.CLOSING_ACC)){
                    return tr.getAmount();
                }
            }
            if (type.equals(AccountType.LIABILITY) || type.equals(AccountType.REVENUE)){
                if (tr.getDebit().equals(id)
                        && tr.getCredit().startsWith(DefaultSchemaId.CLOSING_ACC)){
                    return tr.getAmount();
                }
            }
        }
        throw new IllegalArgumentException("Account has no final state");
    }

    public String getAccBalance(Account account){
        String type = account.getType();
        if (type.equals(AccountType.OFF_BALANCE)){
            throw new IllegalArgumentException("Off-Balance accounts has no balance");
        }
        String id = account.getSchemaId() + "." + account.getSemanticId();
        Integer balance = 0;
        for (Transaction tr : transactions){
            if (tr.getCredit().equals(id)){
                if (type.equals(AccountType.ASSET) || type.equals(AccountType.EXPENSE)) {
                    balance -= Integer.parseInt(tr.getAmount());
                } else {
                    balance += Integer.parseInt(tr.getAmount());
                }
            }
            if (tr.getDebit().equals(id)){
                if (type.equals(AccountType.ASSET) || type.equals(AccountType.EXPENSE)) {
                    balance += Integer.parseInt(tr.getAmount());
                } else {
                    balance -= Integer.parseInt(tr.getAmount());
                }
            }
        }
        return String.valueOf(balance);
    }

    public int getAccTurnover(Account account){
        // TODO: 2/15/17 decide if A->debitTurnvoer ... or maxTurnover
        String accId = account.getSchemaId() + "." + account.getSemanticId();
        int debitTurnover = 0;
        int creditTurnover = 0;
        for (Transaction tr : transactions){
            if (tr.getDebit().equals(accId)){
                debitTurnover += Integer.parseInt(tr.getAmount());
            }
            if (tr.getCredit().equals(accId)){
                creditTurnover += Integer.parseInt(tr.getAmount());
            }
        }
        return (debitTurnover > creditTurnover) ? debitTurnover : creditTurnover;
    }


    public boolean isGroupDeletable(int cId, int gId){
        String schemaId = String.valueOf(cId) + String.valueOf(gId);
        boolean deletable = true;
        for (Account account : accounts){
            if (account.getSchemaId().startsWith(schemaId) && getAccTurnover(account) > 0) deletable = false;
        }
        return deletable;
    }

    public boolean isAccountDeletable(int cId, int gId, int aId){
        String schemaId = String.valueOf(cId) + String.valueOf(gId) + String.valueOf(aId);
        boolean deletable = true;
        for (Account account : accounts){
            if (account.getSchemaId().startsWith(schemaId) && getAccTurnover(account) > 0) deletable = false;
        }
        return deletable;
    }

    public List<Account> getAccountsBySchema(String schemaId){
        return accounts.stream().filter(account -> account.getSchemaId().startsWith(schemaId)).collect(Collectors.toList());
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public static class Account {
        private String schemaId;
        private String semanticId;
        private String type;
        private String name;
        private String metadata;

        public Account() {
        }

        public Account(String schemaId, String semanticId, String type, String name, String metadata) {
            this.schemaId = schemaId;
            this.type = type;
            this.semanticId = semanticId;
            this.name = name;
            this.metadata = metadata;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSchemaId() {
            return schemaId;
        }

        public void setSchemaId(String schemaId) {
            this.schemaId = schemaId;
        }

        public String getSemanticId() {
            return semanticId;
        }

        public void setSemanticId(String semanticId) {
            this.semanticId = semanticId;
        }

        public String getMetadata() {
            return metadata;
        }

        public void setMetadata(String metadata) {
            this.metadata = metadata;
        }
    }

    public static class Transaction{
        private String id;
        private String date;
        private String description;
        private String amount;
        private String debit;
        private String credit;

        public Transaction() {
        }

        public Transaction(String id, String date, String description, String amount, String debit, String credit) {
            this.id = id;
            this.date = date;
            this.description = description;
            this.amount = amount;
            this.debit = debit;
            this.credit = credit;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getDebit() {
            return debit;
        }

        public void setDebit(String debit) {
            this.debit = debit;
        }

        public String getCredit() {
            return credit;
        }

        public void setCredit(String credit) {
            this.credit = credit;
        }
    }
}
