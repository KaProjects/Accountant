package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.listener.CreateAnalyticalAccountAction;
import org.kaleta.accountant.frontend.component.AccountsTree;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

/**
 * Picks one account out of the chart, on the same tree the palette shows: the account is chosen by
 * selecting it, and one is opened by the plus on the schema account it belongs under.
 */
public class SelectAccountDialog extends Dialog {
    private final Map<String, List<AccountsModel.Account>> accountMap;
    private final List<SchemaModel.Class> classList;
    private final AccountsTree accountsTree = new AccountsTree();
    private final boolean expanded;

    public SelectAccountDialog(Configuration configuration, Map<String, List<AccountsModel.Account>> accountMap,
                               List<SchemaModel.Class> classList) {
        this(configuration, accountMap, classList, false);
    }

    public SelectAccountDialog(Configuration configuration, Map<String, List<AccountsModel.Account>> accountMap,
                               List<SchemaModel.Class> classList, boolean expanded) {
        super(configuration, "Selecting Account", "Select");
        this.accountMap = accountMap;
        this.classList = classList;
        this.expanded = expanded;
        buildDialogContent();
        setDialogValid("No account selected");
        this.setSize(350, (int) (0.8f * Toolkit.getDefaultToolkit().getScreenSize().height));
    }

    private void buildDialogContent() {
        accountsTree.onSelection(tree -> setDialogValid(tree.getSelectedAccountId().isEmpty() ? "No account selected" : null));
        accountsTree.onCreate(schemaId -> new CreateAnalyticalAccountAction(this, schemaId, false)
                .onCreated(() -> SwingUtilities.invokeLater(() -> reload(schemaId)))
                .actionPerformed(new ActionEvent(accountsTree, ActionEvent.ACTION_PERFORMED, null)));
        show("");

        JScrollPane pane = new JScrollPane(accountsTree);
        setContent(layout -> {
            layout.setHorizontalGroup(layout.createParallelGroup().addComponent(pane));
            layout.setVerticalGroup(layout.createSequentialGroup().addComponent(pane));
        });
    }

    private void show(String expandSchemaId) {
        accountsTree.show(classList, accountMap, expandSchemaId, expanded, List.of());
    }

    /** Shows what was just opened under that schema account, leaving the rest of the tree as it is. */
    private void reload(String schemaId) {
        accountMap.put(schemaId, Service.ACCOUNT.getAccountsBySchemaId(getConfiguration().getSelectedYear(), schemaId));
        accountsTree.update(classList, accountMap);
    }

    public String getSelectedAccountId() {
        return accountsTree.getSelectedAccountId();
    }

    public String getSelectedAccountName() {
        return accountsTree.getSelectedAccountName();
    }
}
