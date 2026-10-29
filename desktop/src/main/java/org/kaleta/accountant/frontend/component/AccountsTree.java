package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.component.accounts.AccountsEditorRules;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The chart of accounts as a tree: class, group, schema account, and the accounts opened under it.
 * <p>
 * It is a drag source - an account is dragged onto the account field of a transaction - and it
 * carries its own action: an open schema account that may be added to shows a plus at the end of
 * its row, which opens an account under that very schema account. It appears only once the row is
 * expanded, where what is already there can be seen, and never on the schema accounts the app
 * fills itself.
 */
public class AccountsTree extends JTree {
    /** Width of the clickable plus at the end of a row, icon and the gap before it. */
    private static final int ACTION_WIDTH = 28;
    private static final int ICON_SIZE = 16;

    private final DefaultTreeModel treeModel;
    private Consumer<String> createAction = schemaId -> { };
    private final List<Consumer<AccountsTree>> selectionListeners = new ArrayList<>();

    private String selectedAccountId = "";
    private String selectedAccountName = "";
    private String selectedSchemaId = "";

    public AccountsTree() {
        super(new DefaultTreeModel(new DefaultMutableTreeNode("root")));
        treeModel = (DefaultTreeModel) getModel();
        // a schema account with nothing in it yet is still a place to open an account, so it is a
        // node that can be expanded rather than a leaf
        treeModel.setAsksAllowsChildren(true);
        setRootVisible(false);
        setToggleClickCount(1);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setCellRenderer(new LevelColouredRenderer());
        setTransferHandler(new AccountTransferHandler());
        // one listener, so that what is selected is always recorded before anyone is told about it:
        // tree listeners are called in reverse order of registration, and a listener added later
        // would otherwise be handed the selection the tree had a moment ago
        addTreeSelectionListener(e -> {
            rememberSelection();
            selectionListeners.forEach(listener -> listener.accept(this));
        });
    }

    /**
     * The plus at the end of a row is a control of its own, not part of the row: the click is taken
     * before the tree sees it, so that using it neither selects the row nor folds it away - which
     * is what a single click anywhere else on a schema account does.
     */
    @Override
    protected void processMouseEvent(MouseEvent e) {
        boolean press = e.getID() == MouseEvent.MOUSE_PRESSED || e.getID() == MouseEvent.MOUSE_RELEASED
                || e.getID() == MouseEvent.MOUSE_CLICKED;
        if (press && SwingUtilities.isLeftMouseButton(e)) {
            String schemaId = actionAt(e.getX(), e.getY());
            if (schemaId != null) {
                if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                    createAction.accept(schemaId);
                }
                e.consume();
                return;
            }
        }
        super.processMouseEvent(e);
    }

    /** What the plus at the end of a schema account's row does. */
    public void onCreate(Consumer<String> action) {
        this.createAction = action;
    }

    /** The schema account whose plus was clicked, or null when the click was anywhere else. */
    private String actionAt(int x, int y) {
        int row = getRowForLocation(x, y);
        if (row < 0) {
            return null;
        }
        Object node = getPathForRow(row).getLastPathComponent();
        if (!(node instanceof SchemaAccountNode) || !AccountsEditorRules.canCreateAccount(((SchemaAccountNode) node).schemaId)
                || !isExpanded(row)) {
            return null;
        }
        Rectangle bounds = getRowBounds(row);
        return x >= bounds.x + bounds.width - ACTION_WIDTH ? ((SchemaAccountNode) node).schemaId : null;
    }

    /** Rebuilds the tree; the schema account that was just added to is expanded to show its accounts. */
    public void show(List<SchemaModel.Class> classList, Map<String, List<AccountsModel.Account>> accountMap, String expandSchemaId) {
        show(classList, accountMap, expandSchemaId, false, List.of());
    }

    /**
     * @param expandAll  every schema account opened, for a picker that shows one class only
     * @param collapsed  classes shown collapsed: the assets are a long list nothing is dragged from
     */
    public void show(List<SchemaModel.Class> classList, Map<String, List<AccountsModel.Account>> accountMap,
                     String expandSchemaId, boolean expandAll, List<String> collapsed) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode expandNode = null;

        for (SchemaModel.Class clazz : classList) {
            DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(clazz.getName());
            for (SchemaModel.Class.Group group : clazz.getGroup()) {
                DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group.getName());
                for (SchemaModel.Class.Group.Account schemaAccount : group.getAccount()) {
                    String schemaId = clazz.getId() + group.getId() + schemaAccount.getId();
                    SchemaAccountNode schemaAccountNode = new SchemaAccountNode(schemaAccount.getName(), schemaId);

                    for (AccountsModel.Account account : accountMap.getOrDefault(schemaId, List.of())) {
                        schemaAccountNode.add(accountNode(account, schemaAccount.getName()));
                    }
                    groupNode.add(schemaAccountNode);
                    if (schemaId.equals(expandSchemaId)) {
                        expandNode = schemaAccountNode;
                    }
                }
                classNode.add(groupNode);
            }
            root.add(classNode);
        }

        treeModel.setRoot(root);
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode classNode = (DefaultMutableTreeNode) root.getChildAt(i);
            if (collapsed.contains(classNode.toString())) {
                continue;
            }
            expandPath(new TreePath(classNode.getPath()));
            if (expandAll) {
                for (int j = 0; j < classNode.getChildCount(); j++) {
                    DefaultMutableTreeNode groupNode = (DefaultMutableTreeNode) classNode.getChildAt(j);
                    expandPath(new TreePath(groupNode.getPath()));
                    for (int k = 0; k < groupNode.getChildCount(); k++) {
                        expandPath(new TreePath(((DefaultMutableTreeNode) groupNode.getChildAt(k)).getPath()));
                    }
                }
            }
        }
        if (expandNode != null) {
            expandPath(new TreePath(expandNode.getPath()));
            setSelectionPath(new TreePath(expandNode.getPath()));
        }
    }

    /**
     * Brings the tree up to date without building it again: only the accounts under each schema
     * account are added, removed or renamed, so everything the user opened stays open and what was
     * selected stays selected. The tree is rebuilt only when the chart itself changed shape.
     */
    public void update(List<SchemaModel.Class> classList, Map<String, List<AccountsModel.Account>> accountMap) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        if (schemaIdsOf(root).equals(schemaIdsOf(classList))) {
            for (SchemaAccountNode schemaAccountNode : schemaAccountNodes(root)) {
                syncAccounts(schemaAccountNode, accountMap.getOrDefault(schemaAccountNode.schemaId, List.of()));
            }
        } else {
            show(classList, accountMap, "");
        }
    }

    private void syncAccounts(SchemaAccountNode schemaAccountNode, List<AccountsModel.Account> accounts) {
        for (int i = schemaAccountNode.getChildCount() - 1; i >= 0; i--) {
            AccountNode node = (AccountNode) schemaAccountNode.getChildAt(i);
            if (accounts.stream().noneMatch(account -> account.getFullId().equals(node.id))) {
                treeModel.removeNodeFromParent(node);
            }
        }
        for (int i = 0; i < accounts.size(); i++) {
            AccountsModel.Account account = accounts.get(i);
            AccountNode existing = i < schemaAccountNode.getChildCount()
                    ? (AccountNode) schemaAccountNode.getChildAt(i) : null;
            if (existing == null || !existing.id.equals(account.getFullId())) {
                treeModel.insertNodeInto(accountNode(account, schemaAccountNode.schemaAccountName), schemaAccountNode, i);
            } else if (!existing.getUserObject().equals(account.getName())) {
                existing.setUserObject(account.getName());
                existing.fullName = fullNameOf(account, schemaAccountNode.schemaAccountName);
                treeModel.nodeChanged(existing);
            }
        }
    }

    private static List<String> schemaIdsOf(DefaultMutableTreeNode root) {
        List<String> ids = new ArrayList<>();
        for (SchemaAccountNode node : schemaAccountNodes(root)) {
            ids.add(node.schemaId);
        }
        return ids;
    }

    private static List<String> schemaIdsOf(List<SchemaModel.Class> classList) {
        List<String> ids = new ArrayList<>();
        for (SchemaModel.Class clazz : classList) {
            for (SchemaModel.Class.Group group : clazz.getGroup()) {
                for (SchemaModel.Class.Group.Account account : group.getAccount()) {
                    ids.add(clazz.getId() + group.getId() + account.getId());
                }
            }
        }
        return ids;
    }

    private static List<SchemaAccountNode> schemaAccountNodes(DefaultMutableTreeNode root) {
        List<SchemaAccountNode> nodes = new ArrayList<>();
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode classNode = (DefaultMutableTreeNode) root.getChildAt(i);
            for (int j = 0; j < classNode.getChildCount(); j++) {
                DefaultMutableTreeNode groupNode = (DefaultMutableTreeNode) classNode.getChildAt(j);
                for (int k = 0; k < groupNode.getChildCount(); k++) {
                    nodes.add((SchemaAccountNode) groupNode.getChildAt(k));
                }
            }
        }
        return nodes;
    }

    /** Called whenever the selection changes, so a button can follow what is selected. */
    public void onSelection(Consumer<AccountsTree> listener) {
        selectionListeners.add(listener);
    }

    private void rememberSelection() {
        Object node = getLastSelectedPathComponent();
        selectedAccountId = node instanceof AccountNode ? ((AccountNode) node).id : "";
        selectedAccountName = node instanceof AccountNode ? ((AccountNode) node).fullName : "";
        selectedSchemaId = node instanceof SchemaAccountNode ? ((SchemaAccountNode) node).schemaId : "";
        setDragEnabled(!selectedAccountId.isEmpty());
    }

    public String getSelectedAccountId() {
        return selectedAccountId;
    }

    public String getSelectedAccountName() {
        return selectedAccountName;
    }

    /** The schema account under which a new account would be opened, empty when none is selected. */
    public String getSelectedSchemaId() {
        return selectedSchemaId;
    }

    public boolean canCreateUnderSelection() {
        return !selectedSchemaId.isEmpty() && AccountsEditorRules.canCreateAccount(selectedSchemaId);
    }

    /**
     * The row's own action, drawn rather than taken from the app's icons: a plain thin plus, large
     * enough to hit but quiet enough that a long list of schema accounts does not turn into a column
     * of buttons. The green plus of the cards would read as the loudest thing on the row.
     */
    private static class SubtlePlus implements Icon {
        private static final Color COLOUR = new Color(0x8A, 0x94, 0xA6);

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COLOUR);
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int centre = ICON_SIZE / 2;
            int arm = ICON_SIZE / 2 - 2;
            g2.drawLine(x + centre - arm, y + centre, x + centre + arm, y + centre);
            g2.drawLine(x + centre, y + centre - arm, x + centre, y + centre + arm);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return ICON_SIZE;
        }

        @Override
        public int getIconHeight() {
            return ICON_SIZE;
        }
    }

    private static AccountNode accountNode(AccountsModel.Account account, String schemaAccountName) {
        AccountNode node = new AccountNode(account.getName(), account.getFullId(), fullNameOf(account, schemaAccountName));
        node.setAllowsChildren(false);
        return node;
    }

    /** What an account is called once dropped: the general account goes by its schema account's name. */
    private static String fullNameOf(AccountsModel.Account account, String schemaAccountName) {
        return account.getName().equals(Constants.Account.GENERAL_ACCOUNT_NAME)
                ? schemaAccountName : schemaAccountName + " - " + account.getName();
    }

    private static class AccountNode extends DefaultMutableTreeNode {
        private final String id;
        private String fullName;

        AccountNode(String title, String id, String fullName) {
            super(title);
            this.id = id;
            this.fullName = fullName;
        }
    }

    private static class SchemaAccountNode extends DefaultMutableTreeNode {
        private final String schemaId;
        private final String schemaAccountName;

        SchemaAccountNode(String title, String schemaId) {
            super(title);
            this.schemaId = schemaId;
            this.schemaAccountName = title;
        }
    }

    /**
     * The three levels keep the colours the overview uses, so the same thing looks the same; a
     * schema account that may be added to carries the plus that opens an account under it.
     */
    private static class LevelColouredRenderer extends DefaultTreeCellRenderer {
        private final JPanel withAction = new JPanel();
        private final JLabel action = new JLabel(new SubtlePlus());

        LevelColouredRenderer() {
            withAction.setLayout(new BoxLayout(withAction, BoxLayout.X_AXIS));
            withAction.setOpaque(false);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            Component component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            component.setFont(new Font(component.getFont().getName(), Font.BOLD, 15));
            setIcon(null);
            switch (((DefaultMutableTreeNode) value).getLevel()) {
                case 1: component.setForeground(Constants.Color.OVERVIEW_CLASS); break;
                case 2: component.setForeground(Constants.Color.OVERVIEW_GROUP); break;
                case 3: component.setForeground(Constants.Color.OVERVIEW_ACCOUNT); break;
                default: break;
            }
            component.setBackground(Color.LIGHT_GRAY);
            ((JLabel) component).setOpaque(value.toString().equals(Constants.Account.GENERAL_ACCOUNT_NAME) && !hasFocus);

            // only once the schema account is open: a row that is folded up is not being added to
            if (expanded && value instanceof SchemaAccountNode
                    && AccountsEditorRules.canCreateAccount(((SchemaAccountNode) value).schemaId)) {
                withAction.removeAll();
                withAction.add(component);
                withAction.add(Box.createHorizontalStrut(ACTION_WIDTH - ICON_SIZE));
                withAction.add(action);
                return withAction;
            }
            return component;
        }
    }

    /** Drag only: what an account field accepts is the account's id, and that is all this exports. */
    private class AccountTransferHandler extends TransferHandler {
        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }

        @Override
        public Transferable createTransferable(JComponent c) {
            return new StringSelection(selectedAccountId);
        }
    }
}
