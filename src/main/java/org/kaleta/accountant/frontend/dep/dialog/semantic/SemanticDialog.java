package org.kaleta.accountant.frontend.dep.dialog.semantic;

import org.kaleta.accountant.backend.entity.Schema;
import org.kaleta.accountant.backend.entity.Semantic;
import org.kaleta.accountant.frontend.dialog.Dialog;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Stanislav Kaleta on 23.05.2016.
 */
public class SemanticDialog extends Dialog {
    private Schema schema;
    private Semantic semantic;

    public SemanticDialog(Component parent, Schema schema, Semantic semantic) {
        super(parent, "Semantic Accounts");
        this.schema = schema;
        this.semantic = semantic;
        buildDialog();
        this.pack();
    }

    @Override
    protected void buildDialog() {
        Map<String, List<Semantic.Account>> accountMap = new HashMap<>();
        for (Semantic.Account account : semantic.getAccount()){
            String schemaId = account.getSchemaId();
            if (accountMap.keySet().contains(schemaId)){
                accountMap.get(schemaId).add(account);
            } else {
                accountMap.put(schemaId, new ArrayList<>());
                accountMap.get(schemaId).add(account);
            }
        }

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        for (Schema.Class clazz : schema.getClazz()){
            DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(clazz.getName());
            root.add(classNode);
            for (Schema.Class.Group group : clazz.getGroup()){
                DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group.getName());
                classNode.add(groupNode);
                for (Schema.Class.Group.Account acc : group.getAccount()){
                    DefaultMutableTreeNode accNode = new DefaultMutableTreeNode(acc.getName());
                    groupNode.add(accNode);
                    String schemaId = clazz.getId() + group.getId() + acc.getId();
                    if (accountMap.keySet().contains(schemaId)){
                        for (Semantic.Account account : accountMap.get(schemaId)){
                            DefaultMutableTreeNode accountLeaf = new DefaultMutableTreeNode(account.getId() + " - " + account.getName());
                            accNode.add(accountLeaf);
                        }
                    }
                }
            }
        }

        JButton buttonAdd = new JButton("Add Account");
        JButton buttonDelete = new JButton("Delete Account");

        JTree tree = new JTree(root);
        tree.setRootVisible(false);
        tree.setCellRenderer(new DefaultTreeCellRenderer(){
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                Component c = super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,hasFocus);
                boolean isAcc = false;
                boolean isSem = false;
                if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
                    if (leaf){
                        c.setForeground(Color.BLACK);
                        c.setFont(new Font(new JLabel().getFont().getName(), Font.BOLD, new JLabel().getFont().getSize()));
                        isSem = true;
                    } else {
                        c.setFont(new JLabel().getFont());
                        if (((DefaultMutableTreeNode) value).getParent().getParent() == null){
                            c.setForeground(Color.getHSBColor(160 / 360f, 1f, 0.5f));
                        } else {
                            if (((DefaultMutableTreeNode) value).getParent().getParent().getParent() == null){
                                c.setForeground(Color.getHSBColor(200 / 360f, 1f, 0.5f));
                            } else {
                                c.setForeground(Color.getHSBColor(290 / 360f, 1f, 0.5f));
                                isAcc = true;
                            }
                        }
                    }
                }
                if (sel){
                    buttonAdd.setEnabled(isAcc);
                    buttonDelete.setEnabled(isSem);
                }
                return c;
            }
        });
        JScrollPane scrollPane = new JScrollPane(tree);

        buttonAdd.addActionListener(a -> {
            // // TODO: 5/23/16
            // add to tree
            // add to semantic
        });
        buttonAdd.setEnabled(false);

        buttonDelete.addActionListener(a -> {
            // TODO: 5/23/16
            // del from tree
            // del from semantic
        });
        buttonDelete.setEnabled(false);

        JButton buttonExpandAll = new JButton("Expand All");
        buttonExpandAll.addActionListener(a -> {
            for (int i = 0; i < tree.getRowCount(); i++) {
                tree.expandRow(i);
            }
        });

        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(a -> this.dispose());
        JButton buttonOk = new JButton("Save");
        buttonOk.addActionListener(a -> {
            result = true;
            dispose();
        });

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                    .addGap(10).addComponent(buttonExpandAll).addGap(10).addComponent(buttonAdd).addGap(10).addComponent(buttonDelete).addGap(5,5,Short.MAX_VALUE))
                .addComponent(scrollPane)
                .addGroup(layout.createSequentialGroup()
                        .addGap(5, 5, Short.MAX_VALUE).addComponent(buttonCancel).addGap(5).addComponent(buttonOk).addGap(5)));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGap(5)
                .addGroup(layout.createParallelGroup()
                    .addComponent(buttonExpandAll).addComponent(buttonAdd).addComponent(buttonDelete))
                .addGap(5)
                .addComponent(scrollPane)
                .addGap(5)
                .addGroup(layout.createParallelGroup()
                        .addComponent(buttonCancel).addComponent(buttonOk))
                .addGap(5));
    }
}
