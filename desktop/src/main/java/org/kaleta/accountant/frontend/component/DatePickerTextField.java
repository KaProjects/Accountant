package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.frontend.common.Validable;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Year;

/**
 * A date as this app keeps them: four digits, day and month, no year - every transaction belongs to
 * the year its books are open in.
 * <p>
 * The digits can be typed or dragged in, and a right click opens a calendar: the months first, then
 * the days of the one chosen. No year appears in it - the year being booked only lays a month out,
 * so that its days fall on the weekdays they really did.
 */
public class DatePickerTextField extends HintValidatedTextField implements Validable {
    private static final int DATE_LENGTH = 4;

    private final int year;

    public DatePickerTextField(String date, DocumentListener documentListener) {
        this(date, documentListener, null);
    }

    /** @param year the year being booked, which only lays the calendar out; null means this one */
    public DatePickerTextField(String date, DocumentListener documentListener, String year) {
        super(date, "Date Picker", "set date", true, documentListener);
        this.year = yearOf(year);
        this.setHorizontalAlignment(SwingConstants.RIGHT);
        this.setToolTipText("Date as DDMM - right click for a calendar");
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                openCalendar(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                openCalendar(e);
            }
        });
    }

    private static int yearOf(String year) {
        try {
            return year == null ? Year.now().getValue() : Integer.parseInt(year);
        } catch (NumberFormatException e) {
            return Year.now().getValue();
        }
    }

    /** Right click, whichever way the platform reports it. */
    private void openCalendar(MouseEvent e) {
        if (!e.isPopupTrigger() && e.getButton() != MouseEvent.BUTTON3) {
            return;
        }
        new DayMonthPickerPopup(year, this::setDate).show(this, e.getX(), e.getY());
    }

    private void setDate(int day, int month) {
        focusGained(null); // the hint is showing while the field is empty, and would be typed over
        setText(String.format("%02d%02d", day, month));
    }

    @Override
    boolean doValidate() {
        return this.getText() != null && !this.getText().trim().isEmpty() && this.getText().length() == DATE_LENGTH;
    }

    @Override
    public String validator() {
        if (!validatorEnabled) return null;
        return doValidate() ? null : "Date NOT set";
    }
}
