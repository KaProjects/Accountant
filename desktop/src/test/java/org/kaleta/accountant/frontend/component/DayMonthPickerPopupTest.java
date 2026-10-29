package org.kaleta.accountant.frontend.component;

import org.junit.Assert;
import org.junit.Test;

import javax.swing.JLabel;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The calendar behind a date field: the months first, then the days of the one chosen. A date here
 * is a day and a month, so that is all it hands back - and all it ever shows.
 */
public class DayMonthPickerPopupTest {

    @Test
    public void aMonthIsChosenFirstAndThenADayOfIt() {
        AtomicReference<String> picked = new AtomicReference<>();
        DayMonthPickerPopup popup = new DayMonthPickerPopup(2026, (day, month) -> picked.set(day + "." + month));

        Assert.assertNull("the days come only after a month is chosen", cell(popup, "17"));

        click(cell(popup, "Sep"));
        click(cell(popup, "17"));

        Assert.assertEquals("17.9", picked.get());
    }

    /** Going back to the months is what the header is for, and picking again works from there. */
    @Test
    public void theMonthCanBeChosenAgain() {
        AtomicReference<String> picked = new AtomicReference<>();
        DayMonthPickerPopup popup = new DayMonthPickerPopup(2026, (day, month) -> picked.set(day + "." + month));

        click(cell(popup, "Sep"));
        click(header(popup));
        click(cell(popup, "Mar"));
        click(cell(popup, "3"));

        Assert.assertEquals("3.3", picked.get());
    }

    @Test
    public void aMonthIsAsLongAsItReallyIs() {
        DayMonthPickerPopup february = new DayMonthPickerPopup(2026, (day, month) -> { });
        click(cell(february, "Feb"));

        Assert.assertNotNull(cell(february, "28"));
        Assert.assertNull(cell(february, "29"));
    }

    @Test
    public void aLeapFebruaryHasItsTwentyNinth() {
        DayMonthPickerPopup february = new DayMonthPickerPopup(2024, (day, month) -> { });
        click(cell(february, "Feb"));

        Assert.assertNotNull(cell(february, "29"));
    }

    @Test
    public void noYearIsEverShown() {
        DayMonthPickerPopup popup = new DayMonthPickerPopup(2026, (day, month) -> { });
        assertSaysNothingAboutTheYear(popup);

        click(cell(popup, "Sep"));
        assertSaysNothingAboutTheYear(popup);
    }

    private static void assertSaysNothingAboutTheYear(DayMonthPickerPopup popup) {
        for (JLabel label : labels(popup, new ArrayList<>())) {
            Assert.assertFalse("the calendar shows a year: '" + label.getText() + "'",
                    label.getText().contains("2026") || label.getText().contains("2024"));
        }
    }

    private static void click(JLabel cell) {
        cell.dispatchEvent(new MouseEvent(cell, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0,
                1, 1, 1, false, MouseEvent.BUTTON1));
    }

    /** A month or a day is a label one can click; the weekday headings above them are not. */
    private static JLabel cell(DayMonthPickerPopup popup, String text) {
        for (JLabel label : labels(popup, new ArrayList<>())) {
            if (label.getText().equals(text) && label.getMouseListeners().length > 0) {
                return label;
            }
        }
        return null;
    }

    private static JLabel header(DayMonthPickerPopup popup) {
        for (JLabel label : labels(popup, new ArrayList<>())) {
            if (label.getText().startsWith("‹")) {
                return label;
            }
        }
        return null;
    }

    private static List<JLabel> labels(Component component, List<JLabel> found) {
        if (component instanceof JLabel) {
            found.add((JLabel) component);
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                labels(child, found);
            }
        }
        return found;
    }
}
