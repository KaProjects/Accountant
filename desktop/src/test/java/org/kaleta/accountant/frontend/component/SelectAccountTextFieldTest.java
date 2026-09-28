package org.kaleta.accountant.frontend.component;

import org.junit.Assert;
import org.junit.Test;
import org.kaleta.accountant.core.TestParent;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A debit or credit field must only ever hold an account that exists.
 * <p>
 * Its drop handler used to ask whether the component being dropped *on* was one of these fields,
 * which is always true, and never what was being dropped. Any text was then stored as the selected
 * account - and since the amount field beside it is draggable, dragging an amount across set the
 * amount as the debit. Nothing complained: the field went on displaying the account it had, and the
 * validator only checked that something was selected.
 */
public class SelectAccountTextFieldTest extends TestParent {

    private static final String SCHEMA_ID = "300"; // receivables to persons, present in the default schema

    private SelectAccountTextField field() {
        Configuration configuration = new Configuration() {
            public void update(int command) { }
            public void selectYear(String yearId) { }
            public String getSelectedYear() { return YEAR; }
        };
        return new SelectAccountTextField(configuration, new HashMap<>(), new ArrayList<>(), "Debit", null);
    }

    private boolean drop(SelectAccountTextField field, String text) {
        TransferHandler handler = field.getTransferHandler();
        TransferHandler.TransferSupport support =
                new TransferHandler.TransferSupport(field, new StringSelection(text));
        return handler.canImport(support) && handler.importData(support);
    }

    @Test
    public void acceptsAnAccountThatExists() {
        Service.ACCOUNT.createAccount(YEAR, "cash", SCHEMA_ID, "0", "");
        SelectAccountTextField field = field();

        Assert.assertTrue("an existing account should be droppable", drop(field, SCHEMA_ID + ".0"));
        Assert.assertEquals(SCHEMA_ID + ".0", field.getSelectedAccount());
    }

    @Test
    public void refusesAnAmountDraggedFromTheAmountField() {
        Service.ACCOUNT.createAccount(YEAR, "cash", SCHEMA_ID, "0", "");
        SelectAccountTextField field = field();
        field.setSelectedAccount(SCHEMA_ID + ".0");

        Assert.assertFalse("an amount is not an account", drop(field, "1234"));
        Assert.assertEquals("the field must keep the account it had", SCHEMA_ID + ".0", field.getSelectedAccount());
    }

    @Test
    public void refusesAnAccountThatDoesNotExist() {
        SelectAccountTextField field = field();

        Assert.assertFalse("a well formed id that is not in this year is still not droppable",
                drop(field, "999.7"));
        Assert.assertEquals("", field.getSelectedAccount());
    }

    @Test
    public void refusesArbitraryText() {
        SelectAccountTextField field = field();

        Assert.assertFalse(drop(field, "some dragged sentence"));
        Assert.assertFalse(drop(field, ""));
        Assert.assertEquals("", field.getSelectedAccount());
    }

    @Test
    public void refusesWhatIsNotText() {
        SelectAccountTextField field = field();
        TransferHandler.TransferSupport support = new TransferHandler.TransferSupport(field, new StringSelection("x") {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{DataFlavor.imageFlavor};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return false;
            }
        });
        Assert.assertFalse(field.getTransferHandler().canImport(support));
    }
}
