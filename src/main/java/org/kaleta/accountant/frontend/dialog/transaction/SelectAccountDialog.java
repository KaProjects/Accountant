package org.kaleta.accountant.frontend.dialog.transaction;

import org.kaleta.accountant.backend.entity.Schema;
import org.kaleta.accountant.backend.entity.Semantic;
import org.kaleta.accountant.common.ErrorHandler;
import org.kaleta.accountant.frontend.Initializer;
import org.kaleta.accountant.frontend.dialog.Dialog;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Stanislav Kaleta on 05.09.2016.
 */
public class SelectAccountDialog extends Dialog {
    private JTree tree;

    public SelectAccountDialog(Component parent, boolean isDebit) {
        super(parent, "Select " + ((isDebit)?"Debit":"Credit") + " Account");
        buildDialog();
        this.setSize(300,500);
    }

    @Override
    protected void buildDialog() {
        Map<String, List<Semantic.Account>> accountMap = new HashMap<>();
        for (Semantic.Account account : Service.DEPACCOUNT.getSemanticAccounts().getAccount()){
            String schemaId = account.getSchemaId();
            if (accountMap.keySet().contains(schemaId)){
                accountMap.get(schemaId).add(account);
            } else {
                accountMap.put(schemaId, new ArrayList<>());
                accountMap.get(schemaId).add(account);
            }
        }

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        for (Schema.Class clazz : Service.DEPACCOUNT.getSchema().getClazz()){
            DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(clazz.getName());
            root.add(classNode);
            for (Schema.Class.Group group : clazz.getGroup()){
                DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group.getName());
                classNode.add(groupNode);
                for (Schema.Class.Group.Account acc : group.getAccount()){
                    DefaultMutableTreeNode accNode = new DefaultMutableTreeNode(acc.getName());
                    groupNode.add(accNode);
                    accNode.add(new DefaultMutableTreeNode("0 - Default"));
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
        tree = new JTree(root);
        tree.setRootVisible(false);
        JScrollPane scrollPane = new JScrollPane(tree);

        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(a -> this.dispose());
        JButton buttonOk = new JButton("Select");
        buttonOk.addActionListener(a -> {
            result = true;
            dispose();
        });

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(scrollPane)
                .addGroup(layout.createSequentialGroup()
                        .addGap(5, 5, Short.MAX_VALUE).addComponent(buttonCancel).addGap(5).addComponent(buttonOk).addGap(5)));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGap(5)
                .addComponent(scrollPane)
                .addGap(5)
                .addGroup(layout.createParallelGroup()
                        .addComponent(buttonCancel).addComponent(buttonOk))
                .addGap(5));
    }

    public void preselectPath(String schemaId){
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getModel().getRoot();
        List<TreeNode> path = new ArrayList<>();
        path.add(node);
        // todo class starts with 0, rest starts with 1 - formalize that !
        if (schemaId.length() > 0) {
            node = (DefaultMutableTreeNode) node.getChildAt(Integer.parseInt(schemaId.substring(0,1)));
            path.add(node);
        }
        if (schemaId.length() > 1) {
            node = (DefaultMutableTreeNode) node.getChildAt(Integer.parseInt(schemaId.substring(1,2)) - 1); // todo from 1
            path.add(node);
        }
        if (schemaId.length() == 3) {
            node = (DefaultMutableTreeNode) node.getChildAt(Integer.parseInt(schemaId.substring(2,3)) - 1); // todo from 1
            path.add(node);
            path.add(node.getChildAt(0));
        }
        if (schemaId.length() == 5){
            node = (DefaultMutableTreeNode) node.getChildAt(Integer.parseInt(schemaId.substring(2,3)) - 1); // todo from 1
            path.add(node);
            path.add(node.getChildAt(Integer.parseInt(schemaId.substring(4,5))));
        }

        try {
            tree.expandPath(new TreePath(path.toArray()));
            tree.clearSelection();
            tree.addSelectionPath(new TreePath(path.toArray()));
        } catch (NumberFormatException | IndexOutOfBoundsException e){
            Initializer.LOG.warning(ErrorHandler.getThrowableStackTrace(e));
            ErrorHandler.getThrowableDialog(e).setVisible(true);
        }
    }

    public String getSelectedAcc(){
        String accountId = "";
        if (tree.getSelectionPath().getPathCount() > 1){
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) tree.getSelectionPath().getPath()[0];
            Integer index = parent.getIndex((TreeNode) tree.getSelectionPath().getPath()[1]);
            accountId += String.valueOf(index);
        }
        if (tree.getSelectionPath().getPathCount() > 2){
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) tree.getSelectionPath().getPath()[1];
            int index = parent.getIndex((TreeNode) tree.getSelectionPath().getPath()[2]);
            accountId += String.valueOf(index + 1); // todo from 1
        }
        if (tree.getSelectionPath().getPathCount() > 3){
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) tree.getSelectionPath().getPath()[2];
            int index = parent.getIndex((TreeNode) tree.getSelectionPath().getPath()[3]);
            accountId += String.valueOf(index + 1); // todo from 1
        }
        if (tree.getSelectionPath().getPathCount() > 4){
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) tree.getSelectionPath().getPath()[3];
            Integer index = parent.getIndex((TreeNode) tree.getSelectionPath().getPath()[4]);
            if (index != 0) {
                accountId += "-" + String.valueOf(index);
            }
        }
        return accountId;
    }
}
