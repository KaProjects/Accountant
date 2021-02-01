package org.kaleta.accountant.frontend.core.accounting;

import org.kaleta.accountant.common.Utils;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.AccountingRowPanel;
import org.kaleta.accountant.service.Service;

import javax.swing.*;

public class CashFlowOverview extends AccountingOverview {

    public CashFlowOverview(Configuration configuration){
        setConfiguration(configuration);
        update();
    }

    @Override
    public void update() {
        this.removeAll();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        String year = getConfiguration().getSelectedYear();
        int valuesType = AccountingRowPanel.VALUE_INIT_MONTHLY_BALANCE;

        this.add(new AccountingRowPanel(valuesType));

        Integer cashFlow = Service.TRANSACTIONS.getSchemaIdPrefixBalance(year, "20", "21", "23")
                - Service.TRANSACTIONS.getSchemaIdPrefixBalance(year, "22");

        Integer[] cashFlowMonthly = Utils.substractArrays(
                Service.TRANSACTIONS.getMonthlySchemaIdPrefixBalance(year, "20", "21", "23"),
                Service.TRANSACTIONS.getMonthlySchemaIdPrefixBalance(year, "22"));

        Integer initialCashFlow = Service.TRANSACTIONS.getSchemaIdPrefixInitialValue(year, "20", "21", "23")
                - Service.TRANSACTIONS.getSchemaIdPrefixInitialValue(year, "22");

        AccountingRowPanel header = new AccountingRowPanel("X", AccountingRowPanel.SUM, "Cash Flow", cashFlow, cashFlowMonthly, initialCashFlow);

        this.add(getSumPanelInstance(header, false,
                getGroupPanelInstance("2", "0", AccountingRowPanel.CF, valuesType, true),
                getGroupPanelInstance("2", "1", AccountingRowPanel.CF, valuesType, true),
                getGroupPanelInstance("2", "3", AccountingRowPanel.CF, valuesType, true),
                getGroupPanelInstance("2", "2", AccountingRowPanel.CF, valuesType, false)));

        this.repaint();
        this.revalidate();
    }
}
