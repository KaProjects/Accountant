package org.kaleta.accountant.backend.comparator;

import org.kaleta.accountant.backend.entity.Transaction;

import java.util.Comparator;

/**
 * Created by Stanislav Kaleta on 08.08.2016.
 */
public class TransactionComparator implements Comparator<Transaction> {
    @Override
    public int compare(Transaction t0, Transaction t1) {
        int m0 = Integer.parseInt(t0.getDate().substring(2,4));
        int m1 = Integer.parseInt(t1.getDate().substring(2,4));
        if (m0 != m1){
            return m0 - m1;
        }
        int d0 = Integer.parseInt(t0.getDate().substring(0,2));
        int d1 = Integer.parseInt(t1.getDate().substring(0,2));
        if (d0 != d1){
            return d0 - d1;
        }
        return Integer.parseInt(t0.getId()) - Integer.parseInt(t1.getId());
    }
}
