package org.kaleta.accountant.frontend.component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.BiConsumer;

/**
 * The calendar behind a date field, in two steps: the months, then the days of the one picked.
 * <p>
 * A date in this app is a day and a month, and that is all this asks for - no year is shown or
 * chosen. The year the books are open in is used only to lay a month out, so that its days fall on
 * the weekdays they really did.
 */
class DayMonthPickerPopup extends JPopupMenu {
    private static final Color TODAY = new Color(0x1F, 0x5C, 0xB8);
    private static final Color HOVER = new Color(0xE7, 0xEF, 0xFD);
    private static final Color MUTED = new Color(0x8A, 0x94, 0xA6);

    private final int year;
    private final BiConsumer<Integer, Integer> onPick;
    private final JPanel body = new JPanel(new BorderLayout());

    DayMonthPickerPopup(int year, BiConsumer<Integer, Integer> onPick) {
        this.year = year;
        this.onPick = onPick;

        setBorder(BorderFactory.createLineBorder(MUTED));
        body.setBorder(new EmptyBorder(6, 8, 8, 8));
        body.setOpaque(false);
        add(body);

        showMonths();
    }

    private void showMonths() {
        JPanel months = new JPanel(new GridLayout(4, 3, 2, 2));
        months.setOpaque(false);
        for (Month month : Month.values()) {
            months.add(cell(month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH), false,
                    () -> showDays(month.getValue())));
        }
        replaceBody(months);
    }

    private void showDays(int month) {
        YearMonth shown = YearMonth.of(year, month);

        JLabel back = new JLabel("‹  " + shown.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        back.setFont(back.getFont().deriveFont(Font.BOLD));
        back.setForeground(MUTED);
        back.setBorder(new EmptyBorder(0, 2, 5, 2));
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                showMonths();
            }
        });

        JPanel days = new JPanel(new GridLayout(0, 7, 1, 1));
        days.setOpaque(false);
        for (DayOfWeek day : DayOfWeek.values()) {
            JLabel heading = new JLabel(day.getDisplayName(TextStyle.NARROW, Locale.ENGLISH), SwingConstants.CENTER);
            heading.setFont(heading.getFont().deriveFont(Font.PLAIN, heading.getFont().getSize2D() - 2f));
            heading.setForeground(MUTED);
            days.add(heading);
        }
        // the month starts on its own weekday, so the first row is padded up to it
        for (int i = 1; i < shown.atDay(1).getDayOfWeek().getValue(); i++) {
            days.add(new JLabel(""));
        }
        for (int day = 1; day <= shown.lengthOfMonth(); day++) {
            int picked = day;
            boolean today = shown.atDay(day).equals(java.time.LocalDate.now());
            days.add(cell(String.valueOf(day), today, () -> {
                onPick.accept(picked, month);
                setVisible(false);
            }));
        }

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(back, BorderLayout.NORTH);
        panel.add(days, BorderLayout.CENTER);
        replaceBody(panel);
    }

    private JLabel cell(String text, boolean highlighted, Runnable action) {
        JLabel cell = new JLabel(text, SwingConstants.CENTER);
        cell.setBorder(new EmptyBorder(3, 7, 3, 7));
        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (highlighted) {
            cell.setFont(cell.getFont().deriveFont(Font.BOLD));
            cell.setForeground(TODAY);
        }
        cell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                cell.setOpaque(true);
                cell.setBackground(HOVER);
                cell.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cell.setOpaque(false);
                cell.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                action.run();
            }
        });
        return cell;
    }

    private void replaceBody(JComponent content) {
        body.removeAll();
        body.add(content, BorderLayout.CENTER);
        body.revalidate();
        body.repaint();
        pack();
    }
}
