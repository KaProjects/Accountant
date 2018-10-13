package org.kaleta.accountant.frontend.core.analysis;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.core.accounting.AccountingOverview;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class AssetDepreciationOverview extends AccountingOverview {

    public AssetDepreciationOverview(Configuration configuration) {
        setConfiguration(configuration);
        update();
    }

    public void update() {
        this.removeAll();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        String year = getConfiguration().getSelectedYear();

        this.add(new Row("", -1, "Initial Value", "Accumulated Depreciation", "Actual Value", "Depreciation Ratio"));

        Integer assetBalance = Service.TRANSACTIONS.getSchemaIdPrefixBalance(year, "0");
        Integer assetDepBalance = Service.TRANSACTIONS.getSchemaIdPrefixBalance(year, "0" + Constants.Schema.ACCUMULATED_DEP_GROUP_ID);
        assetBalance = assetBalance - assetDepBalance;
        Integer assetActualBalance = assetBalance - assetDepBalance;
        Float assetDepRatio = Float.intBitsToFloat(assetDepBalance) / Float.intBitsToFloat(assetBalance);

        Row assetRow = new Row(Constants.Schema.CLASS_0_NAME, 0, String.valueOf(assetBalance), String.valueOf(assetDepBalance),
                String.valueOf(assetActualBalance), String.format("%.2f%%", assetDepRatio * 100f));

        java.util.List<JPanel> assetBodyPanels = new ArrayList<>();
        for (SchemaModel.Class.Group group : Service.SCHEMA.getSchemaClassMap(year).get(0).getGroup()) {
            if (group.getId().equals(Constants.Schema.ACCUMULATED_DEP_GROUP_ID)) continue;

            Integer groupBalance = Service.TRANSACTIONS.getSchemaIdPrefixBalance(year, "0" + group.getId());
            Integer groupDepBalance = Service.TRANSACTIONS.getSchemaIdPrefixBalance(year, "0" + Constants.Schema.ACCUMULATED_DEP_GROUP_ID + group.getId());
            Integer groupActualBalance = groupBalance - groupDepBalance;
            Float groupDepRatio = Float.intBitsToFloat(groupDepBalance) / Float.intBitsToFloat(groupBalance);

            Row groupRow = new Row(group.getName(), 0, String.valueOf(groupBalance), String.valueOf(groupDepBalance),
                    String.valueOf(groupActualBalance), String.format("%.2f%%", groupDepRatio * 100f));
            assetBodyPanels.add(groupRow);
            JPanel groupBody = getBodyPanelInstance();
            for (SchemaModel.Class.Group.Account account : group.getAccount()) {
                Integer accBalance = Service.TRANSACTIONS.getSchemaIdPrefixBalance(year, "0" + group.getId() + account.getId());
                Integer accDepBalance = Service.TRANSACTIONS.getAccountListBalance(year,
                        filterDepAccounts(year, "0" + Constants.Schema.ACCUMULATED_DEP_GROUP_ID + group.getId(), account.getId()));
                Integer accActualBalance = accBalance - accDepBalance;
                Float accDepRatio = Float.intBitsToFloat(accDepBalance) / Float.intBitsToFloat(accBalance);

                Row accRow = new Row(account.getName(), 1, String.valueOf(accBalance), String.valueOf(accDepBalance),
                        String.valueOf(accActualBalance), String.format("%.2f%%", accDepRatio * 100f));
                groupBody.add(accRow);

                JPanel accBody = getBodyPanelInstance();
                for (AccountsModel.Account acc : Service.ACCOUNT.getAccountsBySchemaId(year, "0" + group.getId() + account.getId())) {
                    Integer seAccBalance = Service.TRANSACTIONS.getAccountBalance(year, acc);
//                    if (seAccBalance == 0) continue;
                    Integer seAccDepBalance = Service.TRANSACTIONS.getAccountBalance(year, Service.ACCOUNT.getAccumulatedDepAccount(year, acc));
                    Integer seAccActualBalance = seAccBalance - seAccDepBalance;
                    Float seAccDepRatio = Float.intBitsToFloat(seAccDepBalance) / Float.intBitsToFloat(seAccBalance);

                    Row seAccRow = new Row(acc.getName(), 2, String.valueOf(seAccBalance), String.valueOf(seAccDepBalance),
                            String.valueOf(seAccActualBalance), String.format("%.2f%%", seAccDepRatio * 100f));
                    accBody.add(seAccRow);
                }
                accRow.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        accBody.setVisible(!accBody.isVisible());
                    }
                });
                groupBody.add(accBody);
            }
            groupRow.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    groupBody.setVisible(!groupBody.isVisible());
                }
            });
            assetBodyPanels.add(groupBody);
        }

        this.add(getSumPanelInstance(assetRow, true, assetBodyPanels.toArray(new JPanel[]{})));


        this.repaint();
        this.revalidate();
    }

    private java.util.List<AccountsModel.Account> filterDepAccounts(String year, String schemaId, String sematicIdPrefix) {
        java.util.List<AccountsModel.Account> filteredAccounts = new ArrayList<>();
        for (AccountsModel.Account account : Service.ACCOUNT.getAccountsBySchemaId(year, schemaId)) {
            if (account.getSemanticId().startsWith(sematicIdPrefix)) filteredAccounts.add(account);
        }
        return filteredAccounts;
    }

    private class Row extends JPanel {

        public Row(String title, int type, String value, String dep, String actual, String ratio) {
            int rowHeight;
            Color backgroundColor;
            Font cellValueFont;
            switch (type) {
                case -1: {
                    rowHeight = 25;
                    backgroundColor = Color.lightGray.darker();
                    cellValueFont = new Font(new JLabel().getFont().getName(), Font.BOLD, rowHeight - 5);
                    break;
                }
                case 0: {
                    rowHeight = 30;
                    backgroundColor = Color.lightGray.darker();
                    cellValueFont = new Font(new JLabel().getFont().getName(), Font.BOLD, rowHeight - 5);
                    break;
                }
                case 1: {
                    rowHeight = 25;
                    backgroundColor = Color.lightGray;
                    cellValueFont = new Font(new JLabel().getFont().getName(), Font.BOLD, rowHeight - 5);
                    break;
                }
                case 2: {
                    rowHeight = 20;
                    backgroundColor = Color.white;
                    cellValueFont = new Font(new JLabel().getFont().getName(), Font.BOLD, rowHeight - 5);
                    break;
                }
                default:
                    throw new IllegalArgumentException("illegal type");
            }
            int cellWidth = 250;

            this.setBackground(backgroundColor);

            JLabel labelName = new JLabel(" " + title);
            labelName.setFont(cellValueFont);

            JPanel panelHeader = new JPanel();
            panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.X_AXIS));
            panelHeader.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            panelHeader.setOpaque(false);
            panelHeader.add(labelName);


            JPanel panelValues = new JPanel();
            panelValues.setLayout(new BoxLayout(panelValues, BoxLayout.X_AXIS));
            panelValues.setOpaque(false);

            JLabel labelValue = (type == -1) ? new JLabel(value, SwingConstants.CENTER)
                    : new JLabel(value + " ", SwingConstants.RIGHT);
            if (type != -1) labelValue.setToolTipText(value);
            labelValue.setMinimumSize(new Dimension(cellWidth, rowHeight));
            labelValue.setPreferredSize(new Dimension(cellWidth, rowHeight));
            labelValue.setMaximumSize(new Dimension(cellWidth, rowHeight));
            labelValue.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            labelValue.setFont(cellValueFont);
            panelValues.add(labelValue);

            JLabel labelDep = (type == -1) ? new JLabel(dep, SwingConstants.CENTER)
                    : new JLabel(dep + " ", SwingConstants.RIGHT);
            if (type != -1) labelDep.setToolTipText(dep);
            labelDep.setMinimumSize(new Dimension(cellWidth, rowHeight));
            labelDep.setPreferredSize(new Dimension(cellWidth, rowHeight));
            labelDep.setMaximumSize(new Dimension(cellWidth, rowHeight));
            labelDep.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            labelDep.setFont(cellValueFont);
            panelValues.add(labelDep);

            JLabel labelActual = (type == -1) ? new JLabel(actual, SwingConstants.CENTER)
                    : new JLabel(actual + " ", SwingConstants.RIGHT);
            if (type != -1) labelActual.setToolTipText(actual);
            labelActual.setMinimumSize(new Dimension(cellWidth, rowHeight));
            labelActual.setPreferredSize(new Dimension(cellWidth, rowHeight));
            labelActual.setMaximumSize(new Dimension(cellWidth, rowHeight));
            labelActual.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            labelActual.setFont(cellValueFont);
            panelValues.add(labelActual);

            JLabel labelRatio = (type == -1) ? new JLabel(ratio, SwingConstants.CENTER)
                    : new JLabel(ratio + " ", SwingConstants.RIGHT);
            if (type != -1) labelRatio.setToolTipText(ratio);
            labelRatio.setMinimumSize(new Dimension(cellWidth, rowHeight));
            labelRatio.setPreferredSize(new Dimension(cellWidth, rowHeight));
            labelRatio.setMaximumSize(new Dimension(cellWidth, rowHeight));
            labelRatio.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            labelRatio.setFont(cellValueFont);
            panelValues.add(labelRatio);


            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(layout.createSequentialGroup()
                    .addComponent(panelHeader, 320, 320, Short.MAX_VALUE)
                    .addComponent(panelValues));
            layout.setVerticalGroup(layout.createParallelGroup()
                    .addComponent(panelHeader, rowHeight, rowHeight, rowHeight)
                    .addComponent(panelValues, rowHeight, rowHeight, rowHeight));
        }

    }
}
