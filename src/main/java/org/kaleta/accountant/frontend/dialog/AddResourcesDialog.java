package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.frontend.common.NumberFilter;
import org.kaleta.accountant.frontend.component.SelectAccountTextField;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class AddResourcesDialog extends Dialog {
    private final Map<String, List<AccountsModel.Account>> resourceAccountMap;
    private final SchemaModel.Class resourceClass;
    private final Map<String, List<AccountsModel.Account>> creditAccountMap;
    private final List<SchemaModel.Class> creditClasses;
    private final Map<String, List<AccountsModel.Account>> debitAccountMap;
    private final List<SchemaModel.Class> debitClasses ;

    private JTextField tfDate;
    private JComboBox<SchemaModel.Class> cbCreditClass;
    private JComboBox<SchemaModel.Class.Group> cbCreditGroup;
    private JComboBox<SchemaModel.Class.Group.Account> cbCreditAccount;
    private JComboBox<AccountsModel.Account> cbCreditSemantic;
    private final List<ResourcePanel> resourcePanelList;
    private JPanel resourcesPanel;

    public AddResourcesDialog(Configuration configuration, Map<String, List<AccountsModel.Account>> resourceAccountMap, SchemaModel.Class resourceClass,
                              Map<String, List<AccountsModel.Account>> creditAccountMap, List<SchemaModel.Class> creditClasses,
                              Map<String, List<AccountsModel.Account>> debitAccountMap, List<SchemaModel.Class> debitClasses) {
        super(configuration, "Adding Resources");
        this.resourceAccountMap = resourceAccountMap;
        this.resourceClass = resourceClass;
        this.creditAccountMap = creditAccountMap;
        this.creditClasses = creditClasses;
        this.debitAccountMap = debitAccountMap;
        this.debitClasses = debitClasses;
        resourcePanelList = new ArrayList<>();
        buildDialog();
        this.setMinimumSize(new Dimension(500, 600));
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
            if (cbCreditClass.getSelectedIndex() == -1 || cbCreditGroup.getSelectedIndex() == -1 || cbCreditAccount.getSelectedIndex() == -1 || cbCreditSemantic.getSelectedIndex() == -1
                    || tfDate.getText() == null || tfDate.getText().trim().isEmpty() || tfDate.getText().length() != 4
                    || !transactionsValid){
                JOptionPane.showMessageDialog(AddResourcesDialog.this, "Mandatory attribute is not set!", "Value Missing", JOptionPane.ERROR_MESSAGE);
                return;
            }
            result = true;
            dispose();
        });

        JLabel labelDate = new JLabel("Date: ");
        tfDate = new JTextField();
        tfDate.setHorizontalAlignment(SwingConstants.RIGHT);
        JButton buttonToday = new JButton("Today");
        buttonToday.addActionListener(a -> {
            Calendar calendar = Calendar.getInstance();
            String date = String.format("%1$02d%2$02d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1);
            tfDate.setText(date);
        });

        JLabel labelPayedBy = new JLabel("Payed By: ");

        cbCreditClass = new JComboBox<>();
        creditClasses.forEach(clazz -> cbCreditClass.addItem(clazz));
        cbCreditClass.setSelectedIndex(-1);

        cbCreditGroup = new JComboBox<>();
        cbCreditAccount = new JComboBox<>();
        cbCreditSemantic = new JComboBox<>();

        cbCreditClass.addActionListener(a -> {
            cbCreditGroup.removeAllItems();
            if (cbCreditClass.getSelectedItem() != null) {
                ((SchemaModel.Class)cbCreditClass.getSelectedItem()).getGroup().forEach(group -> cbCreditGroup.addItem(group));
            }
            cbCreditGroup.setSelectedIndex(-1);
        });
        cbCreditGroup.addActionListener(a -> {
            cbCreditAccount.removeAllItems();
            if (cbCreditGroup.getSelectedItem() != null && cbCreditGroup.getSelectedIndex() >= 0){
                ((SchemaModel.Class.Group)cbCreditGroup.getSelectedItem()).getAccount().forEach(account -> cbCreditAccount.addItem(account));
            }
            cbCreditAccount.setSelectedIndex(-1);
        });
        cbCreditAccount.addActionListener(a -> {
            cbCreditSemantic.removeAllItems();
            if (cbCreditClass.getSelectedItem() != null && cbCreditGroup.getSelectedItem() != null && cbCreditAccount.getSelectedItem() != null
                    && cbCreditAccount.getSelectedIndex() >= 0) {
                String schemaId = ((SchemaModel.Class)cbCreditClass.getSelectedItem()).getId()
                        + ((SchemaModel.Class.Group)cbCreditGroup.getSelectedItem()).getId()
                        + ((SchemaModel.Class.Group.Account)cbCreditAccount.getSelectedItem()).getId();
                if (creditAccountMap.get(schemaId) != null) {
                    creditAccountMap.get(schemaId).forEach(semantic -> cbCreditSemantic.addItem(semantic));
                }
            }
            cbCreditSemantic.setSelectedIndex(-1);
        });

        resourcesPanel = new JPanel();
        resourcesPanel.setLayout(new BoxLayout(resourcesPanel, BoxLayout.Y_AXIS));
        ResourcePanel firstResourcePanel = new ResourcePanel(false);
        resourcesPanel.add(firstResourcePanel);
        resourcePanelList.add(firstResourcePanel);

        JScrollPane resourcesPane = new JScrollPane(resourcesPanel);

        JButton buttonAddResource = new JButton("Add Item");
        buttonAddResource.addActionListener(a -> {
            ResourcePanel resourcePanel = new ResourcePanel(true);
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
                        .addGroup(layout.createSequentialGroup().addComponent(cbCreditClass).addComponent(cbCreditGroup).addComponent(cbCreditAccount).addComponent(cbCreditSemantic))
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
                .addGroup(layout.createParallelGroup().addComponent(cbCreditClass,25,25,25).addComponent(cbCreditGroup,25,25,25).addComponent(cbCreditAccount,25,25,25).addComponent(cbCreditSemantic,25,25,25))
                .addComponent(separator1,10,10,10)
                .addComponent(resourcesPane)
                .addComponent(separator2,10,10,10)
                .addGroup(layout.createParallelGroup().addComponent(buttonAddResource).addComponent(buttonCancel).addComponent(buttonOk))
                .addGap(10));
    }

    public String getDate() {
        return tfDate.getText();
    }

    public String getCreditAcc(){
        assert cbCreditSemantic.getSelectedItem() != null;
        return ((AccountsModel.Account)cbCreditSemantic.getSelectedItem()).getFullId();
    }

    public List<ResourceData> getResourceData(){
        List<ResourceData> resourceData = new ArrayList<>();
        for (ResourcePanel resourcePanel : resourcePanelList){
            if (resourcePanel.isConsumed()){
                resourceData.add(new ResourceData(resourcePanel.getResourceId(), resourcePanel.getAmount()));
            } else {
                resourceData.add(new ResourceData(resourcePanel.getResourceId(), resourcePanel.getAmount(), resourcePanel.getDebitId(), resourcePanel.getDebitInfo()));
            }
        }
        return resourceData;
    }

    class ResourcePanel extends JPanel {
        private final JComboBox<SchemaModel.Class.Group> cbGroup;
        private final JComboBox<SchemaModel.Class.Group.Account> cbAccount;
        private final JComboBox<AccountsModel.Account> cbSemantic;
        private final JTextField tfAmount;
        private final JCheckBox checkBoxConsumed;
        private final SelectAccountTextField textFieldDebit;
        private final JTextField textFieldDebitInfo;

        private ResourcePanel(boolean deletable){
            this.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            cbGroup = new JComboBox<>();
            resourceClass.getGroup().forEach(cbGroup::addItem);
            cbGroup.setSelectedIndex(-1);

            cbAccount = new JComboBox<>();
            cbSemantic = new JComboBox<>();

            cbGroup.addActionListener(a -> {
                cbAccount.removeAllItems();
                if (cbGroup.getSelectedItem() != null && cbGroup.getSelectedIndex() >= 0) {
                    ((SchemaModel.Class.Group)cbGroup.getSelectedItem()).getAccount().forEach(cbAccount::addItem);
                }
                cbAccount.setSelectedIndex(-1);
            });
            cbAccount.addActionListener(a -> {
                cbSemantic.removeAllItems();
                if (cbGroup.getSelectedItem() != null && cbAccount.getSelectedItem() != null && cbAccount.getSelectedIndex() >= 0) {
                    String schemaId = "1" + ((SchemaModel.Class.Group)cbGroup.getSelectedItem()).getId()
                            + ((SchemaModel.Class.Group.Account)cbAccount.getSelectedItem()).getId();
                    if (resourceAccountMap.get(schemaId) != null){
                        resourceAccountMap.get(schemaId).forEach(cbSemantic::addItem);
                    }
                }
                cbSemantic.setSelectedIndex(-1);
            });

            JButton buttonDelete = new JButton(IconLoader.getIcon(IconLoader.DELETE,new Dimension(10,10)));
            buttonDelete.addActionListener(e -> {
                resourcePanelList.remove(this);
                resourcesPanel.removeAll();
                for (ResourcePanel resourcePanel : resourcePanelList){
                    resourcesPanel.add(resourcePanel);
                }
                resourcesPanel.repaint();
                resourcesPanel.revalidate();
            });
            buttonDelete.setEnabled(deletable);

            tfAmount = new JTextField();
            tfAmount.setHorizontalAlignment(SwingConstants.RIGHT);
            tfAmount.setToolTipText("Amount");
            ((PlainDocument) tfAmount.getDocument()).setDocumentFilter(new NumberFilter());

            JLabel labelDebit = new JLabel("In favor of: ");
            labelDebit.setVisible(false);
            textFieldDebit = new SelectAccountTextField(getConfiguration(), debitAccountMap, debitClasses);
            textFieldDebit.setVisible(false);

            textFieldDebitInfo = new JTextField();
            textFieldDebitInfo.setVisible(false);
            JLabel labelDebitInfo = new JLabel("Info: ");
            labelDebitInfo.setVisible(false);

            checkBoxConsumed = new JCheckBox();
            checkBoxConsumed.setToolTipText("Whether resource is consumed or...");
            checkBoxConsumed.setSelected(true);
            checkBoxConsumed.addActionListener(e -> {
                labelDebit.setVisible(!checkBoxConsumed.isSelected());
                textFieldDebit.setVisible(!checkBoxConsumed.isSelected());
                textFieldDebitInfo.setVisible(!checkBoxConsumed.isSelected());
                labelDebitInfo.setVisible(!checkBoxConsumed.isSelected());
            });

            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(layout.createParallelGroup().addGroup(layout.createSequentialGroup()
                    .addComponent(buttonDelete,10,10,10).addComponent(cbGroup).addComponent(cbAccount).addComponent(cbSemantic).addComponent(tfAmount,100,100,1000).addComponent(checkBoxConsumed))
                    .addGroup(layout.createSequentialGroup()
                            .addGap(5).addComponent(labelDebit).addComponent(textFieldDebit).addGap(5).addComponent(labelDebitInfo).addComponent(textFieldDebitInfo)));
            layout.setVerticalGroup(layout.createSequentialGroup().addGap(2).addGroup(layout.createParallelGroup()
                    .addComponent(buttonDelete,25,25,25).addComponent(cbGroup,25,25,25).addComponent(cbAccount,25,25,25).addComponent(cbSemantic,25,25,25).addComponent(tfAmount,25,25,25).addComponent(checkBoxConsumed,25,25,25))
                    .addGroup(layout.createParallelGroup()
                            .addComponent(labelDebit,25,25,25).addComponent(textFieldDebit,25,25,25).addComponent(labelDebitInfo,25,25,25).addComponent(textFieldDebitInfo,25,25,25))
                    .addGap(2));
        }

        boolean hasValidValues(){
            return (cbGroup.getSelectedIndex() != -1 && cbAccount.getSelectedIndex() != -1 && cbSemantic.getSelectedIndex() != -1 && tfAmount.getText() != null && !tfAmount.getText().trim().isEmpty())
                    && (checkBoxConsumed.isSelected() || !textFieldDebit.getSelectedAccount().trim().isEmpty());
        }

        String getAmount(){
            return tfAmount.getText();
        }

        String getResourceId(){
            assert cbSemantic.getSelectedItem() != null;
            return ((AccountsModel.Account)cbSemantic.getSelectedItem()).getFullId();
        }

        boolean isConsumed(){
            return checkBoxConsumed.isSelected();
        }

        String getDebitId(){
            return textFieldDebit.getSelectedAccount();
        }

        String getDebitInfo(){
            return textFieldDebitInfo.getText();
        }
    }

    public class ResourceData {
        private final String resourceId;
        private final String amount;
        private final boolean isConsumed;
        private String debitId;
        private String debitInfo;

        ResourceData(String resourceId, String amount) {
            this.resourceId = resourceId;
            this.amount = amount;
            isConsumed = true;
        }

        ResourceData(String resourceId, String amount, String debitId, String debitInfo) {
            this.resourceId = resourceId;
            this.amount = amount;
            this.debitId = debitId;
            this.debitInfo = debitInfo;
            isConsumed = false;
        }

        public String getResourceId() {
            return resourceId;
        }

        public String getAmount() {
            return amount;
        }

        public boolean isConsumed() {
            return isConsumed;
        }

        public String getDebitId() {
            return debitId;
        }

        public String getDebitInfo() {
            return debitInfo;
        }
    }
}
