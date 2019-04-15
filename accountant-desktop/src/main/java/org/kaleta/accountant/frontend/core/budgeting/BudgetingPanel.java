package org.kaleta.accountant.frontend.core.budgeting;

import org.kaleta.accountant.backend.model.BudgetModel;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetingPanel extends JPanel implements Configurable {
    private Configuration configuration;

    private JRadioButton buttonBudget;

    private List<RowPanel> incomePanelList = new ArrayList<>();
    private JPanel panelIncome;
    private RowPanel incomeSumPanel;

    public static final class Type {
        public static final String INCOME = "income";
    }

    public BudgetingPanel(Configuration configuration){
        setConfiguration(configuration);
        initComponents();
        update();
    }

    private void initComponents(){
        buttonBudget = new JRadioButton("Budget");
        buttonBudget.addActionListener(l -> {
            for (RowPanel rowPanel : incomePanelList) rowPanel.setState(RowPanel.State.BUDGET);
            incomeSumPanel.setState(RowPanel.State.BUDGET);
        });
        JRadioButton buttonActual = new JRadioButton("Actual");
        buttonActual.addActionListener(l -> {
            for (RowPanel rowPanel : incomePanelList) rowPanel.setState(RowPanel.State.ACTUAL);
            incomeSumPanel.setState(RowPanel.State.ACTUAL);
        });
        JRadioButton buttonDiff = new JRadioButton("Diff.");
        buttonDiff.addActionListener(l -> {
            for (RowPanel rowPanel : incomePanelList) rowPanel.setState(RowPanel.State.DIFF);
            incomeSumPanel.setState(RowPanel.State.DIFF);
        });

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(buttonBudget);
        buttonGroup.add(buttonActual);
        buttonGroup.add(buttonDiff);
        buttonGroup.setSelected(buttonActual.getModel(), true);



        panelIncome = new JPanel();
        panelIncome.setLayout(new BoxLayout(panelIncome, BoxLayout.Y_AXIS));

        incomeSumPanel = new RowPanel(BudgetingPanel.Type.INCOME, "Net Income");







        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addComponent(buttonActual)
                        .addComponent(buttonBudget)
                        .addComponent(buttonDiff))
                .addComponent(panelIncome)
                .addComponent(incomeSumPanel));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(buttonActual)
                        .addComponent(buttonBudget)
                        .addComponent(buttonDiff))
                .addComponent(panelIncome)
                .addComponent(incomeSumPanel));

        this.getActionMap().put(Configuration.SCHEMA_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        this.getActionMap().put(Configuration.ACCOUNT_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        this.getActionMap().put(Configuration.TRANSACTION_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {update();
            }
        });
        this.getActionMap().put(Configuration.BUDGET_UPDATED, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
    }

    public void update() {
        Map<String, String> aMap = new HashMap<>();
        for (int m=1;m<=12;m++){
            if (m == 3) {
                aMap.put(String.valueOf(m), null);
                continue;
            }
            if (m == 2) {
                aMap.put(String.valueOf(m), "0");
                continue;
            }
            aMap.put(String.valueOf(m), "5000");
        }

        Map<String, List<BudgetModel.Row>> rowsByType = Service.BUDGET.getRowsByType(getConfiguration().getSelectedYear());

        incomePanelList.clear();
        panelIncome.removeAll();
        for (BudgetModel.Row row : rowsByType.get(Type.INCOME)){
            RowPanel panel = new RowPanel(this.getConfiguration(), row, aMap);
            if (buttonBudget.isSelected()) panel.setState(RowPanel.State.BUDGET);
            incomePanelList.add(panel);
            panelIncome.add(panel);
        }
        panelIncome.repaint();
        panelIncome.revalidate();

        incomeSumPanel.updateSumPanel(rowsByType.get(Type.INCOME), aMap);
        if (buttonBudget.isSelected()) incomeSumPanel.setState(RowPanel.State.BUDGET);

        incomeSumPanel.repaint();
        incomeSumPanel.revalidate();
    }




    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
}
