package org.kaleta.accountant.frontend.action.configuration;

import org.kaleta.accountant.frontend.dep.JournalTable;
import org.kaleta.accountant.frontend.dep.JournalTableModel;

/**
 * Created by Stanislav Kaleta on 03.08.2016.
 */
@Deprecated
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
