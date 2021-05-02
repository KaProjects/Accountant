package org.kaleta.accountant.frontend.core.accounting;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.AccountingRowPanel;

import javax.swing.*;

public class CashFlowOverview extends AccountingOverview implements Configurable {

    public CashFlowOverview(Configuration configuration){
        setConfiguration(configuration);
        update();
    }

    @Override
    public void update() {
        this.removeAll();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        int valuesType = AccountingRowPanel.VALUE_INIT_MONTHLY_BALANCE;

        this.add(new AccountingRowPanel(valuesType));

        AccountAggregate cfAggregate = AccountAggregate
                .create("Cash Flow")
                .increasing("20", "21", "23")
                .decreasing("22");

        AccountingRowPanel header = new AccountingRowPanel(getConfiguration(), cfAggregate, AccountingRowPanel.SUM, valuesType);

        this.add(getSumPanelInstance(header, false,
                getGroupPanelInstance("2", "0", AccountingRowPanel.CF, valuesType, true),
                getGroupPanelInstance("2", "1", AccountingRowPanel.CF, valuesType, true),
                getGroupPanelInstance("2", "3", AccountingRowPanel.CF, valuesType, true),
                getGroupPanelInstance("2", "2", AccountingRowPanel.CF, valuesType, false)));

        this.repaint();
        this.revalidate();
    }
}
