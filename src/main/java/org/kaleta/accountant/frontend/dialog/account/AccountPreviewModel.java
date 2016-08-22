package org.kaleta.accountant.frontend.dialog.account;

import org.kaleta.accountant.backend.entity.Semantic;
import org.kaleta.accountant.backend.entity.Transaction;
import org.kaleta.accountant.service.Service;

import java.util.*;

/**
 * Created by Stanislav Kaleta on 16.04.2016.
 */
public class AccountPreviewModel {
    private String schemaId;
    private String accType;
    private String accName;
    private List<Transaction> transactionList;
    private List<Semantic.Account> semanticList;
    private Map<String, Map<String, List<Transaction>>> accounts;

    public AccountPreviewModel(String schemaId, int year){
        this.schemaId = schemaId;
        accType = Service.ACCOUNT.getAccountType(schemaId);
        accName = Service.ACCOUNT.getAccountName(schemaId);
        transactionList = Service.JOURNAL.listAccountTransactions(schemaId, year);
        semanticList = Service.ACCOUNT.getSemanticAccounts(schemaId);
        accounts = new LinkedHashMap<>();
        for (int i=0; i<=semanticList.size(); i++){
            Map<String, List<Transaction>> transactions = new HashMap<>();
            transactions.put("DEBIT", new ArrayList<>());
            transactions.put("CREDIT", new ArrayList<>());
            String accId = (i==0) ? schemaId : schemaId + "-" + i;
            for (Transaction transaction : transactionList){
                if (transaction.getDebit().equals(accId)){
                    transactions.get("DEBIT").add(transaction);
                }
                if (transaction.getCredit().equals(accId)){
                    transactions.get("CREDIT").add(transaction);
                }
            }
            accounts.put(getSemanticName(schemaId, i), transactions);
        }
    }

    private String getSemanticName(String schemaId, int accId){
        for (Semantic.Account account : semanticList){
            if (account.getSchemaId().equals(schemaId) && account.getId().equals(String.valueOf(accId))){
                return account.getName();
            }
        }
        return "Default";
    }

    public String getAccountName(){
        return accName;
    }

    public String getAccountType(){
        return accType;
    }

    public Set<String> getSemanticNames(){
        return accounts.keySet();
    }

    public List<Transaction> getSemanticTransactions(String semanticName, boolean isDebit){
        return accounts.get(semanticName).get((isDebit) ? "DEBIT" : "CREDIT");
    }

    public String getSemanticId(String semanticName){
        for (Semantic.Account account : semanticList){
            if (account.getName().equals(semanticName)){
                return account.getSchemaId() + "-" + account.getId();
            }
        }
        return schemaId;
    }


}
