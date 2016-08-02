package org.kaleta.accountant.frontend.dialog.account;

import org.kaleta.accountant.backend.entity.Transaction;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Created by Stanislav Kaleta on 03.06.2016.
 */
public class AccountValueLabel extends JLabel {
    private Font font = new Font(super.getFont().getName(), super.getFont().getStyle(), 15);

    AccountValueLabel(Transaction transaction){
        this.setFont(font);
        this.setHorizontalAlignment(TRAILING);
        this.setText(transaction.getAmount());
        this.setToolTipText(transaction.getDate() + " - " + transaction.getDescription());
    }

    AccountValueLabel(int value, String text){
        this.setFont(font);
        this.setHorizontalAlignment(TRAILING);
        this.setText(String.valueOf(value));
        this.setToolTipText(text);
    }

    AccountValueLabel(){
        this.setFont(font);
        this.setHorizontalAlignment(TRAILING);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(100, 20);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(100, 20);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(100, 20);
    }
}
