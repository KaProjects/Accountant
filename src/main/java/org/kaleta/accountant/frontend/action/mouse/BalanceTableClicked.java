package org.kaleta.accountant.frontend.action.mouse;

import org.kaleta.accountant.backend.entity.Transaction;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.component.BalanceTableModel;
import org.kaleta.accountant.frontend.component.ProfitTableModel;
import org.kaleta.accountant.frontend.dialog.account.AccountModel;
import org.kaleta.accountant.frontend.dialog.account.AccountPreviewDialog;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 20.08.2016.
 */
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
            String accType = Service.ACCOUNT.getAccountType(schemaId);
            String accName = Service.ACCOUNT.getAccountName(schemaId);
            List<Transaction> transactionList = Service.JOURNAL.listAccountTransactions(schemaId, 2016);
            // TODO: 6/3/16 get year
            List<Transaction> trDebit = new ArrayList<>();
            List<Transaction> trCredit = new ArrayList<>();
            for (Transaction transaction : transactionList){
                if (transaction.getDebit().startsWith(schemaId)){
                    trDebit.add(transaction);
                }
                if (transaction.getCredit().startsWith(schemaId)){
                    trCredit.add(transaction);
                }
            }
            AccountModel model = new AccountModel(accType, accName, trDebit, trCredit, 0);
            // TODO: 6/3/16 get INITIAL STATE
            AccountPreviewDialog dialog = new AccountPreviewDialog((Component) getConfiguration(), model);
            dialog.setVisible(true);
        }
    }
}
