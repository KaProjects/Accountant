package org.kaleta.accountant.frontend.core.accounting;

import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.AccountingRowPanel;

import javax.swing.*;

public class ProfitOverview extends AccountingOverview {

    public ProfitOverview(Configuration configuration) {
        setConfiguration(configuration);
        update();
    }

    public void update() {
        this.removeAll();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        String year = getConfiguration().getSelectedYear();
        int valuesType = AccountingRowPanel.VALUE_MONTHLY_BALANCE;

        this.add(new AccountingRowPanel(valuesType));


        AccountAggregate netWorkIncomeAgg = AccountAggregate.create("Cisty Pracovny Prijem").increasing("60", "63").decreasing("55");

        AccountingRowPanel nwiHeader = new AccountingRowPanel(getConfiguration(), netWorkIncomeAgg, AccountingRowPanel.SUM, valuesType);
        this.add(getSumPanelInstance(nwiHeader, false,
                getGroupPanelInstance("6", "0", AccountingRowPanel.REVENUE, valuesType, true),
                getGroupPanelInstance("5", "5", AccountingRowPanel.EXPENSE, valuesType, true),
                getGroupPanelInstance("6", "3", AccountingRowPanel.REVENUE, valuesType, true)));


        AccountAggregate operatingBalanceAgg = AccountAggregate.create("Prevadzkovy Zisk").increasing("60", "63").decreasing("55", "50", "51", "52", "53");

        AccountingRowPanel obHeader = new AccountingRowPanel(getConfiguration(), operatingBalanceAgg, AccountingRowPanel.SUM, valuesType);
        this.add(getSumPanelInstance(obHeader, false,
                getGroupPanelInstance("5", "0", AccountingRowPanel.EXPENSE, valuesType, true),
                getGroupPanelInstance("5", "1", AccountingRowPanel.EXPENSE, valuesType, true),
                getGroupPanelInstance("5", "2", AccountingRowPanel.EXPENSE, valuesType, true),
                getGroupPanelInstance("5", "3", AccountingRowPanel.EXPENSE, valuesType, true)));


        AccountAggregate netBalanceAgg = AccountAggregate.create("Cisty Zisk/Strata").increasing("60", "63", "61", "62").decreasing("55", "50", "51", "52", "53", "54", "56");

        AccountingRowPanel nbHeader = new AccountingRowPanel(getConfiguration(), netBalanceAgg, AccountingRowPanel.SUM, valuesType);
        this.add(getSumPanelInstance(nbHeader, false,
                getGroupPanelInstance("6", "1", AccountingRowPanel.REVENUE, valuesType, true),
                getGroupPanelInstance("5", "6", AccountingRowPanel.EXPENSE, valuesType, true),
                getGroupPanelInstance("6", "2", AccountingRowPanel.REVENUE, valuesType, true),
                getGroupPanelInstance("5", "4", AccountingRowPanel.EXPENSE, valuesType, true)));

        this.repaint();
        this.revalidate();
    }
}
