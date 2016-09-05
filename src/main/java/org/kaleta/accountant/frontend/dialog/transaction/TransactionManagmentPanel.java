package org.kaleta.accountant.frontend.dialog.transaction;

import javax.swing.*;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 05.09.2016.
 */
public class TransactionManagmentPanel extends JPanel {
    private List<TransactionPanel> transactionList;


    // TODO del icon + transaction panel
    // add tr. mngment ...





    public void setTransactionList(List<TransactionPanel> transactionList) {
        this.transactionList = transactionList;
    }
    public List<TransactionPanel> getTransactionList() {
        return transactionList;
    }
}
