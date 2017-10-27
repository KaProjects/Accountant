package org.kaleta.accountant.frontend.dep.dialog.schema;

import org.kaleta.accountant.backend.entity.Schema;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.frontend.common.NumberFilter;
import org.kaleta.accountant.frontend.dialog.Dialog;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 20.04.2016.
 */
class ClassDialog extends Dialog {
    private final Schema.Class clazz;
    private final List<Schema.Class.Group> groups;

    public ClassDialog(Schema.Class clazz, Frame parent) {
        super(parent, "Class Editor");
        this.clazz = clazz;
        groups = new ArrayList<>(clazz.getGroup());
        buildDialog();
        pack();
    }

    @Override
    protected void buildDialog() {
        JLabel labelId = new JLabel("Class ID:");
        JTextField tfId = new JTextField();
        ((PlainDocument) tfId.getDocument()).setDocumentFilter(new NumberFilter());
        tfId.setText(clazz.getId());

        JLabel labelName = new JLabel("Class Name:");
        JTextField tfName = new JTextField();
        tfName.setText(clazz.getName());

        JLabel labelTable = new JLabel("Groups:");
        JTable tableGroups = new JTable(new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return groups.size();
            }

            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public Object getValueAt(int r, int c) {
                switch (c) {
                    case 0:
                        return groups.get(r).getId();
                    case 1:
                        return groups.get(r).getName();
                    default:
                        return null;
                }
            }
        });
        tableGroups.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableColumnModel columnModel = tableGroups.getColumnModel();
        columnModel.getColumn(0).setMinWidth(15);
        columnModel.getColumn(0).setMaxWidth(15);
        columnModel.getColumn(1).setMinWidth(185);
        columnModel.getColumn(1).setPreferredWidth(185);

        JButton buttonAddGroup = new JButton(IconLoader.getIcon(IconLoader.ADD, new Dimension(10, 10)));
        buttonAddGroup.addActionListener(l -> {
            Schema.Class.Group newGroup = new Schema.Class.Group();
            GroupDialog dialog = new GroupDialog(newGroup, (Frame) ClassDialog.this.getParent());
            dialog.setVisible(true);
            if (dialog.getResult()) {
                groups.add(newGroup);
                tableGroups.revalidate();
                tableGroups.repaint();
                this.pack();
            }
        });
        JButton buttonEditGroup = new JButton(IconLoader.getIcon(IconLoader.EDIT, new Dimension(10, 10)));
        buttonEditGroup.addActionListener(l -> {
            int selectedRow = tableGroups.getSelectedRow();
            if (selectedRow >= 0) {
                GroupDialog dialog = new GroupDialog(groups.get(selectedRow), (Frame) ClassDialog.this.getParent());
                dialog.setVisible(true);
                if (dialog.getResult()) {
                    tableGroups.revalidate();
                    tableGroups.repaint();
                    this.pack();
                }
            }
        });
        JButton buttonRemoveGroup = new JButton(IconLoader.getIcon(IconLoader.DELETE, new Dimension(10, 10)));
        buttonRemoveGroup.addActionListener(l -> {
            int selectedRow = tableGroups.getSelectedRow();
            if (selectedRow >= 0) {
                groups.remove(selectedRow);
                tableGroups.revalidate();
                tableGroups.repaint();
                this.pack();
            }
        });
        JButton buttonOk = new JButton("OK");
        buttonOk.addActionListener(l -> {
            clazz.setId(tfId.getText());
            clazz.setName(tfName.getText());
            clazz.getGroup().clear();
            clazz.getGroup().addAll(groups);
            result = true;
            this.dispose();
        });
        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(l -> this.dispose());

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGap(10)
                .addGroup(layout.createParallelGroup().addComponent(labelId).addComponent(tfId, 25, 25, 25))
                .addGap(5)
                .addGroup(layout.createParallelGroup().addComponent(labelName).addComponent(tfName, 25, 25, 25))
                .addGap(5)
                .addComponent(labelTable)
                .addComponent(tableGroups)
                .addGroup(layout.createParallelGroup().addComponent(buttonRemoveGroup).addComponent(buttonEditGroup).addComponent(buttonAddGroup))
                .addGap(10)
                .addGroup(layout.createParallelGroup().addComponent(buttonCancel).addComponent(buttonOk))
                .addGap(10));
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGap(10)
                .addGroup(layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup().addComponent(labelId).addGap(5).addComponent(tfId))
                        .addGroup(layout.createSequentialGroup().addComponent(labelName).addGap(5).addComponent(tfName))
                        .addComponent(labelTable)
                        .addComponent(tableGroups)
                        .addGroup(layout.createSequentialGroup().addComponent(buttonRemoveGroup).addGap(5).addComponent(buttonEditGroup).addGap(5).addComponent(buttonAddGroup))
                        .addGroup(layout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(buttonCancel).addGap(5).addComponent(buttonOk)))
                .addGap(10));
    }
}
