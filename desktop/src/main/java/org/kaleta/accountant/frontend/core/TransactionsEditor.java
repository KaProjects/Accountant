package org.kaleta.accountant.frontend.core;

import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.listener.OpenAddTransactionDialog;
import org.kaleta.accountant.frontend.action.listener.OpenCreateProcedureDialog;
import org.kaleta.accountant.frontend.action.listener.OpenEditProcedureDialog;
import org.kaleta.accountant.frontend.action.mouse.ProcedurePanelClicked;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.frontend.common.WrapLayout;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class TransactionsEditor extends JPanel implements Configurable {
    private Configuration configuration;

    private final JPanel panelProcedures;

    private List<ProceduresModel.Group> procedureGroups = new ArrayList<>();
    public TransactionsEditor(Configuration configuration) {
        setConfiguration(configuration);

        procedureGroups.addAll(Service.PROCEDURES.getProcedureGroupList(getConfiguration().getSelectedYear()));

        JButton buttonAddTr = new JButton("Add Transaction(s)");
        buttonAddTr.addActionListener(new OpenAddTransactionDialog(this));

        JButton buttonCreateProcedure = new JButton("Create Procedure");
        buttonCreateProcedure.addActionListener(new OpenCreateProcedureDialog(this));

        panelProcedures = new JPanel();
        panelProcedures.setLayout(new WrapLayout(FlowLayout.LEFT));
        panelProcedures.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Constants.Color.OVERVIEW_CLASS.brighter()),
                "Procedures",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new JLabel().getFont(),
                Constants.Color.OVERVIEW_CLASS));

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGap(5)
                .addGroup(layout.createParallelGroup().addComponent(buttonAddTr).addComponent(buttonCreateProcedure))
                .addGap(5)
                .addComponent(panelProcedures));
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup().addGap(5).addComponent(buttonAddTr).addGap(5).addComponent(buttonCreateProcedure))
                .addComponent(panelProcedures));

        this.getActionMap().put(Configuration.PROCEDURE_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                TransactionsEditor.this.update();
            }
        });

        update();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        panelProcedures.repaint();
        panelProcedures.revalidate();
    }

    public void update(){
        panelProcedures.removeAll();
        for (ProceduresModel.Group group : procedureGroups) {
            JPanel panelProcedureGroup = new JPanel();
            panelProcedureGroup.setLayout(new WrapLayout(FlowLayout.LEFT));
            panelProcedureGroup.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Constants.Color.OVERVIEW_GROUP.brighter()),
                    group.getName(),
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    new JLabel().getFont(),
                    Constants.Color.OVERVIEW_GROUP));
            panelProcedureGroup.setAlignmentX(Component.LEFT_ALIGNMENT);

            for (ProceduresModel.Group.Procedure procedure : group.getProcedure()){
                ProcedurePanel procedurePanel = new ProcedurePanel(procedure, group.getName());
                panelProcedureGroup.add(procedurePanel);
            }
            panelProcedures.add(panelProcedureGroup);
        }
        panelProcedures.repaint();
        panelProcedures.revalidate();
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    private class ProcedurePanel extends JPanel {
        ProcedurePanel(ProceduresModel.Group.Procedure procedure, String groupName){
            this.setBorder(BorderFactory.createLineBorder(Constants.Color.OVERVIEW_GROUP, 2, true));
            this.setBackground(Color.LIGHT_GRAY);

            JLabel label = new JLabel(procedure.getName());
            label.setForeground(Constants.Color.OVERVIEW_ACCOUNT);
            label.setFont(new Font(label.getFont().getName(), Font.BOLD, 20));
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    ((JPanel) e.getComponent().getAccessibleContext().getAccessibleParent()).dispatchEvent(e);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    ((JPanel) e.getComponent().getAccessibleContext().getAccessibleParent()).dispatchEvent(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    ((JPanel) e.getComponent().getAccessibleContext().getAccessibleParent()).dispatchEvent(e);
                }
            });
            JButton buttonEdit = new JButton(IconLoader.getIcon(IconLoader.EDIT, new Dimension(15,15)));
            buttonEdit.setBackground(Color.LIGHT_GRAY);
            buttonEdit.addActionListener(new OpenEditProcedureDialog(getConfiguration(), procedure, groupName));

            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(layout.createSequentialGroup().addGap(10).addComponent(label).addGap(10).addComponent(buttonEdit,15,15,15));
            layout.setVerticalGroup(layout.createParallelGroup()
                    .addGroup(layout.createSequentialGroup().addGap(10).addComponent(label).addGap(10))
                    .addComponent(buttonEdit,15,15,15));

            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    ProcedurePanel.this.setBackground(ProcedurePanel.this.getBackground().darker());
                    buttonEdit.setBackground(buttonEdit.getBackground().darker());
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    ProcedurePanel.this.setBackground(ProcedurePanel.this.getBackground().brighter());
                    buttonEdit.setBackground(buttonEdit.getBackground().brighter());
                }
            });
            this.addMouseListener(new ProcedurePanelClicked(TransactionsEditor.this, procedure));
        }
    }
}
