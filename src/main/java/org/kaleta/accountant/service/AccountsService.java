package org.kaleta.accountant.service;

import org.kaleta.accountant.backend.manager.AccountsManager;
import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.manager.TransactionsManager;
import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.TransactionsModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.common.ErrorHandler;
import org.kaleta.accountant.frontend.Initializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides access to data source which is related to accounts.
 */
public class AccountsService {

    AccountsService(){
        // package-private
    }

    /**
     * todo
     */
    public List<AccountsModel.Account> getAccountsBySchemaId(String year, String schemaIdPrefix){
        try {
            AccountsModel accountsModel = new AccountsManager(year).retrieve();
            List<AccountsModel.Account> eligibleAccounts = new ArrayList<>();
            for (AccountsModel.Account account : accountsModel.getAccount()) {
                if (account.getSchemaId().startsWith(schemaIdPrefix)) {
                    eligibleAccounts.add(account);
                }
            }
            return eligibleAccounts;
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo
     */
    public Map<String, List<AccountsModel.Account>> getAccountsViaSchemaMap(String year){
        try {
            Map<String, List<AccountsModel.Account>> accountMap = new HashMap<>();
            for (AccountsModel.Account account : new AccountsManager(year).retrieve().getAccount()){
                String schemaId = account.getSchemaId();
                if (accountMap.keySet().contains(schemaId)){
                    accountMap.get(schemaId).add(account);
                } else {
                    accountMap.put(schemaId, new ArrayList<>());
                    accountMap.get(schemaId).add(account);
                }
            }
            return accountMap;
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo
     */
    public String getAccountTurnover(String year, AccountsModel.Account account) {
        String accountType = Service.SCHEMA.getSchemaAccountType(year, account.getSchemaId());
        if (accountType.equals(Constants.AccountType.OFF_BALANCE)){
            throw new IllegalArgumentException("Off-Balance accounts has no balance");
        }
        try {
            String accountId = account.getSchemaId() + "." + account.getSemanticId();
            Integer turnover = 0;
            for (TransactionsModel.Transaction tr : new TransactionsManager(year).retrieve().getTransaction()){
                if (tr.getDebit().equals(accountId) && (accountType.equals(Constants.AccountType.ASSET) || accountType.equals(Constants.AccountType.EXPENSE))){
                    turnover += Integer.parseInt(tr.getAmount());
                }
                if (tr.getCredit().equals(accountId) && (accountType.equals(Constants.AccountType.LIABILITY) || accountType.equals(Constants.AccountType.REVENUE))){
                    turnover += Integer.parseInt(tr.getAmount());
                }
            }
            return String.valueOf(turnover);
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo
     */
    public String getAccountBalance(String year, AccountsModel.Account account) {
        String accountType = Service.SCHEMA.getSchemaAccountType(year, account.getSchemaId());
        if (accountType.equals(Constants.AccountType.OFF_BALANCE)){
            throw new IllegalArgumentException("Off-Balance accounts has no balance");
        }
        try {
            String accountId = account.getSchemaId() + "." + account.getSemanticId();
            Integer balance = 0;
            for (TransactionsModel.Transaction tr : new TransactionsManager(year).retrieve().getTransaction()){
                if (tr.getCredit().equals(accountId)){
                    if (accountType.equals(Constants.AccountType.ASSET) || accountType.equals(Constants.AccountType.EXPENSE)) {
                        balance -= Integer.parseInt(tr.getAmount());
                    } else {
                        balance += Integer.parseInt(tr.getAmount());
                    }
                }
                if (tr.getDebit().equals(accountId)){
                    if (accountType.equals(Constants.AccountType.ASSET) || accountType.equals(Constants.AccountType.EXPENSE)) {
                        balance += Integer.parseInt(tr.getAmount());
                    } else {
                        balance -= Integer.parseInt(tr.getAmount());
                    }
                }
            }
            return String.valueOf(balance);
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Composes full id of accumulated depreciation account of specified asset's account.
     */
    public String getAccumulatedDepAccountId(String schemaId, String semanticId) {
        if (!schemaId.startsWith("0") || schemaId.startsWith("09")){
            throw new IllegalArgumentException("Only accounts of class 0 and groups 0-8 have accumulated depreciation accounts");
        }
        return "0" + Constants.Schema.ACCUMULATED_DEP_GROUP_ID + schemaId.substring(1, 2)
                + "." + schemaId.substring(2, 3) + "-" + semanticId;
    }

    /**
     * Composes full id of depreciation account of specified asset's account.
     */
    public String getDepreciationAccountId(String schemaId, String semanticId) {
        if (!schemaId.startsWith("0") || schemaId.startsWith("09")){
            throw new IllegalArgumentException("Only accounts of class 0 and groups 0-8 have depreciation accounts");
        }
        return "5" + Constants.Schema.DEPRECIATION_GROUP_ID + schemaId.substring(1, 2)
                + "." + schemaId.substring(2, 3) + "-" + semanticId;
    }

    /**
     * Composes full id of consumption account of specified resource's account.
     */
    public String getConsumptionAccountId(String schemaId, String semanticId) {
        if (!schemaId.startsWith("1")){
            throw new IllegalArgumentException("Only accounts of class 1 have consumption accounts");
        }
        return "5" + Constants.Schema.CONSUMPTION_GROUP_ID + schemaId.substring(1, 2)
                + "." + schemaId.substring(2, 3) + "-" + semanticId;
    }

    /**
     * todo
     */
    public AccountsModel.Account getAccumulatedDepAccount(String year, AccountsModel.Account account){
        String accDepId = getAccumulatedDepAccountId(account.getSchemaId(), account.getSemanticId());
        try {
            for (AccountsModel.Account acc : new AccountsManager(year).retrieve().getAccount()) {
                if (acc.getFullId().equals(accDepId)) {
                    return acc;
                }
            }
            throw new IllegalArgumentException("Accumulated depreciation account not found for '" + account.getFullId() + "'");
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo
     */
    public AccountsModel.Account getDepreciationAccount(String year, AccountsModel.Account account){
        String depId = getDepreciationAccountId(account.getSchemaId(), account.getSemanticId());
        try {
            for (AccountsModel.Account acc : new AccountsManager(year).retrieve().getAccount()) {
                if (acc.getFullId().equals(depId)) {
                    return acc;
                }
            }
            throw new IllegalArgumentException("Depreciation account not found for '" + account.getFullId() + "'");
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo
     */
    public String getLastDepreciationDate(String year, AccountsModel.Account account){
        try {
            String depAccId = getDepreciationAccountId(account.getSchemaId(), account.getSemanticId());
            String accDepAccId = getAccumulatedDepAccountId(account.getSchemaId(), account.getSemanticId());

            TransactionsModel model = new TransactionsManager(year).retrieve();
            TransactionsModel.Transaction lastDepTransaction = null;
            for (TransactionsModel.Transaction transaction : model.getTransaction()){
                if (transaction.getDebit().equals(depAccId) && transaction.getCredit().equals(accDepAccId)){
                    if (lastDepTransaction == null){
                        lastDepTransaction = transaction;
                    } else {
                        int newTrId = Integer.parseInt(transaction.getId());
                        int lastTrId = Integer.parseInt(lastDepTransaction.getId());
                        lastDepTransaction = (newTrId > lastTrId) ? transaction : lastDepTransaction;
                    }
                }
            }
            if (lastDepTransaction != null){
                return lastDepTransaction.getDate();
            } else {
                return null;
            }
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Creates semantic account according to specified attributes.
     */
    public AccountsModel.Account createAccount(String year, String name, String schemaId, String semanticId, String metadata){
        try {
            AccountsManager accountsManager = new AccountsManager(year);
            AccountsModel accountsModel = accountsManager.retrieve();

            AccountsModel.Account account = new AccountsModel.Account();
            account.setName(name);
            account.setSchemaId(schemaId);
            account.setSemanticId(semanticId);
            account.setMetadata(metadata);
            accountsModel.getAccount().add(account);

            accountsManager.update(accountsModel);
            // TODO: 10/26/17 LOG
            return account;
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns name of semantic account according to specified attributes.
     */
    public String getAccountName(String year, String fullId){
        try {
            AccountsModel accountsModel = new AccountsManager(year).retrieve();
            for (AccountsModel.Account account : accountsModel.getAccount()){
                if (account.getFullId().equals(fullId)){
                    return account.getName();
                }
            }
            throw new IllegalArgumentException("No account found for '"+ fullId + "'");
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }



}
