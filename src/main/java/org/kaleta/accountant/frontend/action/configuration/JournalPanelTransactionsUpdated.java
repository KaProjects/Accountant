package org.kaleta.accountant.frontend.action.configuration;

import org.kaleta.accountant.backend.entity.Transaction;
import org.kaleta.accountant.frontend.component.JournalPanel;
import org.kaleta.accountant.frontend.component.TransactionPanel;
import org.kaleta.accountant.service.Service;

/**
 * Created by Stanislav Kaleta on 03.08.2016.
 */
public class JournalPanelTransactionsUpdated extends ConfigurationAction {
    private JournalPanel target;
    public JournalPanelTransactionsUpdated(JournalPanel target){
        super(target);
        this.target = target;
    }

    @Override
    protected void actionPerformed() {
        target.removeAll();
        target.revalidate();
        target.repaint();
        int year = 2016; // TODO: 8/3/16 service get year
        for (Transaction transaction : Service.JOURNAL.getYournal(year).getTransaction()){
            target.add(new TransactionPanel(transaction));
        }
        target.revalidate();
        target.repaint();
    }
}
