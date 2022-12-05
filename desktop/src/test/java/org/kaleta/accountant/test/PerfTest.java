package org.kaleta.accountant.test;

import org.junit.Ignore;
import org.junit.Test;
import org.kaleta.accountant.core.TestParent;
import org.kaleta.accountant.service.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class PerfTest extends TestParent {

    @Test
    @Ignore // not a unit test
    public void testMaxTransactionAmount() {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        long startTime = System.currentTimeMillis();

        for (Short i = 0; i < Short.MAX_VALUE; i++) {
            Service.TRANSACTIONS.addTransaction(YEAR, "0101", String.valueOf(i), "123", "456", "test");
        }
        long endTime = System.currentTimeMillis();
        System.out.println(Service.TRANSACTIONS.getTransactions(YEAR, null, null).size());
        long finishTime = System.currentTimeMillis();

        System.out.println("RESULTS");
        System.out.println("#################");
        System.out.println(df.format(startTime));
        System.out.println(df.format(endTime));
        System.out.println(df.format(finishTime));
        System.out.println("#################");
    }
}
