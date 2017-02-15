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

    public AccountModel(){

    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public List<Account> getAccountsBySchema(String schemaId){
        return accounts.stream().filter(account -> account.getSchemaId().startsWith(schemaId)).collect(Collectors.toList());
    }


    public boolean groupDeletable(int cId, int gId){
        String schemaId = String.valueOf(cId) + String.valueOf(gId);
        boolean deletable = true;
        for (Account account : accounts){
            if (account.getSchemaId().startsWith(schemaId) && account.getTurnover() > 0) deletable = false;
        }
        return deletable;
    }

    public boolean accountDeletable(int cId, int gId, int aId){
        String schemaId = String.valueOf(cId) + String.valueOf(gId) + String.valueOf(aId);
        boolean deletable = true;
        for (Account account : accounts){
            if (account.getSchemaId().startsWith(schemaId) && account.getTurnover() > 0) deletable = false;
        }
        return deletable;
    }

    public static class Account {
        private String schemaId;
        private String semanticId;
        private String type;
        private String name;
        private List<Transaction> transactions = new ArrayList<>();

        public Account() {
        }

        public Account(String schemaId, String semanticId, String type, String name) {
            this.schemaId = schemaId;
            this.type = type;
            this.semanticId = semanticId;
            this.name = name;
        }

        public String getInitState() {
            for (Transaction tr : transactions) {
                if (type.equals(AccountType.ASSET) || type.equals(AccountType.EXPENSE)) {
                    if (tr.getDebit().startsWith(schemaId)
                            && tr.getCredit().startsWith(DefaultSchemaId.INIT_ACC)) {
                        return tr.getAmount();
                    }
                }
                if (type.equals(AccountType.LIABILITY) || type.equals(AccountType.REVENUE)) {
                    if (tr.getCredit().startsWith(schemaId)
                            && tr.getDebit().startsWith(DefaultSchemaId.INIT_ACC)) {
                        return tr.getAmount();
                    }
                }
            }
            throw new IllegalArgumentException("Account has no initial state");
        }

        public String getFinalState(){
            for (Transaction tr : transactions){
                if (type.equals(AccountType.ASSET) || type.equals(AccountType.EXPENSE)) {
                    if (tr.getCredit().startsWith(schemaId)
                            &&tr.getDebit().startsWith(DefaultSchemaId.CLOSING_ACC)){
                        return tr.getAmount();
                    }
                }
                if (type.equals(AccountType.LIABILITY) || type.equals(AccountType.REVENUE)){
                    if (tr.getDebit().startsWith(schemaId)
                            &&tr.getCredit().startsWith(DefaultSchemaId.CLOSING_ACC)){
                        return tr.getAmount();
                    }
                }
            }
            throw new IllegalArgumentException("Account has no final state");
        }

        public int getTurnover(){
            // TODO: 2/15/17 decide if A->debitTurnvoer ... or maxTurnover
            int debitTurnover = 0;
            int creditTurnover = 0;
            for (Transaction tr : transactions){
                if (tr.getDebit().startsWith(schemaId)){
                    debitTurnover =+ Integer.parseInt(tr.getAmount());
                }
                if (tr.getCredit().startsWith(schemaId)){
                    creditTurnover =+ Integer.parseInt(tr.getAmount());
                }
            }
            return (debitTurnover > creditTurnover) ? debitTurnover : creditTurnover;
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

        public List<Transaction> getTransactions() {
            return transactions;
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
