package org.kaleta.accountant.service;

/**
 * Provides access to data source which is related to transactions.
 */
public class TransactionsService {

    TransactionsService(){
        // package-private
    }

    /**
     * todo = maybe marge with accounts service
     */
//    public List<TransactionsModel.Transaction> getTransactionsForAccount(String year, String schemaId, String semanticId) {
//        try {
//            TransactionsModel transactionsModel = new TransactionsManager(year).retrieve();
//
//            List<TransactionsModel.Transaction> eligibleTransactions = new ArrayList<>();
//            for (TransactionsModel.Transaction transaction : transactionsModel.getTransaction()) {
//                if (transaction.get.getSchemaId().startsWith(schemaIdPrefix)) {
//                    eligibleTransactions.add(account);
//                }
//            }
//            return eligibleTransactions;
//        } catch (ManagerException e){
//            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
//            throw new ServiceFailureException(e);
//        }
//    }
}
