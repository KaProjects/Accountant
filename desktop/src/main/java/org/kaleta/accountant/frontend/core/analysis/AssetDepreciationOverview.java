package org.kaleta.accountant.frontend.core.analysis;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.table.TableStyle;
import org.kaleta.accountant.frontend.component.TableRow;
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
                accRow.addRowMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        accBody.setVisible(!accBody.isVisible());
                    }
                });
                groupBody.add(accBody);
            }
            groupRow.addRowMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    groupBody.setVisible(!groupBody.isVisible());
                }
            });
            assetBodyPanels.add(groupBody);
        }

        this.add(getSumPanelInstance(assetRow, true, assetBodyPanels.toArray(new JPanel[]{})));

        alignNameColumn();

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

    private class Row extends JPanel implements TableRow {
        /** Width every value cell asks for; they all share it, and they grow together beyond it. */
        private static final int VALUE_COLUMN_WIDTH = 250;

        private JPanel panelHeader;
        private int titleWidth;
        private int rowHeight;

        public Row(String title, int type, String value, String dep, String actual, String ratio) {
            // -1 is the heading, 0 the total of everything, 1 a group of assets, 2 one asset
            int level = type == -1 ? TableStyle.HEADING
                    : type == 0 ? TableStyle.TOTAL
                    : type == 1 ? TableStyle.CLASS : TableStyle.ACCOUNT;
            rowHeight = TableStyle.rowHeight(level);
            this.setBackground(TableStyle.background(level));

            panelHeader = new JPanel();
            panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.X_AXIS));
            panelHeader.setBorder(BorderFactory.createCompoundBorder(TableStyle.cellBorder(),
                    BorderFactory.createEmptyBorder(0, 8, 0, 0)));
            panelHeader.setOpaque(false);
            panelHeader.add(TableStyle.name(title, level));
            panelHeader.add(Box.createHorizontalStrut(10));
            // what this row alone would need; the overview widens every row to the longest of them
            titleWidth = panelHeader.getPreferredSize().width;

            // a grid, not a box: every value cell keeps the same width and they share the space
            // the name column does not take
            JPanel panelValues = new JPanel();
            panelValues.setLayout(new GridLayout(1, 0));
            panelValues.setOpaque(false);
            for (String cellValue : new String[]{value, dep, actual, ratio}) {
                JLabel cell = level == TableStyle.HEADING
                        ? TableStyle.heading(cellValue) : TableStyle.value(cellValue, level);
                if (level != TableStyle.HEADING) {
                    cell.setToolTipText(cellValue);
                }
                cell.setPreferredSize(new Dimension(VALUE_COLUMN_WIDTH, rowHeight));
                panelValues.add(cell);
            }

            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(layout.createSequentialGroup()
                    .addComponent(panelHeader, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelValues, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
            layout.setVerticalGroup(layout.createParallelGroup()
                    .addComponent(panelHeader, rowHeight, rowHeight, rowHeight)
                    .addComponent(panelValues, rowHeight, rowHeight, rowHeight));
        }

        @Override
        public int getTitleWidth() {
            return titleWidth;
        }

        @Override
        public void setNameColumnWidth(int width) {
            Dimension size = new Dimension(width, rowHeight);
            panelHeader.setMinimumSize(size);
            panelHeader.setPreferredSize(size);
            panelHeader.setMaximumSize(size);
        }

        @Override
        public void addRowMouseListener(java.awt.event.MouseListener listener) {
            attachToRow(this, listener);
        }

        private void attachToRow(Component component, java.awt.event.MouseListener listener) {
            component.addMouseListener(listener);
            if (component instanceof Container) {
                for (Component child : ((Container) component).getComponents()) {
                    attachToRow(child, listener);
                }
            }
        }
    }
}
