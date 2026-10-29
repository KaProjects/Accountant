package org.kaleta.accountant.frontend.component.card;

import org.junit.Assert;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The card editors have no buttons on their rows: a whole row, or a whole card header, is the click
 * target. That was first built on mouseClicked, which AWT refuses to fire as soon as the pointer
 * moves a single pixel between press and release, so rows opened their dialog only some of the time.
 */
public class ClickableAreaTest {

    private final AtomicInteger clicks = new AtomicInteger();
    private final JPanel area = new JPanel(null);
    private final JLabel child = new JLabel("name");

    public ClickableAreaTest() {
        area.add(child);
        area.setSize(200, 24);
        child.setBounds(40, 0, 100, 24);
        CardStyle.makeClickable(area, e -> clicks.incrementAndGet(), () -> { }, () -> { });
    }

    @Test
    public void opensOnAPlainClick() {
        press(area, 10, 12);
        release(area, 10, 12, 1);
        Assert.assertEquals(1, clicks.get());
    }

    @Test
    public void opensEvenWhenThePointerDriftsWhilePressed() {
        press(child, 20, 12);
        release(child, 23, 13, 1); // three pixels of drift: no mouseClicked would ever arrive
        Assert.assertEquals(1, clicks.get());
    }

    @Test
    public void opensWhenPressedOnAChildAndReleasedOnTheRow() {
        press(child, 20, 12);
        release(area, 150, 12, 1);
        Assert.assertEquals(1, clicks.get());
    }

    @Test
    public void staysClosedWhenReleasedOutside() {
        press(area, 10, 12);
        release(area, 10, 400, 1); // dragged off the row and let go
        Assert.assertEquals(0, clicks.get());
    }

    @Test
    public void opensOnlyOnceForADoubleClick() {
        press(area, 10, 12);
        release(area, 10, 12, 1);
        press(area, 10, 12);
        release(area, 10, 12, 2);
        Assert.assertEquals(1, clicks.get());
    }

    @Test
    public void ignoresTheRightButton() {
        dispatch(area, MouseEvent.MOUSE_PRESSED, 10, 12, 1, MouseEvent.BUTTON3);
        dispatch(area, MouseEvent.MOUSE_RELEASED, 10, 12, 1, MouseEvent.BUTTON3);
        Assert.assertEquals(0, clicks.get());
    }

    private void press(Component source, int x, int y) {
        dispatch(source, MouseEvent.MOUSE_PRESSED, x, y, 1, MouseEvent.BUTTON1);
    }

    private void release(Component source, int x, int y, int clickCount) {
        dispatch(source, MouseEvent.MOUSE_RELEASED, x, y, clickCount, MouseEvent.BUTTON1);
    }

    private void dispatch(Component source, int id, int x, int y, int clickCount, int button) {
        source.dispatchEvent(new MouseEvent(source, id, System.currentTimeMillis(), 0, x, y, clickCount, false, button));
    }
}
