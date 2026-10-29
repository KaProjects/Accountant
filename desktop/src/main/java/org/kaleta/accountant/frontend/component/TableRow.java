package org.kaleta.accountant.frontend.component;

import java.awt.event.MouseListener;

/**
 * A row of one of the accounting or analysis tables.
 * <p>
 * The rows of a table are built independently of each other, so something has to line them up
 * afterwards: each one reports the width its own title needs, and is then given the widest of them.
 * That leaves the value cells to share whatever the window has to spare.
 */
public interface TableRow {

    /** Width this row needs for its title, before any alignment. */
    int getTitleWidth();

    /** Pins the name column, so every row of a table lines up and the longest title still fits. */
    void setNameColumnWidth(int width);

    /**
     * Registers a listener that fires wherever the row is clicked, its own buttons aside. Adding it
     * to the row alone is not enough: a click goes to the innermost component that is listening,
     * and a cell with a tooltip is quietly one of those.
     */
    void addRowMouseListener(MouseListener listener);
}
