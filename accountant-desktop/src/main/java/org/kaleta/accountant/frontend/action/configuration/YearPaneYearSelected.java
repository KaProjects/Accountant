package org.kaleta.accountant.frontend.action.configuration;

import org.kaleta.accountant.frontend.core.*;
import org.kaleta.accountant.frontend.core.accounting.BalanceOverview;
import org.kaleta.accountant.frontend.core.accounting.CashFlowOverview;
import org.kaleta.accountant.frontend.core.accounting.ProfitOverview;
import org.kaleta.accountant.frontend.core.analysis.AssetDepreciationOverview;
import org.kaleta.accountant.frontend.core.budgeting.BudgetingPanel;
import org.kaleta.accountant.service.Service;

import javax.swing.*;

public class YearPaneYearSelected extends ConfigurationAction {
    private final YearPane pane;

    public YearPaneYearSelected(YearPane pane) {
        super(pane);
        this.pane = pane;
    }

    @Override
    protected void actionPerformed() {
        pane.removeAll();

        SchemaOverview schemaOverview = new SchemaOverview(getConfiguration());
        AccountsOverview accountsOverview = new AccountsOverview(getConfiguration());
        TransactionsOverview transactionsOverview = new TransactionsOverview(getConfiguration());

        if (getConfiguration().getSelectedYear().equals(Service.CONFIG.getActiveYear())){
            JTabbedPane schemaPane = new JTabbedPane();
            pane.addTab("Schema", schemaPane);
            schemaPane.addTab("Overview", new JScrollPane(schemaOverview));
            schemaPane.addTab("Editor", new JScrollPane(new SchemaEditor(getConfiguration())));

            JTabbedPane accountsPane = new JTabbedPane();
            pane.addTab("Accounts", accountsPane);
            accountsPane.addTab("Overview", new JScrollPane(accountsOverview));
            accountsPane.add("Assets", new AssetsEditor(getConfiguration()));
            accountsPane.add("Resources", new ResourcesEditor(getConfiguration()));
            accountsPane.add("Finance", new FinanceEditor(getConfiguration()));
            accountsPane.add("Relations",  new RelationsEditor(getConfiguration()));
            accountsPane.add("Funding", new FundingEditor(getConfiguration()));
            accountsPane.add("Expenses", new ExpensesEditor(getConfiguration()));
            accountsPane.add("Revenues", new RevenuesEditor(getConfiguration()));

            JTabbedPane transactionsPane = new JTabbedPane();
            pane.addTab("Transactions", transactionsPane);
            transactionsPane.addTab("Overview", new JScrollPane(transactionsOverview));
            transactionsPane.addTab("Editor", new TransactionsEditor(getConfiguration()));

        } else {
            pane.addTab("Schema", new JScrollPane(schemaOverview));
            pane.addTab("Accounts", new JScrollPane(accountsOverview));
            pane.addTab("Transactions", new JScrollPane(transactionsOverview));
        }

        JTabbedPane accountingPane = new JTabbedPane();
        pane.addTab("Accounting", accountingPane);
        accountingPane.addTab("Balance", new JScrollPane(new BalanceOverview(getConfiguration())));
        accountingPane.addTab("Profit", new JScrollPane(new ProfitOverview(getConfiguration())));
        accountingPane.addTab("Cash Flow", new JScrollPane(new CashFlowOverview(getConfiguration())));

        JTabbedPane analysisPane = new JTabbedPane();
        pane.addTab("Analysis", analysisPane);
        analysisPane.addTab("Assets Depreciation", new JScrollPane(new AssetDepreciationOverview(getConfiguration())));

        pane.addTab("Budgeting", new JScrollPane(new BudgetingPanel(getConfiguration())));



        pane.revalidate();
        pane.repaint();
    }
}
