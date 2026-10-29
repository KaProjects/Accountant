package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.common.Constants;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.List;

/**
 * The procedures as a tree: group, then the procedures in it.
 * <p>
 * Built like the account tree beside it and dragged the same way, except that what it exports is a
 * whole procedure rather than one account - dropping it onto a transaction dialog books every
 * transaction the procedure holds.
 */
public class ProceduresTree extends JTree {
    /** Marks the payload as a procedure, which an account field will not accept by mistake. */
    public static final String PROCEDURE_PREFIX = "procedure:";

    private final DefaultTreeModel treeModel;
    private String selectedProcedureId = "";
    private String shownSignature = "";

    public ProceduresTree() {
        super(new DefaultTreeModel(new DefaultMutableTreeNode("root")));
        treeModel = (DefaultTreeModel) getModel();
        setRootVisible(false);
        setToggleClickCount(1);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setCellRenderer(new LevelColouredRenderer());
        setTransferHandler(new ProcedureTransferHandler());
        addTreeSelectionListener(e -> {
            Object node = getLastSelectedPathComponent();
            selectedProcedureId = node instanceof ProcedureNode ? ((ProcedureNode) node).id : "";
            setDragEnabled(!selectedProcedureId.isEmpty());
        });
    }

    public void show(List<ProceduresModel.Group> groups) {
        shownSignature = signature(groups);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        for (ProceduresModel.Group group : groups) {
            DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group.getName());
            for (ProceduresModel.Group.Procedure procedure : group.getProcedure()) {
                groupNode.add(new ProcedureNode(procedure.getName(), procedure.getId()));
            }
            root.add(groupNode);
        }
        treeModel.setRoot(root);
        for (int i = 0; i < root.getChildCount(); i++) {
            expandPath(new TreePath(((DefaultMutableTreeNode) root.getChildAt(i)).getPath()));
        }
    }

    /**
     * Rebuilds only when something actually changed, so coming back from a dialog that was cancelled
     * leaves the tree, and what is open in it, alone.
     */
    public void update(List<ProceduresModel.Group> groups) {
        if (!signature(groups).equals(shownSignature)) {
            show(groups);
        }
    }

    private static String signature(List<ProceduresModel.Group> groups) {
        StringBuilder signature = new StringBuilder();
        for (ProceduresModel.Group group : groups) {
            signature.append(group.getName()).append('|');
            for (ProceduresModel.Group.Procedure procedure : group.getProcedure()) {
                signature.append(procedure.getId()).append(':').append(procedure.getName()).append(';');
            }
        }
        return signature.toString();
    }

    public String getSelectedProcedureId() {
        return selectedProcedureId;
    }

    private static class ProcedureNode extends DefaultMutableTreeNode {
        private final String id;

        ProcedureNode(String title, String id) {
            super(title);
            this.id = id;
        }
    }

    private static class LevelColouredRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            Component component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            component.setFont(new Font(component.getFont().getName(), Font.BOLD, 15));
            setIcon(null);
            component.setForeground(((DefaultMutableTreeNode) value).getLevel() == 1
                    ? Constants.Color.OVERVIEW_GROUP : Constants.Color.OVERVIEW_ACCOUNT);
            return component;
        }
    }

    private class ProcedureTransferHandler extends TransferHandler {
        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }

        @Override
        public Transferable createTransferable(JComponent c) {
            return new StringSelection(PROCEDURE_PREFIX + selectedProcedureId);
        }
    }
}
