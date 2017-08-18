package org.kaleta.accountant.frontend.component.year.model;

import org.kaleta.accountant.frontend.Initializer;
import org.kaleta.accountant.frontend.common.constants.AccountType;
import org.kaleta.accountant.frontend.common.constants.DefaultSchemaId;
import org.kaleta.accountant.service.ServiceFailureException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Stanislav Kaleta on 14.02.2017.
 */
public class AccountModel {
    private List<Account> accounts = new ArrayList<Account>(){// TODO: 8/18/17 this is only for DEBUG purposes
        @Override
        public boolean add(Account account) {
            Initializer.LOG.info("Account added: " + account.getFullId() + " \"" + account.getName() + "\"");
            return super.add(account);
        }
    };
    private List<Transaction> transactions = new ArrayList<Transaction>(){// TODO: 8/18/17 this is only for DEBUG purposes
        @Override
        public boolean add(Transaction tr) {
            Initializer.LOG.info("Transaction added: " + tr.getDate() + " " + tr.getDebit() + "/" + tr.getCredit() + " " + tr.getAmount() + " " + tr.getDescription());
            return super.add(tr);
        }
    };

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

    // TODO: 3/17/17 change this to: if a acc./tr. is assigned - create acc. create also transaction - despite 0 value, it can cause errors
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

    public Account getAccount(String schemaId, String semanticId){
        if (schemaId.length() != 3){
            throw new IllegalArgumentException("'" + schemaId + "' is not valid schemaId - should have length 3, but has " + schemaId.length());
        }
        List<Account> accountsBySchema = getAccountsBySchema(schemaId);
        List<Account> accountsBySemantic = accountsBySchema.stream().filter(account -> account.getSemanticId().equals(semanticId)).collect(Collectors.toList());
        switch (accountsBySemantic.size()){
            case 0:{
                throw new NullPointerException("Account not found for schemaId '" + schemaId + "' and semanticId '" + semanticId + "'");
            }
            case 1:{
                return accountsBySemantic.get(0);
            }
            default:
                throw new ServiceFailureException("Critical Error: found " + accountsBySemantic.size() +" accounts for schemaId '" + schemaId + "' and semanticId '" + semanticId + "'. Combination of schmaId & semanticId should be unique!");
        }
    }

    public List<Transaction> getTransactions(String debit, String credit){
        List<Transaction> trList = new ArrayList<>();
        for (Transaction tr : getTransactions()){
            if (tr.getCredit().equals(credit) && tr.getDebit().equals(debit)){
                trList.add(tr);
            }
        }

        return trList;
    }

    public String getNextTransactionId(){
        int lastTrId = 0;
        for (AccountModel.Transaction tr : getTransactions()) {
            int thisTrId = Integer.parseInt(tr.getId());
            if (thisTrId > lastTrId) lastTrId = thisTrId;
        }
        return String.valueOf(++lastTrId);
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

        public String getFullId(){
            return schemaId+"."+semanticId;
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
