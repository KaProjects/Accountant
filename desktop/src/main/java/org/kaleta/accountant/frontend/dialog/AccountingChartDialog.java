package org.kaleta.accountant.frontend.dialog;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.core.accounting.AccountAggregate;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class AccountingChartDialog extends JDialog {

    private AccountAggregate aggregate;
    private ChartPanel chartPanel;

    public AccountingChartDialog(Configuration configuration, AccountAggregate aggregate) {
        super((Frame) configuration, "Accounting Chart", false);
        this.aggregate = aggregate;

        buildDialogContent();
        pack();
    }

    private void buildDialogContent() {
        String[] years = Service.CONFIG.getYears().toArray(new String[0]);
        String currentYear = years[years.length - 1];

        JComboBox<String> fromYearCb = new JComboBox<>(years);
        fromYearCb.setSelectedIndex(years.length - 1);
        JComboBox<String> toYearCb = new JComboBox<>(new String[]{currentYear});
        toYearCb.setSelectedIndex(0);

        JComboBox<AccountAggregateCbModel> accountsCb = new JComboBox<>();
        if (aggregate.isSingleClass()) {
            accountsCb.addItem(new AccountAggregateCbModel(aggregate));
            for (SchemaModel.Class.Group group : Service.SCHEMA.getSchemaClassMap(currentYear).get(Integer.parseInt(aggregate.getSchemaId())).getGroup()) {
                AccountAggregate groupAgg = AccountAggregate.create(group.getName()).increasing(aggregate.getSchemaId() + group.getId());
                accountsCb.addItem(new AccountAggregateCbModel(groupAgg));
            }
        } else if (aggregate.isSingleGroup()) {
            aggregate.setName(Service.SCHEMA.getClassName(currentYear, aggregate.getSchemaId()) + " - " + Service.SCHEMA.getGroupName(currentYear, aggregate.getSchemaId()));
            accountsCb.addItem(new AccountAggregateCbModel(aggregate));
            for (SchemaModel.Class.Group.Account account : Service.SCHEMA.getGroup(currentYear, aggregate.getClassId(), aggregate.getGroupId()).getAccount()) {
                AccountAggregate accountAgg = AccountAggregate.create(account.getName()).increasing(aggregate.getSchemaId() + account.getId());
                accountsCb.addItem(new AccountAggregateCbModel(accountAgg));
            }
        } else if (aggregate.isSingleAccount()) {
            aggregate.setName(Service.SCHEMA.getClassName(currentYear, aggregate.getSchemaId()) + " - " + Service.SCHEMA.getGroupName(currentYear, aggregate.getSchemaId()) + " - " + Service.SCHEMA.getAccountName(currentYear, aggregate.getSchemaId()));
            accountsCb.addItem(new AccountAggregateCbModel(aggregate));
            for (AccountsModel.Account account : Service.ACCOUNT.getAccountsBySchemaId(currentYear, aggregate.getSchemaId())) {
                AccountAggregate accountAgg = AccountAggregate.create(account.getName()).increasing(account.getFullId());
                accountsCb.addItem(new AccountAggregateCbModel(accountAgg));
            }
        } else if (!aggregate.isSingleSchemaId()) {
            accountsCb.addItem(new AccountAggregateCbModel(aggregate));
            if (!aggregate.getName().equals("Profit") && !aggregate.getName().equals("Debit Sum") && !aggregate.getName().equals("Credit Sum")) {
                for (String schemaId : aggregate.getIncreasingSchemaIds()) {
                    AccountAggregate increasingGroupAgg = AccountAggregate.create(Service.SCHEMA.getGroupName(currentYear, schemaId)).increasing(schemaId);
                    accountsCb.addItem(new AccountAggregateCbModel(increasingGroupAgg));
                }
                for (String schemaId : aggregate.getDecreasingSchemaIds()) {
                    AccountAggregate decreasingGroupAgg = AccountAggregate.create(Service.SCHEMA.getGroupName(currentYear, schemaId)).increasing(schemaId);
                    accountsCb.addItem(new AccountAggregateCbModel(decreasingGroupAgg));
                }
            }
        } else {
            throw new IllegalArgumentException("unknown aggregate " + aggregate.toString());
        }

        JComboBox<String> optionCb = new JComboBox<>();
        optionCb.addItem("Cumulative");
        optionCb.addItem("Balance");
        optionCb.addItem("Turnover");
        if (aggregate.isSingleSchemaId()) {
            if (aggregate.getSchemaId().startsWith("5") || aggregate.getSchemaId().startsWith("6")) {
                optionCb.setSelectedIndex(1);
            }
            if (aggregate.getSchemaId().startsWith("1")) {
                optionCb.setSelectedIndex(2);
            }
        } else {
            String firstGroup = aggregate.getIncreasingSchemaIds()[0];
            if (firstGroup.startsWith("5") || firstGroup.startsWith("6") || firstGroup.startsWith("2")) {
                optionCb.setSelectedIndex(1);
            }
        }

        fromYearCb.addActionListener(e -> {
            Object toYearSelected = toYearCb.getSelectedItem();
            ActionListener listener = toYearCb.getActionListeners()[0];
            toYearCb.removeActionListener(listener);
            toYearCb.setModel(new DefaultComboBoxModel<>(Arrays.copyOfRange(years, fromYearCb.getSelectedIndex(), years.length)));
            toYearCb.setSelectedItem(toYearSelected);
            toYearCb.addActionListener(listener);
            updateChartData(Arrays.copyOfRange(years, fromYearCb.getSelectedIndex(), toYearCb.getSelectedIndex() + 1 + years.length - toYearCb.getItemCount()),
                    ((AccountAggregateCbModel) accountsCb.getSelectedItem()).getAggregate(), optionCb.getSelectedIndex());
        });
        toYearCb.addActionListener(e -> {
            Object fromYearSelected = fromYearCb.getSelectedItem();
            ActionListener listener = fromYearCb.getActionListeners()[0];
            fromYearCb.removeActionListener(listener);
            fromYearCb.setModel(new DefaultComboBoxModel<>(Arrays.copyOfRange(years, 0, toYearCb.getSelectedIndex() + 1 + years.length - toYearCb.getItemCount())));
            fromYearCb.setSelectedItem(fromYearSelected);
            fromYearCb.addActionListener(listener);
            updateChartData(Arrays.copyOfRange(years, fromYearCb.getSelectedIndex(), toYearCb.getSelectedIndex() + 1 + years.length - toYearCb.getItemCount()),
                    ((AccountAggregateCbModel) accountsCb.getSelectedItem()).getAggregate(), optionCb.getSelectedIndex());
        });
        accountsCb.addActionListener(e -> {
            updateChartData(Arrays.copyOfRange(years, fromYearCb.getSelectedIndex(), toYearCb.getSelectedIndex() + 1 + years.length - toYearCb.getItemCount()),
                    ((AccountAggregateCbModel) accountsCb.getSelectedItem()).getAggregate(), optionCb.getSelectedIndex());
        });
        optionCb.addActionListener(e -> {
            updateChartData(Arrays.copyOfRange(years, fromYearCb.getSelectedIndex(), toYearCb.getSelectedIndex() + 1 + years.length - toYearCb.getItemCount()),
                    ((AccountAggregateCbModel) accountsCb.getSelectedItem()).getAggregate(), optionCb.getSelectedIndex());
        });


        chartPanel = new ChartPanel(null);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
        updateChartData(Arrays.copyOfRange(years, fromYearCb.getSelectedIndex(), toYearCb.getSelectedIndex() + 1 + years.length - toYearCb.getItemCount()),
                ((AccountAggregateCbModel) accountsCb.getSelectedItem()).getAggregate(), optionCb.getSelectedIndex());


        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addComponent(fromYearCb, 60, 60, 60)
                        .addComponent(toYearCb, 60, 60, 60)
                        .addComponent(optionCb, 100, 100, 100)
                        .addComponent(accountsCb))
                .addGroup(layout.createSequentialGroup()
                        .addComponent(chartPanel))
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(fromYearCb, 25, 25, 25)
                        .addComponent(toYearCb, 25, 25, 25)
                        .addComponent(optionCb, 25, 25, 25)
                        .addComponent(accountsCb, 25, 25, 25))
                .addGroup(layout.createParallelGroup()
                        .addComponent(chartPanel))
        );
    }

    private void updateChartData(String[] years, AccountAggregate aggregate, int option) {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (String year : years) {
            Integer[] yearValues;

            switch (option) {
                case 0:
                    yearValues = aggregate.getMonthlyCumulativeBalance(year);
                    break;
                case 1:
                    yearValues = aggregate.getMonthlyBalance(year);
                    break;
                case 2:
                    yearValues = aggregate.getMonthlyTurnover(year);
                    break;
                default:
                    throw new IllegalArgumentException("unknown option " + option);
            }

            for (int i = 0; i < yearValues.length; i++) {
                dataset.setValue(yearValues[i], "month", (i + 1) + "/" + year);
            }

        }


        JFreeChart barChart = ChartFactory.createBarChart(
                null,
                null,
                "CZK",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);
        chartPanel.setChart(barChart);

    }

    @Override
    public void setVisible(boolean b) {
        this.setLocation(getParent().getLocationOnScreen().x + getParent().getSize().width / 2 - getSize().width / 2,
                getParent().getLocationOnScreen().y + getParent().getSize().height / 2 - getSize().height / 2);
        super.setVisible(b);
    }

    private class AccountAggregateCbModel {
        private final AccountAggregate aggregate;

        public AccountAggregateCbModel(AccountAggregate aggregate) {
            this.aggregate = aggregate;
        }

        public AccountAggregate getAggregate() {
            return aggregate;
        }

        @Override
        public String toString() {
            return aggregate.getName();
        }
    }
}
