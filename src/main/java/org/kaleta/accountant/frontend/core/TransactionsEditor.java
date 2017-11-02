package org.kaleta.accountant.frontend.core;

import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.listener.OpenCreateProcedureDialog;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class TransactionsEditor extends JPanel implements Configurable {
    private Configuration configuration;

    private List<ProcedurePanel> procedurePanelList;
    private JPanel panelProcedures;

    public TransactionsEditor(){
        procedurePanelList = new ArrayList<>();

        JButton buttonAddTr = new JButton();
        buttonAddTr.addActionListener(actionEvent -> {

        });

        JButton createProcedure = new JButton("Create Procedure");
        createProcedure.addActionListener(new OpenCreateProcedureDialog(this));


        panelProcedures = new JPanel();
        panelProcedures.setLayout(new BoxLayout(panelProcedures, BoxLayout.Y_AXIS));





        // TODO: 10/31/17 procedures list (design & impl backend, service, frontend)


        this.getActionMap().put(Configuration.PROCEDURE_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                TransactionsEditor.this.update();
            }
        });
    }

    public void update(){
        procedurePanelList.clear();
        panelProcedures.removeAll();
        for (ProceduresModel.Procedure procedure : Service.PROCEDURES.getProcedureList(getConfiguration().getSelectedYear())) {
            ProcedurePanel procedurePanel = new ProcedurePanel(procedure);
            panelProcedures.add(procedurePanel);
            procedurePanelList.add(procedurePanel);
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

    private class ProcedurePanel extends JPanel{

        ProcedurePanel(ProceduresModel.Procedure procedure){
            this.add(new JLabel(procedure.getName()));
        }

    }
}
