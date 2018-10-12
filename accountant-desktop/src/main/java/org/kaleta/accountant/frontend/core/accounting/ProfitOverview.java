package org.kaleta.accountant.frontend.core.accounting;

import org.kaleta.accountant.common.Utils;
import org.kaleta.accountant.frontend.component.AccountingRowPanel;
import org.kaleta.accountant.service.Service;

import javax.swing.*;

public class ProfitOverview extends AccountingOverview {

    public ProfitOverview() {

    }

    public void update() {
        this.removeAll();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        String year = getConfiguration().getSelectedYear();
        int valuesType = AccountingRowPanel.VALUE_MONTHLY_BALANCE;

        this.add(new AccountingRowPanel(valuesType));

        Integer netWorkIncome = Service.TRANSACTIONS.getSchemaIdPrefixBalance(year, "60", "63")
                - Service.TRANSACTIONS.getSchemaIdPrefixBalance(year, "55");
        Integer[] netWorkIncomeMonthly = Utils.substractArrays(
                Service.TRANSACTIONS.getMonthlySchemaIdPrefixBalance(year, "60", "63"),
                Service.TRANSACTIONS.getMonthlySchemaIdPrefixBalance(year, "55"));
        AccountingRowPanel nwiHeader = new AccountingRowPanel("X", AccountingRowPanel.SUM, "Cisty Pracovny Prijem", netWorkIncome, netWorkIncomeMonthly);
        this.add(getSumPanelInstance(nwiHeader, false,
                getGroupPanelInstance("6", "0", AccountingRowPanel.REVENUE, valuesType),
                getGroupPanelInstance("5", "5", AccountingRowPanel.EXPENSE, valuesType),
                getGroupPanelInstance("6", "3", AccountingRowPanel.REVENUE, valuesType)));

        Integer operatingBalance = netWorkIncome - Service.TRANSACTIONS.getSchemaIdPrefixBalance(year, "50", "51", "52", "53");
        Integer[] operatingBalanceMonthly = Utils.substractArrays(netWorkIncomeMonthly,
                Service.TRANSACTIONS.getMonthlySchemaIdPrefixBalance(year, "50", "51", "52", "53"));
        AccountingRowPanel obHeader = new AccountingRowPanel("X", AccountingRowPanel.SUM, "Prevadzkovy Zisk", operatingBalance, operatingBalanceMonthly);
        this.add(getSumPanelInstance(obHeader, false,
                getGroupPanelInstance("5", "0", AccountingRowPanel.EXPENSE, valuesType),
                getGroupPanelInstance("5", "1", AccountingRowPanel.EXPENSE, valuesType),
                getGroupPanelInstance("5", "2", AccountingRowPanel.EXPENSE, valuesType),
                getGroupPanelInstance("5", "3", AccountingRowPanel.EXPENSE, valuesType)));

        Integer netBalance = operatingBalance + Service.TRANSACTIONS.getSchemaIdPrefixBalance(year, "61", "62")
                - Service.TRANSACTIONS.getSchemaIdPrefixBalance(year, "54", "56");
        Integer[] netBalanceMonthly = Utils.substractArrays(
                Utils.mergeArrays(operatingBalanceMonthly, Service.TRANSACTIONS.getMonthlySchemaIdPrefixBalance(year, "61", "62")),
                Service.TRANSACTIONS.getMonthlySchemaIdPrefixBalance(year, "54", "56"));
        AccountingRowPanel nbHeader = new AccountingRowPanel("X", AccountingRowPanel.SUM, "Cisty Zisk/Strata", netBalance, netBalanceMonthly);
        this.add(getSumPanelInstance(nbHeader, false,
                getGroupPanelInstance("6", "1", AccountingRowPanel.REVENUE, valuesType),
                getGroupPanelInstance("5", "6", AccountingRowPanel.EXPENSE, valuesType),
                getGroupPanelInstance("6", "2", AccountingRowPanel.REVENUE, valuesType),
                getGroupPanelInstance("5", "4", AccountingRowPanel.EXPENSE, valuesType)));

        this.repaint();
        this.revalidate();
    }
}
