package org.kaleta.accountant.frontend.action.configuration;

import org.kaleta.accountant.frontend.component.JournalTable;
import org.kaleta.accountant.frontend.component.JournalTableModel;

/**
 * Created by Stanislav Kaleta on 03.08.2016.
 */
public class JournalTableTransactionsUpdated extends ConfigurationAction {
    private JournalTable target;
    private JournalTableModel model;

    public JournalTableTransactionsUpdated(JournalTable target, JournalTableModel model){
        super(target);
        this.target = target;
        this.model = model;
    }

    @Override
    protected void actionPerformed() {
        //todo model.updateJournal(Service.JOURNAL.getJournal(getConfiguration().getActiveYear()));
        target.revalidate();
        target.repaint();
    }
}
