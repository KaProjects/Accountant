package org.kaleta.accountant.frontend.core;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.configuration.ConfigurationAction;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.List;

public class AccountsOverview extends JPanel implements Configurable {
    private Configuration configuration;

    private DefaultTreeModel accountTreeModel;
    private JPanel accountOverviewPanel;

    private String selectedSchemaAccount;

    public AccountsOverview() {
        accountTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode("root"));
        JTree tree = new JTree(accountTreeModel);
        tree.setRootVisible(false);
        tree.setToggleClickCount(1);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                c.setFont(new Font(c.getFont().getName(), Font.BOLD, 15));
                setIcon(null);
                switch (((DefaultMutableTreeNode) value).getLevel()) {
                    case 1: {
                        c.setForeground(Constants.Color.OVERVIEW_CLASS);
                        break;
                    }
                    case 2: {
                        c.setForeground(Constants.Color.OVERVIEW_GROUP);
                        break;
                    }
                    case 3: {
                        c.setForeground(Constants.Color.OVERVIEW_ACCOUNT);
                        break;
                    }
                }
                return c;
            }
        });
        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node instanceof SchemaAccountTreeNode) {
                selectedSchemaAccount = ((SchemaAccountTreeNode) node).getSchemaId();
            } else {
                selectedSchemaAccount = "-1";
            }
            updateAccountOverview();
        });
        JScrollPane accountTreeScrollPane = new JScrollPane(tree);

        accountOverviewPanel = new JPanel();
        accountOverviewPanel.setLayout(new BoxLayout(accountOverviewPanel, BoxLayout.Y_AXIS));
        JScrollPane accountOverviewScrollPane = new JScrollPane(accountOverviewPanel);

        this.setLayout(new GridLayout(1, 1));
        this.add(accountTreeScrollPane);
        this.add(accountOverviewScrollPane);

        this.getActionMap().put(Configuration.SCHEMA_UPDATED, new ConfigurationAction(this) {
            @Override
            protected void actionPerformed() {
                AccountsOverview.this.update();
            }
        });

        this.getActionMap().put(Configuration.ACCOUNT_UPDATED, new ConfigurationAction(this) {
            @Override
            protected void actionPerformed() {
                AccountsOverview.this.updateAccountOverview();
            }
        });
    }

    public void update() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) accountTreeModel.getRoot();
        root.removeAllChildren();
        for (SchemaModel.Class clazz : Service.SCHEMA.getSchemaClassList(getConfiguration().getSelectedYear())) {
            if (clazz.getId().equals("7")) continue;
            DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(clazz.getName());
            root.add(classNode);
            for (SchemaModel.Class.Group group : clazz.getGroup()) {
                DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group.getName());
                classNode.add(groupNode);
                for (SchemaModel.Class.Group.Account account : group.getAccount()) {
                    SchemaAccountTreeNode accountNode = new SchemaAccountTreeNode(account.getName());
                    accountNode.setSchemaId(clazz.getId() + group.getId() + account.getId());
                    groupNode.add(accountNode);
                }
            }
        }
        accountTreeModel.setRoot(root);

        selectedSchemaAccount = "-1";
        updateAccountOverview();

        this.revalidate();
        this.repaint();
    }

    private void updateAccountOverview() {
        accountOverviewPanel.removeAll();
        if (selectedSchemaAccount.equals("-1")) {
            this.revalidate();
            this.repaint();
            return;
        }

        List<AccountsModel.Account> accountList = Service.ACCOUNT.getAccountsBySchemaId(getConfiguration().getSelectedYear(),selectedSchemaAccount);
        if (accountList.size() == 0) {
            accountOverviewPanel.add(new JLabel("Selected Schema Account has no accounts assigned."));
        } else {
            for (AccountsModel.Account account : accountList) {
                String text = account.getSchemaId() + "." + account.getSemanticId() + " '" + account.getName() + "'  turnover="
                        +Service.ACCOUNT.getAccountTurnover(getConfiguration().getSelectedYear(), account) + "  balance="
                        +Service.ACCOUNT.getAccountBalance(getConfiguration().getSelectedYear(), account);
                accountOverviewPanel.add(new JLabel(text));
                // TODO post 1.0 : design&impl
            }
        }
        this.revalidate();
        this.repaint();
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    private class SchemaAccountTreeNode extends DefaultMutableTreeNode {
        private String schemaId;

        SchemaAccountTreeNode(String title) {
            super(title);
        }

        public String getSchemaId() {
            return schemaId;
        }

        public void setSchemaId(String schemaId) {
            this.schemaId = schemaId;
        }
    }
}
