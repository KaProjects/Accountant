package org.kaleta.accountant.service;

import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.manager.TransactionsManager;
import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.TransactionsModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.common.ErrorHandler;
import org.kaleta.accountant.frontend.Initializer;
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
            TransactionsManager manager = new TransactionsManager(year);
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
            Initializer.LOG.info("Transaction created: id=" + transaction.getId() + " amount=" + transaction.getAmount()
                    + " debit=" +  transaction.getDebit() + " credit=" + transaction.getCredit() + " description='"
                    + transaction.getDescription() + "'");
            this.transactionsModel = model;
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
     * Returns value of turnover for specified account.
     */
    public String getAccountTurnover(String year, AccountsModel.Account account) {
        String accountType = Service.SCHEMA.getSchemaAccountType(year, account.getSchemaId());
        if (accountType.equals(Constants.AccountType.OFF_BALANCE)){
            throw new IllegalArgumentException("Off-Balance accounts has no balance");
        }
        try {
            String accountId = account.getSchemaId() + "." + account.getSemanticId();
            Integer turnover = 0;
            for (TransactionsModel.Transaction tr : getModel(year).getTransaction()){
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
     * Returns value of balance for specified account.
     */
    public String getAccountBalance(String year, AccountsModel.Account account) {
        String accountType = Service.SCHEMA.getSchemaAccountType(year, account.getSchemaId());
        if (accountType.equals(Constants.AccountType.OFF_BALANCE)){
            throw new IllegalArgumentException("Off-Balance accounts has no balance");
        }
        try {
            String accountId = account.getSchemaId() + "." + account.getSemanticId();
            Integer balance = 0;
            for (TransactionsModel.Transaction tr : getModel(year).getTransaction()){
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
