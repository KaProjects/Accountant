package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.frontend.common.SwingWorkerHandler;
import org.kaleta.accountant.frontend.component.DatePickerTextField;
import org.kaleta.accountant.frontend.component.HintValidatedTextField;
import org.kaleta.accountant.frontend.component.SelectAccountTextField;
import org.kaleta.accountant.frontend.component.ValidatedComboBox;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class AddResourcesDialog extends Dialog {
    private final Object lock = new Object();
    private final Map<String, List<String>> resourceDescriptionMap;
    private final Map<String, List<AccountsModel.Account>> resourceAccountMap;
    private final SchemaModel.Class resourceClass;
    private final Map<String, List<AccountsModel.Account>> creditAccountMap;
    private final List<SchemaModel.Class> creditClasses;
    private final Map<String, List<AccountsModel.Account>> debitAccountMap;
    private final List<SchemaModel.Class> debitClasses ;

    private DatePickerTextField textFieldDate;
    private JComboBox<SchemaModel.Class> cbCreditClass;
    private JComboBox<SchemaModel.Class.Group> cbCreditGroup;
    private JComboBox<SchemaModel.Class.Group.Account> cbCreditAccount;
    private JComboBox<AccountsModel.Account> cbCreditSemantic;
    private final List<ResourcePanel> resourcePanelList;
    private JPanel resourcesPanel;

    public AddResourcesDialog(Configuration configuration, Map<String, List<AccountsModel.Account>> resourceAccountMap, SchemaModel.Class resourceClass,
                              Map<String, List<AccountsModel.Account>> creditAccountMap, List<SchemaModel.Class> creditClasses,
                              Map<String, List<AccountsModel.Account>> debitAccountMap, List<SchemaModel.Class> debitClasses,
                              Map<String, List<String>> resourceDescriptionMap) {
        super(configuration, "Adding Resources", "Add");
        this.resourceDescriptionMap = resourceDescriptionMap;
        this.resourceAccountMap = resourceAccountMap;
        this.resourceClass = resourceClass;
        this.creditAccountMap = creditAccountMap;
        this.creditClasses = creditClasses;
        this.debitAccountMap = debitAccountMap;
        this.debitClasses = debitClasses;
        resourcePanelList = new ArrayList<>();
        buildDialogContent();
        this.setMinimumSize(new Dimension(500, 600));
        pack();
    }

    private void buildDialogContent() {
        JLabel labelDate = new JLabel("Date: ");
        textFieldDate = new DatePickerTextField("",this);
        JButton buttonToday = new JButton("Today");
        buttonToday.addActionListener(a -> {
            textFieldDate.focusGained(null);
            Calendar calendar = Calendar.getInstance();
            textFieldDate.setText(String.format("%1$02d%2$02d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1));
        });

        JLabel labelPayedBy = new JLabel("Payed By: ");

        cbCreditClass = new ValidatedComboBox<>("Payed By - Class", this);
        creditClasses.forEach(clazz -> cbCreditClass.addItem(clazz));
        cbCreditClass.setSelectedIndex(-1);

        cbCreditGroup = new ValidatedComboBox<>("Payed By - Group", this);
        cbCreditGroup.setSelectedIndex(-1);
        cbCreditAccount = new ValidatedComboBox<>("Payed By - Account", this);
        cbCreditAccount.setSelectedIndex(-1);
        cbCreditSemantic = new ValidatedComboBox<>("Payed By - Semantic", this);
        cbCreditSemantic.setSelectedIndex(-1);

        cbCreditClass.addActionListener(a -> {
            cbCreditGroup.removeAllItems();
            if (cbCreditClass.getSelectedItem() != null) {
                ((SchemaModel.Class) cbCreditClass.getSelectedItem()).getGroup().forEach(group -> cbCreditGroup.addItem(group));
            }
            cbCreditGroup.setSelectedIndex(-1);
        });
        cbCreditGroup.addActionListener(a -> {
            cbCreditAccount.removeAllItems();
            if (cbCreditGroup.getSelectedItem() != null && cbCreditGroup.getSelectedIndex() >= 0) {
                ((SchemaModel.Class.Group) cbCreditGroup.getSelectedItem()).getAccount().forEach(account -> cbCreditAccount.addItem(account));
            }
            cbCreditAccount.setSelectedIndex(-1);
        });
        cbCreditAccount.addActionListener(a -> {
            cbCreditSemantic.removeAllItems();
            if (cbCreditClass.getSelectedItem() != null && cbCreditGroup.getSelectedItem() != null && cbCreditAccount.getSelectedItem() != null
                    && cbCreditAccount.getSelectedIndex() >= 0) {
                String schemaId = ((SchemaModel.Class) cbCreditClass.getSelectedItem()).getId()
                        + ((SchemaModel.Class.Group) cbCreditGroup.getSelectedItem()).getId()
                        + ((SchemaModel.Class.Group.Account) cbCreditAccount.getSelectedItem()).getId();
                if (creditAccountMap.get(schemaId) != null) {
                    creditAccountMap.get(schemaId).forEach(semantic -> cbCreditSemantic.addItem(semantic));
                }
            }
            cbCreditSemantic.setSelectedIndex(-1);
        });

        resourcesPanel = new JPanel();
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

        setContent(layout -> {
            layout.setHorizontalGroup(layout.createParallelGroup()
                    .addComponent(labelDate)
                    .addGroup(layout.createSequentialGroup().addComponent(textFieldDate, 100, 100, 100).addGap(5).addComponent(buttonToday))
                    .addComponent(labelPayedBy)
                    .addGroup(layout.createSequentialGroup().addComponent(cbCreditClass).addComponent(cbCreditGroup).addComponent(cbCreditAccount).addComponent(cbCreditSemantic))
                    .addComponent(separator1)
                    .addComponent(resourcesPane));
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addComponent(labelDate)
                    .addGap(2)
                    .addGroup(layout.createParallelGroup().addComponent(textFieldDate, 25, 25, 25).addComponent(buttonToday, 25, 25, 25))
                    .addGap(5)
                    .addComponent(labelPayedBy)
                    .addGap(2)
                    .addGroup(layout.createParallelGroup().addComponent(cbCreditClass, 25, 25, 25).addComponent(cbCreditGroup, 25, 25, 25).addComponent(cbCreditAccount, 25, 25, 25).addComponent(cbCreditSemantic, 25, 25, 25))
                    .addComponent(separator1, 10, 10, 10)
                    .addComponent(resourcesPane));
        });

        setButtons(jPanel -> jPanel.add(buttonAddResource));
    }

    @Override
    void validateDialog() {
        super.validateDialog();
        if (resourcePanelList.isEmpty()) AddResourcesDialog.this.setDialogValid("No Resource");
    }

    public String getDate() {
        return textFieldDate.getText();
    }

    public String getCreditAcc(){
        assert cbCreditSemantic.getSelectedItem() != null;
        return ((AccountsModel.Account)cbCreditSemantic.getSelectedItem()).getFullId();
    }

    public List<ResourceData> getResourceData(){
        List<ResourceData> resourceData = new ArrayList<>();
        for (ResourcePanel resourcePanel : resourcePanelList){
            if (resourcePanel.isConsumed()){
                resourceData.add(new ResourceData(resourcePanel.getResourceId(), resourcePanel.getAmount(), resourcePanel.getDescription()));
            } else {
                resourceData.add(new ResourceData(resourcePanel.getResourceId(), resourcePanel.getAmount(), resourcePanel.getDescription(), resourcePanel.getDebitId(), resourcePanel.getDebitInfo()));
            }
        }
        return resourceData;
    }

    class ResourcePanel extends JPanel {
        private final SelectAccountTextField tfResource;
        private final HintValidatedTextField tfAmount;
        private final JComboBox<String> cbDescription;
        private final JCheckBox checkBoxConsumed;
        private final SelectAccountTextField textFieldDebit;
        private final JTextField textFieldDebitInfo;

        private ResourcePanel(){
            this.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            tfResource = new SelectAccountTextField(getConfiguration(), resourceAccountMap, resourceClass, "Resource", AddResourcesDialog.this);
            tfAmount = new HintValidatedTextField("","Amount", "set amount", true, AddResourcesDialog.this);
            cbDescription = new JComboBox<>();
            cbDescription.setEditable(true);
            tfResource.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    new SwingWorkerHandler() {
                        @Override
                        protected void runInBackground() {
                            updateDescriptions();
                        }
                    }.execute();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    new SwingWorkerHandler() {
                        @Override
                        protected void runInBackground() {
                            updateDescriptions();
                        }
                    }.execute();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    new SwingWorkerHandler() {
                        @Override
                        protected void runInBackground() {
                            updateDescriptions();
                        }
                    }.execute();
                }
            });

            JLabel labelDebit = new JLabel("In favor of: ");
            labelDebit.setVisible(false);
            textFieldDebit = new SelectAccountTextField(getConfiguration(), debitAccountMap, debitClasses, "In favor of", AddResourcesDialog.this);
            textFieldDebit.setVisible(false);
            textFieldDebit.setValidatorEnabled(false);

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

                textFieldDebit.setValidatorEnabled(!checkBoxConsumed.isSelected());
                AddResourcesDialog.this.validateDialog();
            });

            JButton buttonDelete = new JButton(IconLoader.getIcon(IconLoader.DELETE,new Dimension(10,10)));
            buttonDelete.addActionListener(e -> {
                tfResource.setValidatorEnabled(false);
                tfAmount.setValidatorEnabled(false);

                AddResourcesDialog.this.validateDialog();

                resourcePanelList.remove(this);
                resourcesPanel.removeAll();
                if (resourcePanelList.isEmpty()) {
                    AddResourcesDialog.this.setDialogValid("No Resource");
                } else {
                    for (ResourcePanel resourcePanel : resourcePanelList){
                        resourcesPanel.add(resourcePanel);
                    }
                }
                resourcesPanel.repaint();
                resourcesPanel.revalidate();
            });

            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(layout.createParallelGroup().addGroup(layout.createSequentialGroup()
                    .addComponent(buttonDelete,10,10,10).addComponent(tfResource).addGap(2).addComponent(tfAmount,70,70,70).addGap(2).addComponent(cbDescription,100,100,1000).addComponent(checkBoxConsumed))
                    .addGroup(layout.createSequentialGroup()
                            .addGap(5).addComponent(labelDebit).addComponent(textFieldDebit).addGap(5).addComponent(labelDebitInfo).addComponent(textFieldDebitInfo)));
            layout.setVerticalGroup(layout.createSequentialGroup().addGap(2).addGroup(layout.createParallelGroup()
                    .addComponent(buttonDelete,25,25,25).addComponent(tfResource,25,25,25).addComponent(tfAmount,25,25,25).addComponent(cbDescription,25,25,25).addComponent(checkBoxConsumed,25,25,25))
                    .addGroup(layout.createParallelGroup()
                            .addComponent(labelDebit,25,25,25).addComponent(textFieldDebit,25,25,25).addComponent(labelDebitInfo,25,25,25).addComponent(textFieldDebitInfo,25,25,25))
                    .addGap(2));
        }

        String getAmount(){
            return tfAmount.getText();
        }

        String getResourceId(){
            return tfResource.getSelectedAccount();
        }

        String getDescription(){
            return ((JTextField)cbDescription.getEditor().getEditorComponent()).getText();
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

        private void updateDescriptions() {
            synchronized(lock) {
                String resource = tfResource.getSelectedAccount();
                if (!resource.trim().isEmpty()) {
                    List<String> descList = resourceDescriptionMap.get(resource);
                    if (descList != null) {
                        String cbValue = ((JTextField)cbDescription.getEditor().getEditorComponent()).getText();
                        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) cbDescription.getModel();
                        model.removeAllElements();
                        descList.forEach(model::addElement);
                        ((JTextField)cbDescription.getEditor().getEditorComponent()).setText(cbValue);
                    }
                }
            }
        }
    }

    public class ResourceData {
        private final String resourceId;
        private final String amount;
        private final String acqDescription;
        private final boolean isConsumed;
        private String debitId;
        private String debitInfo;

        ResourceData(String resourceId, String amount, String acqDescription) {
            this.resourceId = resourceId;
            this.amount = amount;
            this.acqDescription = acqDescription;
            isConsumed = true;
        }

        ResourceData(String resourceId, String amount, String acqDescription, String debitId, String debitInfo) {
            this.resourceId = resourceId;
            this.amount = amount;
            this.acqDescription = acqDescription;
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

        public String getAcqDescription() {
            return acqDescription;
        }
    }
}
