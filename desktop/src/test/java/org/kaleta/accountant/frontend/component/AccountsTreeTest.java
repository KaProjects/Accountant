package org.kaleta.accountant.frontend.component;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;

import javax.swing.tree.TreePath;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The plus at the end of a schema account's row opens an account under it. It is a control of its
 * own: using it must not do what clicking the row does, which is to fold the row away.
 */
public class AccountsTreeTest {
    private static final String SCHEMA_ID = "520";

    private AccountsTree tree;
    private final List<String> created = new ArrayList<>();

    @Before
    public void buildTree() {
        tree = new AccountsTree();
        tree.onCreate(created::add);
        tree.show(Collections.singletonList(schemaClass()), accounts(), "", true, List.of());
        tree.setSize(400, 400);
        tree.doLayout();
    }

    @Test
    public void thePlusOpensAnAccountAndLeavesTheRowAsItWas() {
        int row = rowOfSchemaAccount();
        TreePath path = tree.getPathForRow(row);
        Assert.assertTrue("the schema account starts open", tree.isExpanded(path));

        clickAt(xOfThePlus(row), middleOf(row));

        Assert.assertEquals(Collections.singletonList(SCHEMA_ID), created);
        Assert.assertTrue("using the plus folded the row away", tree.isExpanded(path));
    }

    @Test
    public void clickingTheRowItselfIsNotTheAction() {
        int row = rowOfSchemaAccount();

        clickAt(tree.getRowBounds(row).x + 5, middleOf(row));

        Assert.assertTrue("clicking the name should not open an account", created.isEmpty());
    }

    /** A folded row is not being added to, so it carries no plus and the click does nothing. */
    @Test
    public void thePlusIsOnlyThereWhileTheRowIsOpen() {
        int row = rowOfSchemaAccount();
        tree.collapseRow(row);

        clickAt(xOfThePlus(row), middleOf(row));

        Assert.assertTrue("a folded row should offer nothing to click", created.isEmpty());
    }

    /**
     * What was created elsewhere shows up without the tree being built again, or everything the
     * user opened on the way there would fold up as the account appeared.
     */
    @Test
    public void anAccountAddedElsewhereAppearsWithoutFoldingTheTree() {
        int row = rowOfSchemaAccount();
        TreePath path = tree.getPathForRow(row);
        Assert.assertTrue(tree.isExpanded(path));

        Map<String, List<AccountsModel.Account>> accounts = accounts();
        List<AccountsModel.Account> more = new ArrayList<>(accounts.get(SCHEMA_ID));
        more.add(account("1", "telefon"));
        accounts.put(SCHEMA_ID, more);

        tree.update(Collections.singletonList(schemaClass()), accounts);

        Assert.assertTrue("the tree was rebuilt instead of updated", tree.isExpanded(path));
        Assert.assertEquals(2, tree.getModel().getChildCount(path.getLastPathComponent()));
        Assert.assertEquals("telefon", tree.getModel().getChild(path.getLastPathComponent(), 1).toString());
    }

    /** A rename elsewhere reaches the tree the same way, without disturbing it. */
    @Test
    public void aRenamedAccountIsRelabelledInPlace() {
        int row = rowOfSchemaAccount();
        TreePath path = tree.getPathForRow(row);

        Map<String, List<AccountsModel.Account>> accounts = accounts();
        accounts.put(SCHEMA_ID, Collections.singletonList(account("0", "internet na doma")));

        tree.update(Collections.singletonList(schemaClass()), accounts);

        Assert.assertTrue(tree.isExpanded(path));
        Assert.assertEquals("internet na doma", tree.getModel().getChild(path.getLastPathComponent(), 0).toString());
    }

    /**
     * A picker enables its button from what the tree says is selected, so the tree must have
     * recorded the new selection before it tells anyone about it.
     */
    @Test
    public void aSelectionIsRecordedBeforeItIsAnnounced() {
        List<String> announced = new ArrayList<>();
        tree.onSelection(t -> announced.add(t.getSelectedAccountId()));

        tree.setSelectionRow(rowOfAccount("internet"));
        Assert.assertEquals(Collections.singletonList(SCHEMA_ID + ".0"), announced);

        tree.setSelectionRow(rowOfSchemaAccount());
        Assert.assertEquals("a schema account is not an account to pick", "", announced.get(1));
    }

    private int rowOfAccount(String name) {
        for (int row = 0; row < tree.getRowCount(); row++) {
            if (tree.getPathForRow(row).getLastPathComponent().toString().equals(name)) {
                return row;
            }
        }
        throw new AssertionError("the account is not in the tree");
    }

    private int rowOfSchemaAccount() {
        for (int row = 0; row < tree.getRowCount(); row++) {
            if (tree.getPathForRow(row).getLastPathComponent().toString().equals("sluzby")) {
                return row;
            }
        }
        throw new AssertionError("the schema account is not in the tree");
    }

    private int xOfThePlus(int row) {
        Rectangle bounds = tree.getRowBounds(row);
        return bounds.x + bounds.width - 6;
    }

    private int middleOf(int row) {
        Rectangle bounds = tree.getRowBounds(row);
        return bounds.y + bounds.height / 2;
    }

    private void clickAt(int x, int y) {
        long when = System.currentTimeMillis();
        tree.dispatchEvent(new MouseEvent(tree, MouseEvent.MOUSE_PRESSED, when, MouseEvent.BUTTON1_DOWN_MASK, x, y, 1, false, MouseEvent.BUTTON1));
        tree.dispatchEvent(new MouseEvent(tree, MouseEvent.MOUSE_RELEASED, when + 1, 0, x, y, 1, false, MouseEvent.BUTTON1));
        tree.dispatchEvent(new MouseEvent(tree, MouseEvent.MOUSE_CLICKED, when + 2, 0, x, y, 1, false, MouseEvent.BUTTON1));
    }

    private static SchemaModel.Class schemaClass() {
        SchemaModel.Class.Group.Account schemaAccount = new SchemaModel.Class.Group.Account();
        schemaAccount.setId("0");
        schemaAccount.setName("sluzby");
        SchemaModel.Class.Group group = new SchemaModel.Class.Group();
        group.setId("2");
        group.setName("prevadzkove naklady");
        group.getAccount().add(schemaAccount);
        SchemaModel.Class expenses = new SchemaModel.Class();
        expenses.setId("5");
        expenses.setName("Expenses");
        expenses.getGroup().add(group);
        return expenses;
    }

    private static Map<String, List<AccountsModel.Account>> accounts() {
        Map<String, List<AccountsModel.Account>> map = new HashMap<>();
        map.put(SCHEMA_ID, Collections.singletonList(account("0", "internet")));
        return map;
    }

    private static AccountsModel.Account account(String semanticId, String name) {
        AccountsModel.Account account = new AccountsModel.Account();
        account.setSchemaId(SCHEMA_ID);
        account.setSemanticId(semanticId);
        account.setName(name);
        return account;
    }
}
