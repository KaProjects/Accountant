package org.kaleta.accountant.frontend.component.year.dialog;

import org.kaleta.accountant.frontend.component.year.model.AccountModel;
import org.kaleta.accountant.frontend.component.year.model.SchemaModel;
import org.kaleta.accountant.frontend.dialog.Dialog;

import javax.swing.*;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Stanislav Kaleta on 18.08.2017.
 */
public class AddResourcesDialog extends Dialog {
    private final List<SchemaModel.Clazz.Group> resourcesGroupList;
    private final Map<String, List<AccountModel.Account>> resourcesSemanticMap;
    private final List<SchemaModel.Clazz> payableClassesList;
    private final Map<String, List<AccountModel.Account>> payableSemanticMap;

    private JTextField tfDate;

    private JComboBox<String> cbPayableClass;
    private List<SchemaModel.Clazz.Group> payableGroups;
    private JComboBox<String> cbPayableGroup;
    private List<SchemaModel.Clazz.Group.Account> payableAccounts;
    private JComboBox<String> cbPayableAccount;
    private List<AccountModel.Account> payableSemantics;
    private JComboBox<String> cbPayableSemantic;

    private List<ResourcePanel> resourcePanelList;

    public AddResourcesDialog(Component parent, List<SchemaModel.Clazz.Group> resourcesGroupList, Map<String, List<AccountModel.Account>> resourcesSemanticMap,
                              List<SchemaModel.Clazz> payableClassesList, Map<String, List<AccountModel.Account>> payableSemanticMap) {
        super(parent, "Adding Resources");
        this.resourcesGroupList = resourcesGroupList;
        this.resourcesSemanticMap = resourcesSemanticMap;
        this.payableClassesList = payableClassesList;
        this.payableSemanticMap = payableSemanticMap;
        payableGroups = new ArrayList<>();
        payableAccounts = new ArrayList<>();
        payableSemantics = new ArrayList<>();
        resourcePanelList = new ArrayList<>();
        buildDialog();
        this.setMinimumSize(new Dimension(500,600));
        pack();
    }

    @Override
    protected void buildDialog() {
        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(a -> dispose());
        JButton buttonOk = new JButton("Add");
        buttonOk.addActionListener(a -> {
            boolean transactionsValid = true;
            for (ResourcePanel resourcePanel : resourcePanelList){
                transactionsValid = transactionsValid && resourcePanel.hasValidValues();
            }
            if (cbPayableClass.getSelectedIndex() == -1 || cbPayableGroup.getSelectedIndex() == -1 || cbPayableAccount.getSelectedIndex() == -1 || cbPayableSemantic.getSelectedIndex() == -1
                    || tfDate.getText() == null || tfDate.getText().trim().isEmpty() || !transactionsValid){
                JOptionPane.showMessageDialog(AddResourcesDialog.this, "Mandatory attribute is not set!", "Value Missing", JOptionPane.ERROR_MESSAGE);
                return;
            }
            result = true;
            dispose();
        });

        JLabel labelDate = new JLabel("Date: ");
        tfDate = new JTextField();
        JButton buttonToday = new JButton("Today");
        buttonToday.addActionListener(a -> {
            Calendar calendar = Calendar.getInstance();
            String date = String.format("%1$02d%2$02d%3$04d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
            tfDate.setText(date);
        });

        JLabel labelPayedBy = new JLabel("Payed By: ");

        cbPayableClass = new JComboBox<>(payableClassesList.stream().map(SchemaModel.Clazz::getName).collect(Collectors.toList()).toArray(new String[]{}));
        cbPayableClass.setSelectedIndex(-1);
        cbPayableClass.addActionListener(a -> {
            cbPayableGroup.removeAllItems();
            payableGroups.clear();
            payableGroups.addAll(payableClassesList.get(cbPayableClass.getSelectedIndex()).getGroups().values());
            payableGroups.forEach(group -> cbPayableGroup.addItem(group.getName()));
            cbPayableGroup.setSelectedIndex(-1);
        });

        cbPayableGroup = new JComboBox<>(payableGroups.stream().map(SchemaModel.Clazz.Group::getName).collect(Collectors.toList()).toArray(new String[]{}));
        cbPayableGroup.setSelectedIndex(-1);
        cbPayableGroup.addActionListener(a -> {
            cbPayableAccount.removeAllItems();
            payableAccounts.clear();
            if (cbPayableGroup.getSelectedIndex() >= 0){
                payableAccounts.addAll(payableGroups.get(cbPayableGroup.getSelectedIndex()).getAccounts().values());
                payableAccounts.forEach(account -> cbPayableAccount.addItem(account.getName()));
            }
            cbPayableAccount.setSelectedIndex(-1);
        });

        cbPayableAccount = new JComboBox<>(payableAccounts.stream().map(SchemaModel.Clazz.Group.Account::getName).collect(Collectors.toList()).toArray(new String[]{}));
        cbPayableAccount.setSelectedIndex(-1);
        cbPayableAccount.addActionListener(a -> {
            cbPayableSemantic.removeAllItems();
            payableSemantics.clear();
            if (cbPayableAccount.getSelectedIndex() >= 0) {
                String schemaId = String.valueOf(payableClassesList.get(cbPayableClass.getSelectedIndex()).getId())
                        + String.valueOf(payableGroups.get(cbPayableGroup.getSelectedIndex()).getId())
                        + String.valueOf(payableAccounts.get(cbPayableAccount.getSelectedIndex()).getId());
                payableSemantics.addAll(payableSemanticMap.get(schemaId));
                payableSemantics.forEach(semantic -> cbPayableSemantic.addItem(semantic.getName()));
            }
            cbPayableSemantic.setSelectedIndex(-1);
        });

        cbPayableSemantic = new JComboBox<>();
        cbPayableSemantic.setSelectedIndex(-1);

        JPanel resourcesPanel = new JPanel();
        resourcesPanel.setLayout(new BoxLayout(resourcesPanel, BoxLayout.Y_AXIS));
        ResourcePanel firstResourcePanel = new ResourcePanel();
        resourcesPanel.add(firstResourcePanel);
        resourcePanelList.add(firstResourcePanel);

        JScrollPane resourcesPane = new JScrollPane(resourcesPanel);

        JButton buttonAddResource = new JButton("Add Item");
        buttonAddResource.addActionListener(a -> {
            ResourcePanel resourcePanel = new ResourcePanel();
            resourcesPanel.add(resourcePanel);
            resourcePanelList.add(resourcePanel);
            resourcesPane.repaint();
            resourcesPane.revalidate();
        });

        JSeparator separator1 = new JSeparator();
        JSeparator separator2 = new JSeparator();

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGap(10)
                .addGroup(layout.createParallelGroup()
                        .addComponent(labelDate)
                        .addGroup(layout.createSequentialGroup().addComponent(tfDate,100,100,100).addGap(5).addComponent(buttonToday))
                        .addComponent(labelPayedBy)
                        .addGroup(layout.createSequentialGroup().addComponent(cbPayableClass).addComponent(cbPayableGroup).addComponent(cbPayableAccount).addComponent(cbPayableSemantic))
                        .addComponent(separator1)
                        .addComponent(resourcesPane)
                        .addComponent(separator2)
                        .addGroup(layout.createSequentialGroup().addComponent(buttonAddResource).addGap(5, 5, Short.MAX_VALUE).addComponent(buttonCancel).addGap(5).addComponent(buttonOk).addGap(5)))
                .addGap(10));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGap(10)
                .addComponent(labelDate)
                .addGap(2)
                .addGroup(layout.createParallelGroup().addComponent(tfDate,25,25,25).addComponent(buttonToday,25,25,25))
                .addGap(5)
                .addComponent(labelPayedBy)
                .addGap(2)
                .addGroup(layout.createParallelGroup().addComponent(cbPayableClass,25,25,25).addComponent(cbPayableGroup,25,25,25).addComponent(cbPayableAccount,25,25,25).addComponent(cbPayableSemantic,25,25,25))
                .addComponent(separator1,10,10,10)
                .addComponent(resourcesPane)
                .addComponent(separator2,10,10,10)
                .addGroup(layout.createParallelGroup().addComponent(buttonAddResource).addComponent(buttonCancel).addComponent(buttonOk))
                .addGap(10));
    }

    public String getDate() {
        return tfDate.getText();
    }

    public String getCreditor(){
        String schemaId = String.valueOf(payableClassesList.get(cbPayableClass.getSelectedIndex()).getId())
                + String.valueOf(payableGroups.get(cbPayableGroup.getSelectedIndex()).getId())
                + String.valueOf(payableAccounts.get(cbPayableAccount.getSelectedIndex()).getId());
        return payableSemanticMap.get(schemaId).get(cbPayableSemantic.getSelectedIndex()).getFullId();
    }

    public List<ResourcePanel> getResourcePanelList(){
        return resourcePanelList;
    }

    public class ResourcePanel extends JPanel {
        private JComboBox<String> cbGroup;
        private List<SchemaModel.Clazz.Group.Account> accounts;
        private JComboBox<String> cbAccount;
        private List<AccountModel.Account> semantics;
        private JComboBox<String> cbSemantic;

        private JTextField tfAmount;

        private ResourcePanel(){
            accounts = new ArrayList<>();
            semantics = new ArrayList<>();
            cbGroup = new JComboBox<>(resourcesGroupList.stream().map(SchemaModel.Clazz.Group::getName).collect(Collectors.toList()).toArray(new String[]{}));
            cbGroup.setSelectedIndex(-1);
            cbGroup.addActionListener(a -> {
                cbAccount.removeAllItems();
                accounts.clear();
                accounts.addAll(resourcesGroupList.get(cbGroup.getSelectedIndex()).getAccounts().values());
                accounts.forEach(account -> cbAccount.addItem(account.getName()));
                cbAccount.setSelectedIndex(-1);
            });

            cbAccount = new JComboBox<>(accounts.stream().map(SchemaModel.Clazz.Group.Account::getName).collect(Collectors.toList()).toArray(new String[]{}));
            cbAccount.setSelectedIndex(-1);
            cbAccount.addActionListener(a -> {
                cbSemantic.removeAllItems();
                semantics.clear();
                if (cbAccount.getSelectedIndex() >= 0) {
                    String schemaId = "1" + String.valueOf(resourcesGroupList.get(cbGroup.getSelectedIndex()).getId())
                            + String.valueOf(accounts.get(cbAccount.getSelectedIndex()).getId());
                    semantics.addAll(resourcesSemanticMap.get(schemaId));
                    semantics.forEach(account -> cbSemantic.addItem(account.getName()));
                }
                cbSemantic.setSelectedIndex(-1);
            });

            cbSemantic = new JComboBox<>();
            cbSemantic.setSelectedIndex(-1);

            tfAmount = new JTextField();
            tfAmount.setHorizontalAlignment(SwingConstants.RIGHT);
            tfAmount.setToolTipText("Amount");

            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(cbGroup).addComponent(cbAccount).addComponent(cbSemantic).addGap(5).addComponent(tfAmount).addGap(5));
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup().addComponent(cbGroup,25,25,25).addComponent(cbAccount,25,25,25).addComponent(cbSemantic,25,25,25).addComponent(tfAmount,25,25,25))
                    .addGap(2));

        }

        public boolean hasValidValues(){
            return cbGroup.getSelectedIndex() != -1 && cbAccount.getSelectedIndex() != -1 && cbSemantic.getSelectedIndex() != -1
                    && tfAmount.getText() != null && !tfAmount.getText().trim().isEmpty();
        }

        public String getAmount(){
            return tfAmount.getText();
        }

        public String getResourceId(){
            String schemaId = "1" + String.valueOf(resourcesGroupList.get(cbGroup.getSelectedIndex()).getId())
                    + String.valueOf(accounts.get(cbAccount.getSelectedIndex()).getId());
            return resourcesSemanticMap.get(schemaId).get(cbSemantic.getSelectedIndex()).getFullId();
        }
    }
}
