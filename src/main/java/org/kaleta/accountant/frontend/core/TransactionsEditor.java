package org.kaleta.accountant.frontend.core;

import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.listener.OpenAddTransactionDialog;
import org.kaleta.accountant.frontend.action.listener.OpenCreateProcedureDialog;
import org.kaleta.accountant.frontend.action.mouse.ProcedurePanelClicked;
import org.kaleta.accountant.frontend.common.WrapLayout;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TransactionsEditor extends JPanel implements Configurable {
    private Configuration configuration;

    private final JPanel panelProcedures;

    public TransactionsEditor(Configuration configuration) {
        this.configuration = configuration;
        JButton buttonAddTr = new JButton("Add Transaction(s)");
        buttonAddTr.addActionListener(new OpenAddTransactionDialog(this));

        JButton buttonCreateProcedure = new JButton("Create Procedure");
        buttonCreateProcedure.addActionListener(new OpenCreateProcedureDialog(this));

        panelProcedures = new JPanel();
        panelProcedures.setLayout(new WrapLayout(FlowLayout.LEFT));
        panelProcedures.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Constants.Color.OVERVIEW_CLASS.brighter()),
                "Created Procedures",
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
    }

    public void update(){
        panelProcedures.removeAll();
        for (ProceduresModel.Procedure procedure : Service.PROCEDURES.getProcedureList(getConfiguration().getSelectedYear())) {
            ProcedurePanel procedurePanel = new ProcedurePanel(procedure);
            panelProcedures.add(procedurePanel);
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
        ProcedurePanel(ProceduresModel.Procedure procedure){
            this.setBorder(BorderFactory.createLineBorder(Constants.Color.OVERVIEW_GROUP, 2, true));

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

            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(layout.createSequentialGroup().addGap(10).addComponent(label).addGap(10));
            layout.setVerticalGroup(layout.createSequentialGroup().addGap(10).addComponent(label).addGap(10));

            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    ProcedurePanel.this.setBackground(ProcedurePanel.this.getBackground().darker());
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    ProcedurePanel.this.setBackground(ProcedurePanel.this.getBackground().brighter());
                }
            });
            this.addMouseListener(new ProcedurePanelClicked(TransactionsEditor.this, procedure));
        }
    }
}
