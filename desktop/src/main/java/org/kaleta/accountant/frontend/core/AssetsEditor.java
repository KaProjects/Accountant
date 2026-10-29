package org.kaleta.accountant.frontend.core;

import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.backend.model.TransactionsModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.common.ErrorHandler;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.listener.AttachInvoiceAction;
import org.kaleta.accountant.frontend.action.listener.OpenAddAssetDialog;
import org.kaleta.accountant.frontend.action.listener.OpenDepreciateDialog;
import org.kaleta.accountant.frontend.action.listener.OpenExcludeDialog;
import org.kaleta.accountant.frontend.action.listener.RenameAccountAction;
import org.kaleta.accountant.frontend.action.listener.RevealInvoiceAction;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.frontend.component.LeadingIconButton;
import org.kaleta.accountant.frontend.component.card.CardStyle;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class AssetsEditor extends JPanel implements Configurable {

    private static final String REVEAL_INVOICE_LABEL = "Invoice";
    private static final String ATTACH_INVOICE_LABEL = "Add Invoice";
    private static final int INVOICE_ICON_SIZE = 11;

    /**
     * Both states of the invoice button are given this one width, so that a row does not shift
     * sideways the moment an asset gains an invoice.
     */
    private static final int INVOICE_BUTTON_WIDTH = Math.max(
            revealInvoiceButton().getPreferredSize().width,
            new JButton(ATTACH_INVOICE_LABEL).getPreferredSize().width);

    /** The invoice button of an asset that has one: the icon says that it leads out of the app. */
    private static LeadingIconButton revealInvoiceButton() {
        return new LeadingIconButton(REVEAL_INVOICE_LABEL,
                IconLoader.getIcon(IconLoader.EXTERNAL, new Dimension(INVOICE_ICON_SIZE, INVOICE_ICON_SIZE)), 5);
    }

    private Configuration configuration;
    private final JPanel panelItems;
    private final JButton buttonDepreciateAll;

    private String schemaFilter;
    private int activeFilter;

    public AssetsEditor(Configuration configuration) {
        setConfiguration(configuration);
        schemaFilter = "0";
        activeFilter = 0;

        buttonDepreciateAll = new JButton("Depreciate All");

        buttonDepreciateAll.addActionListener(new OpenDepreciateDialog(this, new ArrayList<>()));

        JButton buttonAddItem = new JButton("Add");
        buttonAddItem.addActionListener(new OpenAddAssetDialog(this));

        JComboBox<Object> cbGroups = new JComboBox<>();
        JComboBox<Object> cbAccounts = new JComboBox<>();
        JComboBox<String> cbActive = new JComboBox<>();
        panelItems = new JPanel();

        panelItems.setLayout(new BoxLayout(panelItems, BoxLayout.Y_AXIS));

        cbGroups.addItem("All");
        for (SchemaModel.Class.Group group : Service.SCHEMA.getSchemaClassMap(getConfiguration().getSelectedYear()).get(0).getGroup()) {
            if (group.getId().equals(Constants.Schema.ACCUMULATED_DEP_GROUP_ID)) continue;
            cbGroups.addItem(group);
        }
        cbGroups.addActionListener(e -> {
            cbAccounts.removeAllItems();
            cbAccounts.repaint();
            cbAccounts.revalidate();

            if (cbGroups.getSelectedItem() instanceof SchemaModel.Class.Group) {
                cbAccounts.addItem("All");
                SchemaModel.Class.Group selectedGroup = (SchemaModel.Class.Group) cbGroups.getSelectedItem();
                for (SchemaModel.Class.Group.Account account : selectedGroup.getAccount()) {
                    cbAccounts.addItem(account);
                }
                cbAccounts.setSelectedIndex(0);
            } else {
                schemaFilter = "0";
                update();
            }
        });

        cbAccounts.addActionListener(e -> {
            if (cbGroups.getSelectedItem() instanceof SchemaModel.Class.Group) {
                String groupId = ((SchemaModel.Class.Group) cbGroups.getSelectedItem()).getId();
                if (cbAccounts.getSelectedItem() instanceof SchemaModel.Class.Group.Account) {
                    schemaFilter = "0" + groupId + ((SchemaModel.Class.Group.Account) cbAccounts.getSelectedItem()).getId();
                } else {
                    schemaFilter = "0" + groupId;
                }
                update();
            }
        });

        cbActive.addItem("All");
        cbActive.addItem("Only Active");
        cbActive.addItem("Only Excluded");
        cbActive.addActionListener(e -> {
            activeFilter = cbActive.getSelectedIndex();
            update();
        });

        JScrollPane paneItems = new JScrollPane(panelItems);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addComponent(buttonAddItem).addGap(5)
                        .addComponent(buttonDepreciateAll).addGap(15)
                        .addComponent(cbActive, 110, 130, 160).addGap(5)
                        .addComponent(cbGroups, 140, 200, 260).addGap(5)
                        .addComponent(cbAccounts, 140, 200, 260))
                .addComponent(paneItems));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(buttonAddItem, 25, 25, 25)
                        .addComponent(buttonDepreciateAll, 25, 25, 25)
                        .addComponent(cbActive, 25, 25, 25)
                        .addComponent(cbGroups, 25, 25, 25)
                        .addComponent(cbAccounts, 25, 25, 25))
                .addGap(4)
                .addComponent(paneItems));

        this.getActionMap().put(Configuration.ACCOUNT_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetsEditor.this.update();
            }
        });
        this.getActionMap().put(Configuration.TRANSACTION_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AssetsEditor.this.update();
            }
        });
        this.getActionMap().put(Configuration.SCHEMA_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cbGroups.removeAllItems();
                cbGroups.addItem("All");
                for (SchemaModel.Class.Group group : Service.SCHEMA.getSchemaClassMap(getConfiguration().getSelectedYear()).get(0).getGroup()) {
                    if (group.getId().equals(Constants.Schema.ACCUMULATED_DEP_GROUP_ID)) continue;
                    cbGroups.addItem(group);
                }
                cbGroups.setSelectedIndex(0);
            }
        });

        update();
    }

    public void update() {
        panelItems.removeAll();
        List<AccountsModel.Account> filteredAccounts = new ArrayList<>();
        if (schemaFilter.equals("0")) {
            for (AccountsModel.Account account : Service.ACCOUNT.getAccountsBySchemaId(getConfiguration().getSelectedYear(), schemaFilter)) {
                if (account.getSchemaId().startsWith(schemaFilter + Constants.Schema.ACCUMULATED_DEP_GROUP_ID))
                    continue;
                filteredAccounts.add(account);
            }
        } else {
            filteredAccounts.addAll(Service.ACCOUNT.getAccountsBySchemaId(getConfiguration().getSelectedYear(), schemaFilter));
        }

        for (AccountsModel.Account account : filteredAccounts) {
            AssetPanel panel = new AssetPanel(account);
            if ((panel.isActive && activeFilter == 1) || (!panel.isActive && activeFilter == 2) || (activeFilter == 0)) {
                panelItems.add(panel);
            }
        }
        panelItems.revalidate();
        panelItems.repaint();

        buttonDepreciateAll.removeActionListener(buttonDepreciateAll.getActionListeners()[0]);
        buttonDepreciateAll.addActionListener(new OpenDepreciateDialog(this, filteredAccounts));
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    private class AssetPanel extends JPanel {
        private final JLabel labelInitValue;
        private final JLabel labelCurrentValue;
        private final JSeparator separator;
        private final JPanel depPanel;
        private final JLabel labelDepInfo;
        private final JButton buttonInvoice;
        private final JButton buttonExclude;
        private final JButton buttonDep;

        private final boolean hasInvoice;
        private boolean isActive;
        private final AccountsModel.Account account;


        AssetPanel(AccountsModel.Account assetAccount) {
            this.account = assetAccount;
            this.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            String year = getConfiguration().getSelectedYear();
            Font boldFont = new Font(new JLabel().getFont().getName(), Font.BOLD, 25);
            JLabel labelAccountName = new JLabel(account.getName());
            labelAccountName.setFont(boldFont);
            labelAccountName.setToolTipText(account.getName());
            CardStyle.makeRenamable(labelAccountName, new RenameAccountAction(AssetsEditor.this, account));
            JLabel labelGroup = new JLabel("> " + Service.SCHEMA.getGroupName(year, account.getClassId(), account.getGroupId()));
            labelGroup.setToolTipText("Group");
            JLabel labelAccType = new JLabel(">> " + Service.SCHEMA.getAccountName(year, account.getClassId(), account.getGroupId(), account.getSchemaAccountId()));
            labelAccType.setToolTipText("Account Type");

            labelCurrentValue = new JLabel(" --- ", SwingConstants.RIGHT);
            labelCurrentValue.setToolTipText("Current Value");
            labelCurrentValue.setFont(boldFont);

            labelInitValue = new JLabel(" --- ", SwingConstants.RIGHT);
            labelInitValue.setFont(boldFont);
            labelInitValue.setToolTipText("Purchasing Value");

            separator = new JSeparator(SwingConstants.VERTICAL);

            labelDepInfo = new JLabel("", SwingConstants.CENTER);
            labelDepInfo.setFont(labelDepInfo.getFont().deriveFont(Font.PLAIN, labelDepInfo.getFont().getSize2D() - 1f));
            labelDepInfo.setForeground(Color.DARK_GRAY);
            labelDepInfo.setAlignmentX(CENTER_ALIGNMENT);

            buttonDep = new JButton("Depreciate");
            buttonDep.addActionListener(new OpenDepreciateDialog(AssetsEditor.this, account));
            buttonDep.setAlignmentX(CENTER_ALIGNMENT);

            depPanel = new JPanel();
            depPanel.setLayout(new BoxLayout(depPanel, BoxLayout.Y_AXIS));
            depPanel.add(buttonDep);
            // directly under the button: an empty panel used to sit here and pushed the line to the
            // bottom of the row, where it was cut off
            depPanel.add(Box.createVerticalStrut(3));
            depPanel.add(labelDepInfo);

            // The invoice is the document that says what the asset cost. The asset either has one
            // and the button hands it over to the file manager, or it has none and the button asks
            // for it - a file whose name is recorded but which is no longer in the invoices folder
            // counts as none, so that a wrong one can be replaced by deleting it.
            //
            // The name of the file is not shown. An invoice is named by whoever issued it and
            // carries the account's id on top of that, so it is nearly always too long to read in
            // a row of this width; the tooltip says which file it is.
            hasInvoice = Service.INVOICE.getInvoice(account) != null;
            String invoiceName = Service.INVOICE.getInvoiceName(account);

            buttonInvoice = hasInvoice ? revealInvoiceButton() : new JButton(ATTACH_INVOICE_LABEL);
            buttonInvoice.addActionListener(hasInvoice
                    ? new RevealInvoiceAction(AssetsEditor.this, account)
                    : new AttachInvoiceAction(AssetsEditor.this, account));
            if (hasInvoice) {
                buttonInvoice.setToolTipText("Show '" + invoiceName + "' in the file manager");
            } else if (invoiceName != null) {
                buttonInvoice.setToolTipText("'" + invoiceName
                        + "' is no longer in the invoices folder - choose the invoice again");
            } else {
                buttonInvoice.setToolTipText("Attach the invoice of this asset");
            }

            JPanel panelSeparator = new JPanel();
            panelSeparator.setOpaque(false);

            buttonExclude = new JButton("Exclude");
            buttonExclude.addActionListener(new OpenExcludeDialog(AssetsEditor.this, account));

            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(layout.createSequentialGroup().addGap(5)
                    .addGroup(layout.createParallelGroup()
                            .addComponent(labelAccountName, 500, 500, 500)
                            .addComponent(labelGroup, 500, 500, 500)
                            .addComponent(labelAccType, 500, 500, 500))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(labelInitValue, 150, 150, 150)
                            .addComponent(labelCurrentValue, 150, 150, 150))
                    .addGap(10)
                    .addComponent(separator, 5, 5, 5)
                    .addComponent(depPanel, 200, 200, 200)
                    .addComponent(panelSeparator)
                    .addComponent(buttonInvoice, INVOICE_BUTTON_WIDTH, INVOICE_BUTTON_WIDTH, INVOICE_BUTTON_WIDTH)
                    .addGap(5)
                    .addComponent(buttonExclude)
                    .addGap(25));
            layout.setVerticalGroup(layout.createSequentialGroup().addGap(5)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addGroup(layout.createSequentialGroup()
                                    .addComponent(labelAccountName)
                                    .addComponent(labelGroup)
                                    .addComponent(labelAccType))
                            .addGroup(layout.createSequentialGroup()
                                    .addComponent(labelInitValue)
                                    .addComponent(labelCurrentValue))
                            .addComponent(separator, 60, 60, 60)
                            .addComponent(depPanel, 50, 50, 50)
                            .addComponent(panelSeparator, 50, 50, 50)
                            .addComponent(buttonInvoice, GroupLayout.PREFERRED_SIZE,
                                    GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(buttonExclude))
                    .addGap(5));
            update();
        }

        private void update() {
            String year = getConfiguration().getSelectedYear();
            Integer assetValue = Service.TRANSACTIONS.getAccountBalance(year, account);
            isActive = assetValue != 0;
            if (isActive) {
                Integer accDepValue = Service.TRANSACTIONS.getAccountBalance(year, Service.ACCOUNT.getAccumulatedDepAccount(year, account));
                String currentValue = String.valueOf(assetValue - accDepValue);
                labelCurrentValue.setText(currentValue);
                labelInitValue.setText(String.valueOf(assetValue));
                try {
                    String lastDepDate = Service.TRANSACTIONS.getLastDepreciationDate(year, account);
                    if (lastDepDate != null) {
                        buttonDep.setEnabled(!(lastDepDate.substring(2, 4).equals("12") || currentValue.equals("0")));
                        lastDepDate = new SimpleDateFormat("dd.MM.").format(new SimpleDateFormat("ddMM").parse(lastDepDate));
                        labelDepInfo.setText("Last Depreciation: " + lastDepDate);
                    } else {
                        for (TransactionsModel.Transaction tr : Service.TRANSACTIONS.getTransactions(year, account.getFullId(), null)) {
                            if (tr.getDescription().contains(Constants.Transaction.PURCHASE_DESCRIPTION)) {
                                lastDepDate = tr.getDate();
                            }
                        }
                        if (lastDepDate != null) {
                            buttonDep.setEnabled(!lastDepDate.substring(2, 4).equals("12"));
                            lastDepDate = new SimpleDateFormat("dd.MM.").format(new SimpleDateFormat("ddMM").parse(lastDepDate));
                            labelDepInfo.setText("Purchased: " + lastDepDate);
                        } else {
                            labelDepInfo.setText("No action this year");
                        }
                    }
                } catch (ParseException e) {
                    Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
                    ErrorHandler.getThrowableDialog(e).setVisible(true);
                }
            } else {
                this.setBackground(Constants.Color.EXPENSE_RED);
                labelCurrentValue.setVisible(false);
                labelInitValue.setText("Excluded");
                labelInitValue.setForeground(Color.RED);
                labelInitValue.setFont(new JLabel().getFont());
                labelInitValue.setToolTipText("");
                separator.setVisible(false);
                depPanel.setVisible(false);
                buttonExclude.setVisible(false);
                // an asset no longer owned is not carried into the next year, so there is nothing
                // to attach an invoice to - but the invoice it already has is still its receipt
                buttonInvoice.setVisible(hasInvoice);
            }
            AssetPanel.this.revalidate();
            AssetPanel.this.repaint();
        }
    }
}
