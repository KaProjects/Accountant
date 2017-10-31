package org.kaleta.accountant.frontend.dep.dialog.transaction;

import org.kaleta.accountant.backend.entity.Transaction;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.IconLoader;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Stanislav Kaleta on 05.09.2016.
 */
@Deprecated
public class TransactionManagementPanel extends JPanel {
    private List<TransactionPanel> transactionPanelList;
    private Configuration config;
    private JButton okButton;

    public TransactionManagementPanel(Configuration config){
        this.config = config;
        transactionPanelList = new ArrayList<>();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    private void updateComponents(){
        this.removeAll();
        this.repaint();
        this.revalidate();

        JPanel panelLabels = new JPanel();
        JLabel labelDate = new JLabel("Date:");
        JLabel labelDescription = new JLabel("Description:");
        JLabel labelAmount = new JLabel("Amount:");
        JLabel labelDebit = new JLabel("Debit:");
        JLabel labelCredit = new JLabel("Credit:");
        GroupLayout layoutLabels = new GroupLayout(panelLabels);
        panelLabels.setLayout(layoutLabels);
        layoutLabels.setHorizontalGroup(layoutLabels.createSequentialGroup()
                .addGap(15)
                .addGap(5).addComponent(labelDate,50,50,50)
                .addGap(5).addComponent(labelDescription,200,200,Short.MAX_VALUE)
                .addGap(5).addComponent(labelAmount,75,75,75)
                .addGap(5).addComponent(labelDebit,50,50,50)
                .addGap(5).addComponent(labelCredit,50,50,50)
                .addGap(5));
        layoutLabels.setVerticalGroup(layoutLabels.createParallelGroup()
                .addComponent(labelDate).addComponent(labelDescription).addComponent(labelAmount).addComponent(labelDebit).addComponent(labelCredit));
        this.add(panelLabels);

        for (TransactionPanel transactionPanel : transactionPanelList){

            JButton buttonDel = new JButton(IconLoader.getIcon(IconLoader.DELETE, "Remove Transaction", new Dimension(10,10)));
            buttonDel.addActionListener(l -> {
                transactionPanelList.remove(transactionPanel);
                updateComponents();
                if (transactionPanelList.size() == 0){
                    okButton.setEnabled(false);
                }
                if (transactionPanelList.size() == 1){
                    okButton.setText("Add");
                }
            });

            JPanel panel = new JPanel();
            GroupLayout layout = new GroupLayout(panel);
            panel.setLayout(layout);
            layout.setVerticalGroup(layout.createParallelGroup().addComponent(buttonDel,35,35,35).addComponent(transactionPanel));
            layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(buttonDel,15,15,15).addComponent(transactionPanel));

            this.add(panel);
        }
    }

    public void setOkButton(JButton button){
        this.okButton = button;
    }

    public void addNewTransaction(boolean isActive){
        TransactionPanel transactionPanel = new TransactionPanel(config);
        transactionPanel.toggleActive(isActive);
        transactionPanelList.add(transactionPanel);
        updateComponents();
        if (transactionPanelList.size() > 1){
            okButton.setText("Add All");
        } else {
            okButton.setText("Add");
        }
    }

    public void setTransactions(List<Transaction> transactionList) {
        transactionPanelList.clear();
        for (Transaction transaction : transactionList){
            TransactionPanel transactionPanel = new TransactionPanel(config);
            transactionPanel.setTransaction(transaction);
            transactionPanelList.add(transactionPanel);
        }
        updateComponents();
    }
    public List<Transaction> getTransactions() {
        return transactionPanelList.stream().map(TransactionPanel::getTransaction).collect(Collectors.toList());
    }
}
