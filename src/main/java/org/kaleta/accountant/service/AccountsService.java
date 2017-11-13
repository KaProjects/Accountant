package org.kaleta.accountant.service;

import org.kaleta.accountant.backend.manager.AccountsManager;
import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.model.AccountsModel;
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

    private AccountsModel accountsModel;

    AccountsService(){
        // package-private
    }

    private AccountsModel getModel(String year) throws ManagerException {
        if (accountsModel == null) {
            accountsModel = new AccountsManager(year).retrieve();
        }
        return new AccountsModel(accountsModel);
    }

    /**
     * Returns list of accounts for specified schema id prefix.
     */
    public List<AccountsModel.Account> getAccountsBySchemaId(String year, String schemaIdPrefix){
        try {
            List<AccountsModel.Account> eligibleAccounts = new ArrayList<>();
            for (AccountsModel.Account account : getModel(year).getAccount()) {
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
     * Returns accounts mapped according to schema id.
     */
    public Map<String, List<AccountsModel.Account>> getAccountsViaSchemaMap(String year){
        try {
            Map<String, List<AccountsModel.Account>> accountMap = new HashMap<>();
            for (AccountsModel.Account account : getModel(year).getAccount()){
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
     * Composes full id of accumulated depreciation account of specified asset account.
     */
    public String getAccumulatedDepAccountId(String schemaId, String semanticId) {
        if (!schemaId.startsWith("0") || schemaId.startsWith("09")){
            throw new IllegalArgumentException("Only accounts of class 0 and groups 0-8 have accumulated depreciation accounts");
        }
        return "0" + Constants.Schema.ACCUMULATED_DEP_GROUP_ID + schemaId.substring(1, 2)
                + "." + schemaId.substring(2, 3) + "-" + semanticId;
    }

    /**
     * Composes full id of depreciation account of specified asset account.
     */
    public String getDepreciationAccountId(String schemaId, String semanticId) {
        if (!schemaId.startsWith("0") || schemaId.startsWith("09")){
            throw new IllegalArgumentException("Only accounts of class 0 and groups 0-8 have depreciation accounts");
        }
        return "5" + Constants.Schema.DEPRECIATION_GROUP_ID + schemaId.substring(1, 2)
                + "." + schemaId.substring(2, 3) + "-" + semanticId;
    }

    /**
     * Composes full id of consumption account of specified resource account.
     */
    public String getConsumptionAccountId(String schemaId, String semanticId) {
        if (!schemaId.startsWith("1")){
            throw new IllegalArgumentException("Only accounts of class 1 have consumption accounts");
        }
        return "5" + Constants.Schema.CONSUMPTION_GROUP_ID + schemaId.substring(1, 2)
                + "." + schemaId.substring(2, 3) + "-" + semanticId;
    }

    /**
     * Returns accumulated depreciation account for specified asset account.
     */
    public AccountsModel.Account getAccumulatedDepAccount(String year, AccountsModel.Account account){
        String accDepId = getAccumulatedDepAccountId(account.getSchemaId(), account.getSemanticId());
        try {
            for (AccountsModel.Account acc : getModel(year).getAccount()) {
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
     * Returns depreciation account for specified asset account.
     */
    public AccountsModel.Account getDepreciationAccount(String year, AccountsModel.Account account){
        String depId = getDepreciationAccountId(account.getSchemaId(), account.getSemanticId());
        try {
            for (AccountsModel.Account acc : getModel(year).getAccount()) {
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
     * Creates semantic account according to specified attributes.
     */
    public AccountsModel.Account createAccount(String year, String name, String schemaId, String semanticId, String metadata){
        try {
            AccountsManager manager = new AccountsManager(year);
            AccountsModel model = manager.retrieve();

            AccountsModel.Account account = new AccountsModel.Account();
            account.setName(name);
            account.setSchemaId(schemaId);
            account.setSemanticId(semanticId);
            account.setMetadata(metadata);
            model.getAccount().add(account);

            manager.update(model);
            Initializer.LOG.info("Account created: id=" + schemaId + "." + semanticId + " name='" + name + "'");
            this.accountsModel = model;
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
            for (AccountsModel.Account account : getModel(year).getAccount()){
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
