package org.kaleta.accountant.frontend.common;

import org.kaleta.accountant.backend.model.TransactionsModel;

import java.util.Comparator;

/**
 * descended order by date
 */
public class TransactionComparator implements Comparator<TransactionsModel.Transaction> {
    @Override
    public int compare(TransactionsModel.Transaction tr1, TransactionsModel.Transaction tr2) {
        int monthDiff = Integer.parseInt(tr2.getDate().substring(2,4)) - Integer.parseInt(tr1.getDate().substring(2,4));
        if (monthDiff != 0) {
            return monthDiff;
        } else {
            return Integer.parseInt(tr2.getDate().substring(0,2)) - Integer.parseInt(tr1.getDate().substring(0,2));
        }
    }
}
