package org.kaleta.accountant.frontend.component;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaleta.accountant.core.TestParent;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.service.Service;

import javax.swing.JComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Where each arrow key leads from a field of a transaction row: along the row, and between the rows
 * of the same dialog.
 */
public class ArrowKeyNavigationTest extends TestParent {
    private static final int DATE = 0;
    private static final int AMOUNT = 1;
    private static final int DEBIT = 2;
    private static final int DESCRIPTION = 4;

    private final List<TransactionPanel> rows = new ArrayList<>();

    @Before
    public void buildRows() {
        rows.clear();
        rows.add(row(true));
        rows.add(row(true));
    }

    private TransactionPanel row(boolean withDate) {
        return new TransactionPanel(configuration(), new HashMap<>(), new HashMap<>(),
                Service.SCHEMA.getSchemaClassList(YEAR), null, withDate);
    }

    @Test
    public void rightAndLeftWalkAlongTheRow() {
        TransactionPanel row = rows.get(0);

        Assert.assertSame(row.navigableFields().get(AMOUNT), ArrowKeyNavigation.beside(() -> rows, row, DATE, 1));
        Assert.assertSame(row.navigableFields().get(DATE), ArrowKeyNavigation.beside(() -> rows, row, AMOUNT, -1));
    }

    @Test
    public void thereIsNothingBeyondTheEndsOfARow() {
        TransactionPanel row = rows.get(0);

        Assert.assertNull(ArrowKeyNavigation.beside(() -> rows, row, DATE, -1));
        Assert.assertNull(ArrowKeyNavigation.beside(() -> rows, row, DESCRIPTION, 1));
    }

    /** A procedure's rows carry no date, and the hidden field is stepped over rather than focused. */
    @Test
    public void aHiddenFieldIsSteppedOver() {
        rows.clear();
        rows.add(row(false));
        TransactionPanel row = rows.get(0);

        Assert.assertNull("nothing to the left of the amount once the date is hidden",
                ArrowKeyNavigation.beside(() -> rows, row, AMOUNT, -1));
    }

    @Test
    public void downAndUpKeepTheColumn() {
        TransactionPanel first = rows.get(0);
        TransactionPanel second = rows.get(1);

        Assert.assertSame(second.navigableFields().get(DEBIT), ArrowKeyNavigation.above(() -> rows, first, DEBIT, 1));
        Assert.assertSame(first.navigableFields().get(DEBIT), ArrowKeyNavigation.above(() -> rows, second, DEBIT, -1));
    }

    @Test
    public void thereIsNothingAboveTheFirstRowOrBelowTheLast() {
        Assert.assertNull(ArrowKeyNavigation.above(() -> rows, rows.get(0), DEBIT, -1));
        Assert.assertNull(ArrowKeyNavigation.above(() -> rows, rows.get(1), DEBIT, 1));
    }

    /** The column may not exist in the row below - its date is hidden - so the nearest one is taken. */
    @Test
    public void aColumnThatIsHiddenBelowFallsBackToTheNearestField() {
        rows.clear();
        rows.add(row(true));
        rows.add(row(false));

        JComponent target = ArrowKeyNavigation.above(() -> rows, rows.get(0), DATE, 1);

        Assert.assertSame(rows.get(1).navigableFields().get(AMOUNT), target);
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
