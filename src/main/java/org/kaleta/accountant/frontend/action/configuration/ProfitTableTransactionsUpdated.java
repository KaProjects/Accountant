package org.kaleta.accountant.frontend.action.configuration;

import org.kaleta.accountant.backend.constants.AccountType;
import org.kaleta.accountant.frontend.component.ProfitTable;
import org.kaleta.accountant.frontend.component.ProfitTableModel;
import org.kaleta.accountant.service.Service;

/**
 * Created by Stanislav Kaleta on 10.08.2016.
 */
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
        model.update(Service.ACCOUNT.getSchemaForAccountType(AccountType.REVENUE),
                Service.ACCOUNT.getSchemaForAccountType(AccountType.EXPENSE),
                Service.JOURNAL.getYournal(2016)); // // TODO: 8/9/16 get year from service
        target.revalidate();
        target.repaint();
    }
}
