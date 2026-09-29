package org.kaleta.accountant.frontend.component;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaleta.accountant.core.TestParent;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.service.Service;

import javax.swing.JButton;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A row booked from a procedure offers to send a correction back to it - but only once it says
 * something the procedure does not. The date is not such a thing: a procedure says what is booked,
 * never when.
 */
public class BookedFromProcedureTest extends TestParent {
    private TransactionPanel panel;

    @Before
    public void bookARowFromAProcedure() {
        Service.ACCOUNT.createAccount(YEAR, "cash", "300", "0", "");
        Service.ACCOUNT.createAccount(YEAR, "bank", "300", "1", "");

        panel = new TransactionPanel(configuration(), new HashMap<>(), new HashMap<>(),
                Service.SCHEMA.getSchemaClassList(YEAR), null, true);
        panel.setAmount("100");
        panel.setDebitCreditDescription("300.0", "300.1", "rent");
        panel.bookedFromProcedure("7", 0, e -> { });
    }

    @Test
    public void itIsOfferedButNotYetWorthPressing() {
        Assert.assertTrue("a row from a procedure shows the button", button().isVisible());
        Assert.assertFalse("nothing has changed yet", button().isEnabled());
    }

    @Test
    public void aChangedAmountIsWorthKeeping() {
        panel.setAmount("120");

        Assert.assertTrue(button().isEnabled());
    }

    @Test
    public void soIsAChangedAccountOrDescription() {
        panel.setCredit("300.0");
        Assert.assertTrue(button().isEnabled());

        panel.setCredit("300.1");
        Assert.assertFalse("put back as it was, there is nothing to send", button().isEnabled());

        panel.setDescription("rent - december");
        Assert.assertTrue(button().isEnabled());
    }

    @Test
    public void theDateIsNotSomethingAProcedureSays() {
        panel.setDate("1512");

        Assert.assertFalse(button().isEnabled());
    }

    @Test
    public void onceSentTheRowIsWhatTheProcedureSays() {
        panel.setAmount("120");
        Assert.assertTrue(button().isEnabled());

        panel.procedureUpdated();

        Assert.assertFalse(button().isEnabled());
    }

    /** A row that was not booked from a procedure has nothing to send back. */
    @Test
    public void anOrdinaryRowShowsNothing() {
        TransactionPanel ordinary = new TransactionPanel(configuration(), new HashMap<>(), new HashMap<>(),
                Service.SCHEMA.getSchemaClassList(YEAR), null, true);

        Assert.assertFalse(buttonOf(ordinary).isVisible());
    }

    private JButton button() {
        return buttonOf(panel);
    }

    private static JButton buttonOf(TransactionPanel panel) {
        for (JButton button : buttons(panel, new ArrayList<>())) {
            if ("Update the procedure with these values".equals(button.getToolTipText())) {
                return button;
            }
        }
        throw new AssertionError("the row has no update-procedure button");
    }

    private static List<JButton> buttons(Component component, List<JButton> found) {
        if (component instanceof JButton) {
            found.add((JButton) component);
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                buttons(child, found);
            }
        }
        return found;
    }

    private Configuration configuration() {
        return new Configuration() {
            @Override
            public void update(int command) {
                // nothing to update in a test
            }

            @Override
            public void selectYear(String yearId) {
                // the year never changes here
            }

            @Override
            public String getSelectedYear() {
                return YEAR;
            }
        };
    }
}
