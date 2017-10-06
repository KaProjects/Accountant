package org.kaleta.accountant.frontend.action.configuration;

import org.kaleta.accountant.frontend.dep.ProfitTable;
import org.kaleta.accountant.frontend.dep.ProfitTableModel;

/**
 * Created by Stanislav Kaleta on 10.08.2016.
 */
@Deprecated
public class ProfitTableTransactionsUpdated extends ConfigurationAction{
    private ProfitTable target;
    private ProfitTableModel model;

    public ProfitTableTransactionsUpdated(ProfitTable target, ProfitTableModel model) {
        super(target);
        this.target = target;
        this.model = model;
    }

    @Override
    protected void actionPerformed() {
        // todo model.update(Service.ACCOUNT.getSchemaForAccountType(AccountType.REVENUE),
        // todo        Service.ACCOUNT.getSchemaForAccountType(AccountType.EXPENSE),
        // todo        Service.JOURNAL.getJournal(getConfiguration().getActiveYear()));
        target.revalidate();
        target.repaint();
    }
}
