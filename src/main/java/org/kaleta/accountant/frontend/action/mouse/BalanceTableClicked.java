package org.kaleta.accountant.frontend.action.mouse;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.dep.BalanceTableModel;
import org.kaleta.accountant.frontend.dep.ProfitTableModel;

import javax.swing.*;
import java.awt.event.MouseEvent;

/**
 * Created by Stanislav Kaleta on 20.08.2016.
 */
@Deprecated
public class BalanceTableClicked extends MouseAction {
    private JTable table;
    private Boolean isBalance;

    public BalanceTableClicked(JTable table, Boolean isBalance) {
        super((Configurable) table);
        this.table = table;
        this.isBalance = isBalance;
    }

    @Override
    protected void actionPerformed(MouseEvent e) {
        int column = table.getSelectedColumn();
        int row = table.getSelectedRow();
        String schemaId = (isBalance) ?
                ((BalanceTableModel) table.getModel()).getCellSchemaId(row, column) :
                ((ProfitTableModel) table.getModel()).getCellSchemaId(row, column);
        if (schemaId != null){
            //todo AccountPreviewModel model = new AccountPreviewModel(schemaId, getConfiguration().getActiveYear());
            //todo AccountPreviewFrame frame = new AccountPreviewFrame((Component) getConfiguration(), model);
            //todo frame.setVisible(true);
        }
    }
}
