package org.kaleta.accountant.frontend.action.configuration;

import org.kaleta.accountant.frontend.component.JournalTable;
import org.kaleta.accountant.frontend.component.JournalTableModel;
import org.kaleta.accountant.service.Service;

/**
 * Created by Stanislav Kaleta on 03.08.2016.
 */
public class JournalPanelTransactionsUpdated extends ConfigurationAction {
    private JournalTable target;
    private JournalTableModel model;
    public JournalPanelTransactionsUpdated(JournalTable target, JournalTableModel model){
        super(target);
        this.target = target;
        this.model = model;
    }

    @Override
    protected void actionPerformed() {
        int year = 2016; // TODO: 8/8/16 Service get active year
        model.updateJournal(Service.JOURNAL.getYournal(year));
        target.revalidate();
        target.repaint();
    }
}
