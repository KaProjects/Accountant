package org.kaleta.accountant.frontend.action.configuration;

import org.kaleta.accountant.frontend.core.*;
import org.kaleta.accountant.service.Service;

import javax.swing.*;

public class YearPaneYearSelected extends ConfigurationAction {
    private YearPane pane;

    public YearPaneYearSelected(YearPane pane) {
        super(pane);
        this.pane = pane;
    }

    @Override
    protected void actionPerformed() {
        pane.removeAll();
        SchemaOverview schemaOverview = new SchemaOverview();
        schemaOverview.setConfiguration(getConfiguration());
        schemaOverview.update();
        AccountsOverview accountsOverview = new AccountsOverview();
        accountsOverview.setConfiguration(getConfiguration());
        accountsOverview.update();
        TransactionsOverview transactionsOverview = new TransactionsOverview();
        transactionsOverview.setConfiguration(getConfiguration());
        transactionsOverview.update();
        BalanceOverview balanceOverview = new BalanceOverview();
        balanceOverview.setConfiguration(getConfiguration());
        balanceOverview.update();
        ProfitOverview profitOverview = new ProfitOverview();
        profitOverview.setConfiguration(getConfiguration());
        profitOverview.update();

        if (pane.getConfiguration().getSelectedYear().equals(Service.CONFIG.getActiveYear())){
            JTabbedPane schemaPane = new JTabbedPane();
            pane.addTab("Schema", schemaPane);
            schemaPane.addTab("Overview", new JScrollPane(schemaOverview));
            SchemaEditor schemaEditor = new SchemaEditor(getConfiguration());
            schemaEditor.update();
            schemaPane.addTab("Editor", new JScrollPane(schemaEditor));

            JTabbedPane accountsPane = new JTabbedPane();
            pane.addTab("Accounts", accountsPane);
            accountsPane.addTab("Overview", new JScrollPane(accountsOverview));
            AssetsEditor assetsEditor = new AssetsEditor(getConfiguration());
            assetsEditor.update();
            accountsPane.add("Assets", assetsEditor);
            ResourcesEditor resourcesEditor = new ResourcesEditor(getConfiguration());
            accountsPane.add("Resources", resourcesEditor);
            FinanceEditor financeEditor = new FinanceEditor(getConfiguration());
            accountsPane.add("Finance", financeEditor);
            RelationsEditor relationsEditor = new RelationsEditor(getConfiguration());
            accountsPane.add("Relations", relationsEditor);
            FundingEditor fundingEditor = new FundingEditor(getConfiguration());
            accountsPane.add("Funding", fundingEditor);
            ExpensesEditor expensesEditor = new ExpensesEditor(getConfiguration());
            accountsPane.add("Expenses", expensesEditor);
            RevenuesEditor revenuesEditor = new RevenuesEditor(getConfiguration());
            accountsPane.add("Revenues", revenuesEditor);

            JTabbedPane accountingPane = new JTabbedPane();
            pane.addTab("Accounting", accountingPane);
            accountingPane.addTab("Journal", new JScrollPane(transactionsOverview));
            accountingPane.addTab("Balance", new JScrollPane(balanceOverview));
            accountingPane.addTab("Profit", new JScrollPane(profitOverview));


        } else {
            pane.addTab("Schema", new JScrollPane(schemaOverview));
            pane.addTab("Accounts", new JScrollPane(accountsOverview));
            pane.addTab("Journal", new JScrollPane(transactionsOverview));
            pane.addTab("Balance", new JScrollPane(balanceOverview));
            pane.addTab("Profit", new JScrollPane(profitOverview));
        }
        pane.revalidate();
        pane.repaint();
    }
}
