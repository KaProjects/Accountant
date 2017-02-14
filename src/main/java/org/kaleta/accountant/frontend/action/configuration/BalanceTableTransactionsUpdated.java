package org.kaleta.accountant.frontend.action.configuration;

import org.kaleta.accountant.frontend.component.BalanceTable;
import org.kaleta.accountant.frontend.component.BalanceTableModel;

/**
 * Created by Stanislav Kaleta on 09.08.2016.
 */
public class BalanceTableTransactionsUpdated extends ConfigurationAction {
    private BalanceTable target;
    private BalanceTableModel model;

    public BalanceTableTransactionsUpdated(BalanceTable target, BalanceTableModel model) {
        super(target);
        this.target = target;
        this.model = model;
    }

    @Override
    protected void actionPerformed() {
        // todo model.update(Service.ACCOUNT.getSchemaForAccountType(AccountType.ASSET),
        // todo        Service.ACCOUNT.getSchemaForAccountType(AccountType.LIABILITY),
        // todo        Service.JOURNAL.getJournal(getConfiguration().getActiveYear()));
        target.revalidate();
        target.repaint();
    }
}
