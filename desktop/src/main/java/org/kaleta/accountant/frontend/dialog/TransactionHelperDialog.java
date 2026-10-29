package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.listener.CreateAnalyticalAccountAction;
import org.kaleta.accountant.frontend.common.WindowPlacement;
import org.kaleta.accountant.frontend.component.AccountsTree;
import org.kaleta.accountant.frontend.component.ProceduresTree;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The palette that sits beside a transaction dialog: everything that can be dropped into it.
 * <p>
 * On the left the procedures, on the right the chart of accounts - an account is dragged onto an
 * account field, a procedure onto the dialog itself, which books every transaction it holds. The
 * dialog stays open while transactions are composed, so it reads its trees again whenever it comes
 * back to the front: an account opened from here, or anywhere else, is there straight away.
 * <p>
 * Off balance accounts are left out. They are written by the year's opening and closing, never by
 * hand, so there is nothing here to drag.
 */
public class TransactionHelperDialog extends Dialog {
    private static final String OFF_BALANCE_CLASS_ID = "7";
    private static final String ASSETS_CLASS_ID = "0";

    private final AccountsTree accountsTree = new AccountsTree();
    private final ProceduresTree proceduresTree = new ProceduresTree();

    public TransactionHelperDialog(Configuration configuration) {
        super(configuration, "DnD Palette", "Close");
        setModal(false);
        buildDialogContent();
        hideCancelButton();
        setDialogValid(null);
        build();
        pack();
        setSize(1020, (int) (0.8f * Toolkit.getDefaultToolkit().getScreenSize().height));
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                refresh();
            }
        });
    }

    /**
     * Shown beside the dialog it serves rather than over it: what is dragged out of it has to be
     * dropped into that dialog, so the two stand side by side.
     */
    @Override
    void placeOnScreen() {
        Window beside = WindowPlacement.activeWindow();
        if (beside != null && beside.isShowing() && beside != this) {
            WindowPlacement.placeLeftOf(this, beside);
        } else {
            super.placeOnScreen();
        }
    }

    private void buildDialogContent() {
        // opening an account is the same action the editors use, so a credit or current account
        // opens the dialog that writes its procedure as well
        accountsTree.onCreate(schemaId -> new CreateAnalyticalAccountAction(this, schemaId, false)
                .onCreated(() -> SwingUtilities.invokeLater(this::refresh))
                .actionPerformed(new ActionEvent(accountsTree, ActionEvent.ACTION_PERFORMED, null)));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(proceduresTree), new JScrollPane(accountsTree));
        // both sides are read, not squeezed: the procedure names are the long ones, and the account
        // tree is wide enough for a deep account name without cutting it off
        split.setResizeWeight(0.53);
        split.setDividerLocation(540);

        setContent(layout -> {
            layout.setHorizontalGroup(layout.createParallelGroup().addComponent(split));
            layout.setVerticalGroup(layout.createSequentialGroup().addComponent(split));
        });
    }

    /** The first build of both trees; afterwards they are updated in place. */
    private void build() {
        String year = getConfiguration().getSelectedYear();
        // the assets are a long list that nothing is dragged from often, so they start collapsed
        accountsTree.show(chartWithoutOffBalance(year), Service.ACCOUNT.getAccountsViaSchemaMap(year), "", false,
                List.of(Service.SCHEMA.getSchemaClassMap(year).get(Integer.parseInt(ASSETS_CLASS_ID)).getName()));
        proceduresTree.show(Service.PROCEDURES.getProcedureGroupList(year));
    }

    /**
     * Picks up what was created elsewhere without disturbing the trees: an account opened from here
     * leaves everything the user expanded exactly as it was, and coming back from a dialog that was
     * cancelled changes nothing at all.
     */
    private void refresh() {
        String year = getConfiguration().getSelectedYear();
        accountsTree.update(chartWithoutOffBalance(year), Service.ACCOUNT.getAccountsViaSchemaMap(year));
        proceduresTree.update(Service.PROCEDURES.getProcedureGroupList(year));
    }

    private List<SchemaModel.Class> chartWithoutOffBalance(String year) {
        List<SchemaModel.Class> classes = new ArrayList<>(Service.SCHEMA.getSchemaClassList(year));
        classes.removeIf(clazz -> clazz.getId().equals(OFF_BALANCE_CLASS_ID));
        return classes;
    }
}
