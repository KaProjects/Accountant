package org.kaleta.accountant.frontend.core;

import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.common.ErrorHandler;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.listener.OpenAddFinAssetDialog;
import org.kaleta.accountant.frontend.action.listener.OpenFinAssetDialog;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.frontend.component.AccountsEditor;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class FinanceEditor extends JPanel implements Configurable {
    private Configuration configuration;

    private final AccountsEditor accountsEditor;
    private final JPanel ltFinItems;

    public FinanceEditor(Configuration configuration) {
        setConfiguration(configuration);

        accountsEditor = new AccountsEditor(configuration);
        this.resetEditor();

        ltFinItems = new JPanel();
        ltFinItems.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Constants.Color.OVERVIEW_GROUP),
                "Long-Term Financial Assets",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new JLabel().getFont(),
                Constants.Color.OVERVIEW_GROUP));
        ltFinItems.setLayout(new BoxLayout(ltFinItems, BoxLayout.Y_AXIS));
        this.updateLTFinItems();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(accountsEditor);
        panel.add(ltFinItems);

        JScrollPane pane = new JScrollPane(panel);
        this.setLayout(new GridLayout(1, 1));
        this.add(pane);

        this.getActionMap().put(Configuration.SCHEMA_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FinanceEditor.this.resetEditor();
            }
        });
        this.getActionMap().put(Configuration.ACCOUNT_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accountsEditor.updateEditor();
                FinanceEditor.this.updateLTFinItems();
            }
        });
        this.getActionMap().put(Configuration.TRANSACTION_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FinanceEditor.this.updateLTFinItems();
            }
        });
    }

    private void resetEditor() {
        SchemaModel.Class financeClass = Service.SCHEMA.getSchemaClassMap(configuration.getSelectedYear()).get(2);
        financeClass.getGroup().remove(3);
        accountsEditor.resetEditor(financeClass);
    }

    private void updateLTFinItems() {
        ltFinItems.removeAll();

        for (AccountsModel.Account account : Service.ACCOUNT.getAccountsBySchemaId(getConfiguration().getSelectedYear(), "23")) {
            ltFinItems.add(new LongTermFinPanel(account));
        }

        JButton buttonAdd = new JButton("Add Long-Term Financial Asset");
        buttonAdd.addActionListener(new OpenAddFinAssetDialog(this));

        JPanel buttonAddPanel = new JPanel();
        buttonAddPanel.add(buttonAdd);
        ltFinItems.add(buttonAddPanel);

        ltFinItems.revalidate();
        ltFinItems.repaint();
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    private class LongTermFinPanel extends JPanel {
        private final JLabel labelRevaluationValue;
        private final JLabel labelCurrentValue;
        private final JPanel revPanel;
        private final JLabel labelRevInfo;

        private final JButton buttonBuy;
        private final JButton buttonRevalue;
        private final JButton buttonSell;

        private final AccountsModel.Account account;

        LongTermFinPanel(AccountsModel.Account ltfAccount) {
            this.account = ltfAccount;
            this.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            String year = getConfiguration().getSelectedYear();
            Font boldFont = new Font(new JLabel().getFont().getName(), Font.BOLD, 25);
            JLabel labelAccountName = new JLabel(account.getName());
            labelAccountName.setFont(boldFont);
            labelAccountName.setToolTipText(account.getName());
            JLabel labelAccType = new JLabel(">> " + Service.SCHEMA.getAccountName(year, account.getClassId(), account.getGroupId(), account.getSchemaAccountId()));
            labelAccType.setToolTipText("Account Type");

            labelCurrentValue = new JLabel(" --- ", SwingConstants.RIGHT);
            labelCurrentValue.setToolTipText("Current Value");
            labelCurrentValue.setFont(boldFont);

            labelRevaluationValue = new JLabel(" --- ", SwingConstants.RIGHT);
            labelRevaluationValue.setFont(new Font(boldFont.getName(), boldFont.getStyle(), 20));
            labelRevaluationValue.setToolTipText("Revaluation Value");

            JSeparator separator = new JSeparator(SwingConstants.VERTICAL);

            buttonRevalue = new JButton("Revalue");
            buttonRevalue.addActionListener(new OpenFinAssetDialog(FinanceEditor.this, account, OpenFinAssetDialog.REVALUE));

            labelRevInfo = new JLabel();

            revPanel = new JPanel();
            revPanel.setLayout(new BoxLayout(revPanel, BoxLayout.Y_AXIS));
            revPanel.add(buttonRevalue);
            revPanel.add(new JPanel());
            revPanel.add(labelRevInfo);

            JPanel panelSeparator = new JPanel();
            panelSeparator.setOpaque(false);

            buttonBuy = new JButton("Buy");
            buttonBuy.addActionListener(new OpenFinAssetDialog(FinanceEditor.this, account, OpenFinAssetDialog.BUY));

            buttonSell = new JButton("Sell");
            buttonSell.addActionListener(new OpenFinAssetDialog(FinanceEditor.this, account, OpenFinAssetDialog.SELL));

            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(layout.createSequentialGroup().addGap(5)
                    .addGroup(layout.createParallelGroup()
                            .addComponent(labelAccountName, 500, 500, 500)
                            .addComponent(labelAccType, 500, 500, 500))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(labelCurrentValue, 150, 150, 150)
                            .addComponent(labelRevaluationValue, 150, 150, 150))
                    .addGap(10)
                    .addComponent(separator, 5, 5, 5)
                    .addComponent(revPanel, 200, 200, 200)
                    .addComponent(panelSeparator)
                    .addComponent(buttonBuy)
                    .addComponent(buttonSell)
                    .addGap(25));
            layout.setVerticalGroup(layout.createSequentialGroup().addGap(5)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addGroup(layout.createSequentialGroup()
                                    .addComponent(labelAccountName)
                                    .addComponent(labelAccType))
                            .addGroup(layout.createSequentialGroup()
                                    .addComponent(labelCurrentValue)
                                    .addComponent(labelRevaluationValue))
                            .addComponent(separator, 60, 60, 60)
                            .addComponent(revPanel, 50, 50, 50)
                            .addComponent(panelSeparator, 50, 50, 50)
                            .addComponent(buttonBuy)
                            .addComponent(buttonSell))
                    .addGap(5));
            update();
        }

        private void update() {
            String year = getConfiguration().getSelectedYear();
            labelCurrentValue.setText(String.valueOf(Service.TRANSACTIONS.getAccountBalance(year, account)));

            Integer creation = Service.TRANSACTIONS.getAccountBalance(year, Service.ACCOUNT.getFinCreationAccount(year, account));
            Integer revReval = Service.TRANSACTIONS.getAccountBalance(year, Service.ACCOUNT.getFinRevRevaluationAccount(year, account));
            Integer expReval = Service.TRANSACTIONS.getAccountBalance(year, Service.ACCOUNT.getFinExpRevaluationAccount(year, account));
            Integer revaluation = revReval - creation - expReval;

            String prefix = "";
            Color fColor = labelCurrentValue.getForeground();
            if (revaluation != 0) {
                if (revaluation > 0) {
                    fColor = Constants.Color.INCOME_GREEN;
                    prefix = "+";
                } else {
                    fColor = Constants.Color.EXPENSE_RED;
                }
            }
            labelRevaluationValue.setText(prefix + String.valueOf(revaluation));
            labelRevaluationValue.setForeground(fColor);
            if (Service.TRANSACTIONS.financialAssetNeedsRevaluation(year, account)) {
                labelRevaluationValue.setIcon(IconLoader.getIcon(IconLoader.WARNING, new Dimension(20, 20)));
                labelRevaluationValue.setToolTipText("Asset needs revaluation!");
            }

            try {
                String lastRevDate = Service.TRANSACTIONS.getLastRevaluationDate(year, account);
                if (lastRevDate != null) {
                    lastRevDate = "Last Revaluation: " + new SimpleDateFormat("dd.MM.").format(new SimpleDateFormat("ddMM").parse(lastRevDate));
                } else {
                    lastRevDate = "Not yet revalued this year";
                }
                labelRevInfo.setText(lastRevDate);
            } catch (ParseException e) {
                Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
                ErrorHandler.getThrowableDialog(e).setVisible(true);
            }

            FinanceEditor.LongTermFinPanel.this.revalidate();
            FinanceEditor.LongTermFinPanel.this.repaint();
        }
    }
}
