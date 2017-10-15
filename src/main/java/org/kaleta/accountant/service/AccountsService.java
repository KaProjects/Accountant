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
import java.util.List;

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
}
