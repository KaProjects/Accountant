package org.kaleta.accountant.service;

import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.backend.manager.Manager;
import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.manager.TransactionsManager;
import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.TransactionsModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.common.ErrorHandler;
import org.kaleta.accountant.common.Utils;
import org.kaleta.accountant.frontend.common.AccountPairModel;

import java.util.*;

/**
 * Provides access to data source which is related to transactions.
 */
public class TransactionsService {

    private TransactionsModel transactionsModel;

    TransactionsService() {
        // package-private
    }

    public void invalidateModel() {
        transactionsModel = null;
    }

    private TransactionsModel getModel(String year) throws ManagerException {
        if (transactionsModel == null) {
            transactionsModel = new TransactionsManager(year).retrieve();
        }
        if (!transactionsModel.getYear().equals(year)) {
            transactionsModel = new TransactionsManager(year).retrieve();
        }
        return new TransactionsModel(transactionsModel);
    }

    /**
     * Adds transaction for specified values.
     */
    public void addTransaction(String year, String date, String amount, String debit, String credit, String description) {
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
                    + " debit=" + transaction.getDebit() + " credit=" + transaction.getCredit() + " description='"
                    + transaction.getDescription() + "'");
            invalidateModel();
        } catch (ManagerException e) {
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
            for (TransactionsModel.Transaction transaction : getModel(year).getTransaction()) {
                if (debit == null && credit != null) {
                    if (transaction.getCredit().equals(credit)) {
                        transactionList.add(transaction);
                    }
                }
                if (debit != null && credit == null) {
                    if (transaction.getDebit().equals(debit)) {
                        transactionList.add(transaction);
                    }
                }
                if (debit != null && credit != null) {
                    if (transaction.getDebit().equals(debit) && transaction.getCredit().equals(credit)) {
                        transactionList.add(transaction);
                    }
                }
                if (debit == null && credit == null) {
                    transactionList.add(transaction);
                }
            }
            return transactionList;
        } catch (ManagerException e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns transactions for account specified by full id.
     */
    public List<TransactionsModel.Transaction> getTransactionsForAccount(String year, String accountFullId) {
        try {
            List<TransactionsModel.Transaction> transactionList = new ArrayList<>();
            for (TransactionsModel.Transaction transaction : getModel(year).getTransaction()) {
                if (transaction.getCredit().equals(accountFullId) || transaction.getDebit().equals(accountFullId)) {
                    transactionList.add(transaction);
                }
            }
            return transactionList;
        } catch (ManagerException e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns list of descriptions mapped by account pair.
     */
    public Map<AccountPairModel, Set<String>> getAccountPairDescriptions(String year) {
        try {
            Map<AccountPairModel, Set<String>> accountPairDescriptionMap = new HashMap<>();
            for (TransactionsModel.Transaction transaction : getModel(year).getTransaction()) {
                AccountPairModel accountPair = new AccountPairModel(transaction.getDebit(), transaction.getCredit());
                accountPairDescriptionMap.computeIfAbsent(accountPair, k -> new HashSet<>());
                accountPairDescriptionMap.get(accountPair).add(transaction.getDescription());
            }
            return accountPairDescriptionMap;
        } catch (ManagerException e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns initial value for specified account.
     */
    public Integer getAccountInitialValue(String year, AccountsModel.Account account) {
        String accountType = Service.SCHEMA.getSchemaAccountType(year, account.getSchemaId());
        if (accountType.equals(Constants.AccountType.OFF_BALANCE)
                || accountType.equals(Constants.AccountType.EXPENSE)
                || accountType.equals(Constants.AccountType.REVENUE)) {
            throw new IllegalArgumentException("Accounts of type '" + accountType + "' has no initial value!");
        }
        try {
            for (TransactionsModel.Transaction tr : getModel(year).getTransaction()) {
                if (accountType.equals(Constants.AccountType.ASSET) || accountType.equals(Constants.AccountType.EXPENSE)) {
                    if (tr.getDebit().equals(account.getFullId()) && tr.getCredit().equals(Constants.Account.INIT_ACC_ID)) {
                        return Integer.valueOf(tr.getAmount());
                    }
                } else {
                    if (tr.getCredit().equals(account.getFullId()) && tr.getDebit().equals(Constants.Account.INIT_ACC_ID)) {
                        return Integer.valueOf(tr.getAmount());
                    }
                }
            }
            throw new IllegalArgumentException("Account id=" + account.getFullId() + " not yet opened!");
        } catch (ManagerException e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns mutual initial value for specified list of accounts.
     */
    public Integer getAccountListInitialValue(String year, List<AccountsModel.Account> accountList) {
        int initialValue = 0;
        for (AccountsModel.Account account : accountList) {
            initialValue += this.getAccountInitialValue(year, account);
        }
        return initialValue;
    }

    /**
     * Returns mutual initial value for accounts matching specified schema ID prefix(es).
     */
    public Integer getSchemaIdPrefixInitialValue(String year, String... schemaIdPrefixes) {
        int initialValue = 0;
        for (String schemaIdPrefix : schemaIdPrefixes) {
            initialValue += this.getAccountListInitialValue(year, Service.ACCOUNT.getAccountsBySchemaId(year, schemaIdPrefix));
        }
        return initialValue;
    }

    /**
     * Returns value of turnover for specified account.
     */
    public String getAccountTurnover(String year, AccountsModel.Account account) {
        String accountType = Service.SCHEMA.getSchemaAccountType(year, account.getSchemaId());
        if (accountType.equals(Constants.AccountType.OFF_BALANCE)) {
            throw new IllegalArgumentException("Off-Balance accounts has no turnover!");
        }
        try {
            Integer turnover = 0;
            for (TransactionsModel.Transaction tr : getModel(year).getTransaction()) {
                if (accountType.equals(Constants.AccountType.ASSET) || accountType.equals(Constants.AccountType.EXPENSE)) {
                    if (tr.getDebit().equals(account.getFullId()) && !tr.getCredit().equals(Constants.Account.INIT_ACC_ID)) {
                        turnover += Integer.parseInt(tr.getAmount());
                    }
                } else {
                    if (tr.getCredit().equals(account.getFullId()) && !tr.getDebit().equals(Constants.Account.INIT_ACC_ID)) {
                        turnover += Integer.parseInt(tr.getAmount());
                    }
                }
            }
            return String.valueOf(turnover);
        } catch (ManagerException e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns value of balance for specified account.
     */
    public Integer getAccountBalance(String year, AccountsModel.Account account) {
        String accountType = Service.SCHEMA.getSchemaAccountType(year, account.getSchemaId());
        if (accountType.equals(Constants.AccountType.OFF_BALANCE)) {
            throw new IllegalArgumentException("Off-Balance accounts has no balance!");
        }
        try {
            Integer balance = 0;
            for (TransactionsModel.Transaction tr : getModel(year).getTransaction()) {
                if (accountType.equals(Constants.AccountType.ASSET) || accountType.equals(Constants.AccountType.EXPENSE)) {
                    if (tr.getDebit().equals(account.getFullId())) {
                        balance += Integer.parseInt(tr.getAmount());
                    }
                    if (tr.getCredit().equals(account.getFullId())) {
                        if (tr.getDebit().equals(Constants.Account.CLOSING_ACC_ID) || tr.getDebit().equals(Constants.Account.PROFIT_ACC_ID)) {
                            return Integer.parseInt(tr.getAmount());
                        }
                        balance -= Integer.parseInt(tr.getAmount());
                    }
                } else {
                    if (tr.getDebit().equals(account.getFullId())) {
                        if (tr.getCredit().equals(Constants.Account.CLOSING_ACC_ID) || tr.getCredit().equals(Constants.Account.PROFIT_ACC_ID)) {
                            return Integer.parseInt(tr.getAmount());
                        }
                        balance -= Integer.parseInt(tr.getAmount());
                    }
                    if (tr.getCredit().equals(account.getFullId())) {
                        balance += Integer.parseInt(tr.getAmount());
                    }
                }
            }
            return balance;
        } catch (ManagerException e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns value of mutual balance for specified list of accounts.
     */
    public Integer getAccountListBalance(String year, List<AccountsModel.Account> accountList) {
        int balance = 0;
        for (AccountsModel.Account account : accountList) {
            balance += this.getAccountBalance(year, account);
        }
        return balance;
    }

    /**
     * Returns value of mutual balance for accounts matching specified schema ID prefix(es).
     */
    public Integer getSchemaIdPrefixBalance(String year, String... schemaIdPrefixes) {
        int balance = 0;
        for (String schemaIdPrefix : schemaIdPrefixes) {
            balance += this.getAccountListBalance(year, Service.ACCOUNT.getAccountsBySchemaId(year, schemaIdPrefix));
        }
        return balance;
    }

    /**
     * Returns value of monthly balance for specified account.
     */
    public Integer[] getAccountMonthlyBalance(String year, AccountsModel.Account account) {
        String accountType = Service.SCHEMA.getSchemaAccountType(year, account.getSchemaId());
        if (accountType.equals(Constants.AccountType.OFF_BALANCE)) {
            throw new IllegalArgumentException("Off-Balance accounts has no balance!");
        }
        try {
            Integer[] monthlyBalance = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            for (TransactionsModel.Transaction tr : getModel(year).getTransaction()) {
                if (tr.getDebit().equals(Constants.Account.CLOSING_ACC_ID)
                        || tr.getDebit().equals(Constants.Account.PROFIT_ACC_ID)
                        || tr.getDebit().equals(Constants.Account.INIT_ACC_ID)
                        || tr.getCredit().equals(Constants.Account.CLOSING_ACC_ID)
                        || tr.getCredit().equals(Constants.Account.PROFIT_ACC_ID)
                        || tr.getCredit().equals(Constants.Account.INIT_ACC_ID)) {
                    continue;
                }
                if (accountType.equals(Constants.AccountType.ASSET) || accountType.equals(Constants.AccountType.EXPENSE)) {
                    if (tr.getDebit().equals(account.getFullId())) {
                        int month = Integer.parseInt(tr.getDate().substring(2, 4));
                        monthlyBalance[month - 1] += Integer.parseInt(tr.getAmount());
                    }
                    if (tr.getCredit().equals(account.getFullId())) {
                        int month = Integer.parseInt(tr.getDate().substring(2, 4));
                        monthlyBalance[month - 1] -= Integer.parseInt(tr.getAmount());
                    }
                } else {
                    if (tr.getDebit().equals(account.getFullId())) {
                        int month = Integer.parseInt(tr.getDate().substring(2, 4));
                        monthlyBalance[month - 1] -= Integer.parseInt(tr.getAmount());
                    }
                    if (tr.getCredit().equals(account.getFullId())) {
                        int month = Integer.parseInt(tr.getDate().substring(2, 4));
                        monthlyBalance[month - 1] += Integer.parseInt(tr.getAmount());
                    }
                }
            }
            return monthlyBalance;
        } catch (ManagerException e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns value of mutual monthly balance for specified list of accounts.
     */
    public Integer[] getMonthlyAccountListBalance(String year, List<AccountsModel.Account> accountList) {
        Integer[] monthlyBalance = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (AccountsModel.Account account : accountList) {
            monthlyBalance = Utils.mergeArrays(monthlyBalance, Service.TRANSACTIONS.getAccountMonthlyBalance(year, account));
        }
        return monthlyBalance;
    }

    /**
     * Returns value of mutual monthly balance for accounts matching specified schema ID prefix(es).
     */
    public Integer[] getMonthlySchemaIdPrefixBalance(String year, String... schemaIdPrefixes) {
        Integer[] monthlyBalance = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (String schemaIdPrefix : schemaIdPrefixes) {
            Integer[] thisBalance = Service.TRANSACTIONS.getMonthlyAccountListBalance(year, Service.ACCOUNT.getAccountsBySchemaId(year, schemaIdPrefix));
            monthlyBalance = Utils.mergeArrays(monthlyBalance, thisBalance);
        }
        return monthlyBalance;
    }

    /**
     * Returns value of monthly balance for specified account.
     */
    public Integer[] getAccountMonthlyCumulativeBalance(String year, AccountsModel.Account account) {
        String accountType = Service.SCHEMA.getSchemaAccountType(year, account.getSchemaId());
        if (accountType.equals(Constants.AccountType.OFF_BALANCE)) {
            throw new IllegalArgumentException("Off-Balance accounts has no balance!");
        }
        try {
            Integer[] monthlyBalance = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            for (TransactionsModel.Transaction tr : getModel(year).getTransaction()) {
                if (tr.getDebit().equals(Constants.Account.CLOSING_ACC_ID)
                        || tr.getDebit().equals(Constants.Account.PROFIT_ACC_ID)
                        || tr.getCredit().equals(Constants.Account.CLOSING_ACC_ID)
                        || tr.getCredit().equals(Constants.Account.PROFIT_ACC_ID)) {
                    continue;
                }
                if (accountType.equals(Constants.AccountType.ASSET) || accountType.equals(Constants.AccountType.EXPENSE)) {
                    if (tr.getDebit().equals(account.getFullId())) {
                        int month = Integer.parseInt(tr.getDate().substring(2, 4));
                        monthlyBalance[month - 1] += Integer.parseInt(tr.getAmount());
                    }
                    if (tr.getCredit().equals(account.getFullId())) {
                        int month = Integer.parseInt(tr.getDate().substring(2, 4));
                        monthlyBalance[month - 1] -= Integer.parseInt(tr.getAmount());
                    }
                } else {
                    if (tr.getDebit().equals(account.getFullId())) {
                        int month = Integer.parseInt(tr.getDate().substring(2, 4));
                        monthlyBalance[month - 1] -= Integer.parseInt(tr.getAmount());
                    }
                    if (tr.getCredit().equals(account.getFullId())) {
                        int month = Integer.parseInt(tr.getDate().substring(2, 4));
                        monthlyBalance[month - 1] += Integer.parseInt(tr.getAmount());
                    }
                }
            }

            for (int i=1;i<12;i++){
                monthlyBalance[i] += monthlyBalance[i - 1];
            }

            return monthlyBalance;
        } catch (ManagerException e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns value of mutual monthly cumulative balance for specified list of accounts.
     */
    public Integer[] getMonthlyAccountListCumulativeBalance(String year, List<AccountsModel.Account> accountList) {
        Integer[] monthlyBalance = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (AccountsModel.Account account : accountList) {
            monthlyBalance = Utils.mergeArrays(monthlyBalance, Service.TRANSACTIONS.getAccountMonthlyCumulativeBalance(year, account));
        }
        return monthlyBalance;
    }

    /**
     * Returns value of mutual monthly cumulative balance for accounts matching specified schema ID prefix(es).
     */
    public Integer[] getMonthlySchemaIdPrefixCumulativeBalance(String year, String... schemaIdPrefixes) {
        Integer[] monthlyBalance = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (String schemaIdPrefix : schemaIdPrefixes) {
            Integer[] thisBalance = Service.TRANSACTIONS.getMonthlyAccountListCumulativeBalance(year, Service.ACCOUNT.getAccountsBySchemaId(year, schemaIdPrefix));
            monthlyBalance = Utils.mergeArrays(monthlyBalance, thisBalance);
        }
        return monthlyBalance;
    }

    /**
     * Returns value of monthly turnover for specified account.
     */
    public Integer[] getAccountMonthlyTurnover(String year, AccountsModel.Account account) {
        String accountType = Service.SCHEMA.getSchemaAccountType(year, account.getSchemaId());
        if (accountType.equals(Constants.AccountType.OFF_BALANCE)) {
            throw new IllegalArgumentException("Off-Balance accounts has no balance!");
        }
        try {
            Integer[] monthlyBalance = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            for (TransactionsModel.Transaction tr : getModel(year).getTransaction()) {
                if (tr.getDebit().equals(Constants.Account.CLOSING_ACC_ID)
                        || tr.getDebit().equals(Constants.Account.PROFIT_ACC_ID)
                        || tr.getDebit().equals(Constants.Account.INIT_ACC_ID)
                        || tr.getCredit().equals(Constants.Account.CLOSING_ACC_ID)
                        || tr.getCredit().equals(Constants.Account.PROFIT_ACC_ID)
                        || tr.getCredit().equals(Constants.Account.INIT_ACC_ID)) {
                    continue;
                }
                if (accountType.equals(Constants.AccountType.ASSET) || accountType.equals(Constants.AccountType.EXPENSE)) {
                    if (tr.getDebit().equals(account.getFullId())) {
                        int month = Integer.parseInt(tr.getDate().substring(2, 4));
                        monthlyBalance[month - 1] += Integer.parseInt(tr.getAmount());
                    }
                } else {
                    if (tr.getCredit().equals(account.getFullId())) {
                        int month = Integer.parseInt(tr.getDate().substring(2, 4));
                        monthlyBalance[month - 1] += Integer.parseInt(tr.getAmount());
                    }
                }
            }
            return monthlyBalance;
        } catch (ManagerException e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns value of mutual monthly turnover for specified list of accounts.
     */
    public Integer[] getMonthlyAccountListTurnover(String year, List<AccountsModel.Account> accountList) {
        Integer[] monthlyBalance = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (AccountsModel.Account account : accountList) {
            monthlyBalance = Utils.mergeArrays(monthlyBalance, Service.TRANSACTIONS.getAccountMonthlyTurnover(year, account));
        }
        return monthlyBalance;
    }

    /**
     * Returns value of mutual monthly turnover for accounts matching specified schema ID prefix(es).
     */
    public Integer[] getMonthlySchemaIdPrefixTurnover(String year, String... schemaIdPrefixes) {
        Integer[] monthlyBalance = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (String schemaIdPrefix : schemaIdPrefixes) {
            Integer[] thisBalance = Service.TRANSACTIONS.getMonthlyAccountListTurnover(year, Service.ACCOUNT.getAccountsBySchemaId(year, schemaIdPrefix));
            monthlyBalance = Utils.mergeArrays(monthlyBalance, thisBalance);
        }
        return monthlyBalance;
    }

    /**
     * Transfers profit value from profit account to closing balance account.
     * Note: must be used only after closing all the accounts!
     */
    public void resolveProfit(String year, String date, AccountsModel.Account profitAccount) {
        Integer profitBalance = 0;
        try {
            for (TransactionsModel.Transaction tr : getModel(year).getTransaction()) {
                if (tr.getDebit().equals(profitAccount.getFullId())) {
                    profitBalance -= Integer.parseInt(tr.getAmount());
                }
                if (tr.getCredit().equals(profitAccount.getFullId())) {
                    profitBalance += Integer.parseInt(tr.getAmount());
                }
            }
        } catch (ManagerException e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
        addTransaction(year, date, String.valueOf(profitBalance),
                Constants.Account.PROFIT_ACC_ID, Constants.Account.CLOSING_ACC_ID,
                "profit balance -> closing balance");
    }

    /**
     * Returns date of last depreciation for specified account.
     */
    public String getLastDepreciationDate(String year, AccountsModel.Account account) {
        try {
            String depAccId = Service.ACCOUNT.getDepreciationAccountId(account.getSchemaId(), account.getSemanticId());
            String accDepAccId = Service.ACCOUNT.getAccumulatedDepAccountId(account.getSchemaId(), account.getSemanticId());

            TransactionsModel.Transaction lastDepTransaction = null;
            for (TransactionsModel.Transaction transaction : getModel(year).getTransaction()) {
                if (transaction.getDebit().equals(depAccId) && transaction.getCredit().equals(accDepAccId)) {
                    if (lastDepTransaction == null) {
                        lastDepTransaction = transaction;
                    } else {
                        int newTrId = Integer.parseInt(transaction.getId());
                        int lastTrId = Integer.parseInt(lastDepTransaction.getId());
                        lastDepTransaction = (newTrId > lastTrId) ? transaction : lastDepTransaction;
                    }
                }
            }
            if (lastDepTransaction != null) {
                return lastDepTransaction.getDate();
            } else {
                return null;
            }
        } catch (ManagerException e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns true if specified financial account needs revaluation, false otherwise.
     * Note: an account needs revaluation if no revaluation was done after purchasing new assets.
     */
    public boolean financialAssetNeedsRevaluation(String year, AccountsModel.Account account) {
        if (!account.getSchemaId().startsWith("23")) {
            throw new IllegalArgumentException("Only accounts of 23x are considered for revaluation");
        }
        String creationAccId = Service.ACCOUNT.getFinCreationAccountId(account.getSchemaId(), account.getSemanticId());
        String revRevAccId = Service.ACCOUNT.getFinRevRevaluationAccountId(account.getSchemaId(), account.getSemanticId());
        String expRevAccId = Service.ACCOUNT.getFinExpRevaluationAccountId(account.getSchemaId(), account.getSemanticId());

        TransactionsModel.Transaction lastCreationTransaction = null;
        TransactionsModel.Transaction lastRevTransaction = null;
        try {
            for (TransactionsModel.Transaction transaction : getModel(year).getTransaction()) {
                if (transaction.getDebit().equals(creationAccId)) {
                    if (lastCreationTransaction == null) {
                        lastCreationTransaction = transaction;
                    } else {
                        int newTrId = Integer.parseInt(transaction.getId());
                        int lastTrId = Integer.parseInt(lastCreationTransaction.getId());
                        lastCreationTransaction = (newTrId > lastTrId) ? transaction : lastCreationTransaction;
                    }
                }
                if (transaction.getDebit().equals(expRevAccId) || transaction.getCredit().equals(revRevAccId)) {
                    if (lastRevTransaction == null) {
                        lastRevTransaction = transaction;
                    } else {
                        int newTrId = Integer.parseInt(transaction.getId());
                        int lastTrId = Integer.parseInt(lastRevTransaction.getId());
                        lastRevTransaction = (newTrId > lastTrId) ? transaction : lastRevTransaction;
                    }
                }
            }
        } catch (ManagerException e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }

        if (lastCreationTransaction == null) return false;
        if (lastRevTransaction == null) return true;
        return (Integer.parseInt(lastCreationTransaction.getId()) > Integer.parseInt(lastRevTransaction.getId()));
    }

    /**
     * Returns date of last revaluation for specified account.
     */
    public String getLastRevaluationDate(String year, AccountsModel.Account account) {
        try {
            String revRevAccId = Service.ACCOUNT.getFinRevRevaluationAccountId(account.getSchemaId(), account.getSemanticId());
            String expRevAccId = Service.ACCOUNT.getFinExpRevaluationAccountId(account.getSchemaId(), account.getSemanticId());

            TransactionsModel.Transaction lastDepTransaction = null;
            for (TransactionsModel.Transaction transaction : getModel(year).getTransaction()) {
                if (transaction.getDebit().equals(expRevAccId) || transaction.getCredit().equals(revRevAccId)) {
                    if (lastDepTransaction == null) {
                        lastDepTransaction = transaction;
                    } else {
                        int newTrId = Integer.parseInt(transaction.getId());
                        int lastTrId = Integer.parseInt(lastDepTransaction.getId());
                        lastDepTransaction = (newTrId > lastTrId) ? transaction : lastDepTransaction;
                    }
                }
            }
            if (lastDepTransaction != null) {
                return lastDepTransaction.getDate();
            } else {
                return null;
            }
        } catch (ManagerException e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }
}
