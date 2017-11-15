package org.kaleta.accountant.service;

import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.backend.manager.Manager;
import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.manager.TransactionsManager;
import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.TransactionsModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.common.ErrorHandler;
import org.kaleta.accountant.frontend.common.AccountPairModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides access to data source which is related to transactions.
 */
public class TransactionsService {

    private TransactionsModel transactionsModel;

    TransactionsService(){
        // package-private
    }

    public void invalidateModel(){
        transactionsModel = null;
    }

    private TransactionsModel getModel(String year) throws ManagerException {
        if (transactionsModel == null) {
            transactionsModel = new TransactionsManager(year).retrieve();
        }
        return new TransactionsModel(transactionsModel);
    }

    /**
     * Adds transaction for specified values.
     */
    public void addTransaction(String year, String date, String amount, String debit, String credit, String description){
        try {
            Manager<TransactionsModel> manager = new TransactionsManager(year);
            TransactionsModel model = manager.retrieve();

            TransactionsModel.Transaction transaction = new TransactionsModel.Transaction();
            transaction.setId(String.valueOf(model.getTransaction().size()));
            transaction.setDate(date);
            transaction.setAmount(amount);
            transaction.setDebit(debit);
            transaction.setCredit(credit);
            transaction.setDescription(description);
            model.getTransaction().add(transaction);

            manager.update(model);
            Initializer.LOG.info("Transaction added: id=" + transaction.getId() + " amount=" + transaction.getAmount()
                    + " debit=" +  transaction.getDebit() + " credit=" + transaction.getCredit() + " description='"
                    + transaction.getDescription() + "'");
            invalidateModel();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns transactions for specified debit and credit.
     * Use 'null' for debit/credit if you want to list transaction only for credit/debit
     */
    public List<TransactionsModel.Transaction> getTransactions(String year, String debit, String credit) {
        try {
            List<TransactionsModel.Transaction> transactionList = new ArrayList<>();
            for (TransactionsModel.Transaction transaction : getModel(year).getTransaction()){
                if (debit == null && credit != null){
                    if (transaction.getCredit().equals(credit)){
                        transactionList.add(transaction);
                    }
                }
                if (debit != null && credit == null){
                    if (transaction.getDebit().equals(debit)){
                        transactionList.add(transaction);
                    }
                }
                if (debit != null && credit != null){
                    if (transaction.getDebit().equals(debit) && transaction.getCredit().equals(credit)){
                        transactionList.add(transaction);
                    }
                }
                if (debit == null && credit == null){
                    transactionList.add(transaction);
                }
            }
            return transactionList;
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns list of descriptions mapped by account pair.
     */
    public Map<AccountPairModel, List<String>> getAccountPairDescriptions(String year){
        try {
            Map<AccountPairModel, List<String>> accountPairDescriptionMap = new HashMap<>();
            for (TransactionsModel.Transaction transaction : getModel(year).getTransaction()){
                AccountPairModel accountPair = new AccountPairModel(transaction.getDebit(), transaction.getCredit());
                accountPairDescriptionMap.computeIfAbsent(accountPair, k -> new ArrayList<>());
                accountPairDescriptionMap.get(accountPair).add(transaction.getDescription());
            }
            return accountPairDescriptionMap;
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns initial value for specified account.
     */
    public String getAccountInitValue(String year, AccountsModel.Account account) {
        String accountType = Service.SCHEMA.getSchemaAccountType(year, account.getSchemaId());
        if (accountType.equals(Constants.AccountType.OFF_BALANCE)
                || accountType.equals(Constants.AccountType.EXPENSE)
                || accountType.equals(Constants.AccountType.REVENUE)){
            throw new IllegalArgumentException("Accounts of type '" + accountType + "' has no initial value!");
        }
        try {
            for (TransactionsModel.Transaction tr : getModel(year).getTransaction()){
                if (accountType.equals(Constants.AccountType.ASSET) || accountType.equals(Constants.AccountType.EXPENSE)){
                    if (tr.getDebit().equals(account.getFullId()) && tr.getCredit().equals(Constants.Account.INIT_ACC_ID)){
                        return tr.getAmount();
                    }
                } else {
                    if (tr.getCredit().equals(account.getFullId()) && tr.getDebit().equals(Constants.Account.INIT_ACC_ID)){
                        return tr.getAmount();
                    }
                }
            }
            throw new IllegalArgumentException("Account id=" + account.getFullId() + " not yet opened!");
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns value of turnover for specified account.
     */
    public String getAccountTurnover(String year, AccountsModel.Account account) {
        String accountType = Service.SCHEMA.getSchemaAccountType(year, account.getSchemaId());
        if (accountType.equals(Constants.AccountType.OFF_BALANCE)){
            throw new IllegalArgumentException("Off-Balance accounts has no turnover!");
        }
        try {
            Integer turnover = 0;
            for (TransactionsModel.Transaction tr : getModel(year).getTransaction()){
                if (accountType.equals(Constants.AccountType.ASSET) || accountType.equals(Constants.AccountType.EXPENSE)){
                    if (tr.getDebit().equals(account.getFullId()) && !tr.getCredit().equals(Constants.Account.INIT_ACC_ID)){
                        turnover += Integer.parseInt(tr.getAmount());
                    }
                } else {
                    if (tr.getCredit().equals(account.getFullId()) && !tr.getDebit().equals(Constants.Account.INIT_ACC_ID)){
                        turnover += Integer.parseInt(tr.getAmount());
                    }
                }
            }
            return String.valueOf(turnover);
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns value of balance for specified account.
     */
    public String getAccountBalance(String year, AccountsModel.Account account) {
        String accountType = Service.SCHEMA.getSchemaAccountType(year, account.getSchemaId());
        if (accountType.equals(Constants.AccountType.OFF_BALANCE)){
            throw new IllegalArgumentException("Off-Balance accounts has no balance!");
        }
        try {
            Integer balance = 0;
            for (TransactionsModel.Transaction tr : getModel(year).getTransaction()){
                if (accountType.equals(Constants.AccountType.ASSET) || accountType.equals(Constants.AccountType.EXPENSE)){
                    if (tr.getDebit().equals(account.getFullId())){
                        balance += Integer.parseInt(tr.getAmount());
                    }
                    if (tr.getCredit().equals(account.getFullId())){
                        if (tr.getDebit().equals(Constants.Account.CLOSING_ACC_ID) || tr.getDebit().equals(Constants.Account.PROFIT_ACC_ID)){
                            return tr.getAmount();
                        }
                        balance -= Integer.parseInt(tr.getAmount());
                    }
                } else {
                    if (tr.getDebit().equals(account.getFullId())){
                        if (tr.getCredit().equals(Constants.Account.CLOSING_ACC_ID) || tr.getCredit().equals(Constants.Account.PROFIT_ACC_ID)){
                            return tr.getAmount();
                        }
                        balance -= Integer.parseInt(tr.getAmount());
                    }
                    if (tr.getCredit().equals(account.getFullId())){
                        balance += Integer.parseInt(tr.getAmount());
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
     * Returns date of last depreciation for specified account.
     */
    public String getLastDepreciationDate(String year, AccountsModel.Account account){
        try {
            String depAccId = Service.ACCOUNT.getDepreciationAccountId(account.getSchemaId(), account.getSemanticId());
            String accDepAccId = Service.ACCOUNT.getAccumulatedDepAccountId(account.getSchemaId(), account.getSemanticId());

            TransactionsModel.Transaction lastDepTransaction = null;
            for (TransactionsModel.Transaction transaction : getModel(year).getTransaction()){
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
}
