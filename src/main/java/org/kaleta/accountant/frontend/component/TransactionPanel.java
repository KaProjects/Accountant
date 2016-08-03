package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.backend.entity.Transaction;

import javax.swing.*;

/**
 * Created by Stanislav Kaleta on 03.08.2016.
 */
public class TransactionPanel extends JPanel {

    public TransactionPanel(Transaction transaction){
        this.add(new JLabel(transaction.getDescription()));
        initComponents();
    }

    private void initComponents(){
        // TODO: 8/3/16 IMPL.
    }
}
