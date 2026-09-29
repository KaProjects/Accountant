package org.kaleta.accountant.frontend.common;

import org.kaleta.accountant.Initializer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Decides which screen the app opens on, and remembers where it was left.
 * <p>
 * Centring on {@code Toolkit.getScreenSize()} always lands on the default display, because that
 * size belongs to that display while the window position is a point in the whole virtual desktop -
 * on a Mac with external screens placed left of the built-in one those screens have negative
 * coordinates, so a positive position can never reach them.
 * <p>
 * Instead the window reopens exactly where it was last left, which is what matters for the dev loop
 * that restarts the app on every source change. The first time, with nothing remembered, it opens
 * centred on the screen the mouse pointer is on, which is normally the screen holding the terminal
 * the app was started from.
 */
public final class WindowPlacement {
    private static final int SAVE_DELAY_MS = 500;
    /** How much of the window has to be on a screen for a remembered position to still be usable. */
    private static final int MIN_VISIBLE_WIDTH = 160;
    private static final int MIN_VISIBLE_HEIGHT = 60;

    private WindowPlacement() {
        // static members only
    }

    /**
     * Places the frame and keeps its position up to date from then on. The preferred size is only
     * a wish: it is trimmed to whatever the chosen screen can actually show.
     */
    public static void apply(JFrame frame, Dimension preferredSize) {
        Rectangle remembered = readBounds();
        Rectangle bounds = remembered != null && isVisibleOnSomeScreen(remembered)
                ? remembered
                : centredOnThePointersScreen(preferredSize);
        frame.setBounds(bounds);
        Initializer.LOG.info("Window placed at " + bounds.x + "," + bounds.y + " " + bounds.width + "x" + bounds.height
                + (remembered != null ? " (remembered)" : " (centred on the pointer's screen)"));
        if (preferences().getBoolean(key("maximized"), false)) {
            frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
        }
        rememberChanges(frame);
    }

    /**
     * The window the user is actually working in, which is where a dialog belongs. Passing null as
     * a dialog's parent instead centres it on a shared hidden frame, and that frame sits on the
     * default display - which is how warnings ended up on the built-in screen while the app itself
     * was on an external one.
     */
    public static Window activeWindow() {
        KeyboardFocusManager focus = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        Window active = focus.getActiveWindow();
        if (active != null) {
            return active;
        }
        Component focused = focus.getFocusOwner();
        return focused == null ? null : SwingUtilities.getWindowAncestor(focused);
    }

    /**
     * Centres a window on the window the user is working in, falling back to the screen the pointer
     * is on when there is none. Call it after the window has been packed, or it centres a window of
     * size zero.
     */
    public static void centreOnActiveWindow(Window window) {
        Window parent = activeWindow();
        if (parent != null && parent.isShowing()) {
            window.setLocationRelativeTo(parent);
            return;
        }
        Rectangle screen = usableBounds(screenUnderPointer());
        window.setLocation(screen.x + (screen.width - window.getWidth()) / 2,
                screen.y + (screen.height - window.getHeight()) / 2);
    }

    /**
     * Puts a window immediately to the left of another, tops aligned, so that the two are read and
     * dragged between side by side. Where there is no room left of it - the other window is against
     * the edge of its screen - the window goes to its right instead, and if neither side fits, it is
     * pushed onto the screen rather than off it. Call it after the window has been sized.
     */
    public static void placeLeftOf(Window window, Window other) {
        Rectangle screen = usableBounds(other.getGraphicsConfiguration() == null
                ? screenUnderPointer() : other.getGraphicsConfiguration());
        int gap = 8;
        int x = other.getX() - window.getWidth() - gap;
        if (x < screen.x) {
            int toTheRight = other.getX() + other.getWidth() + gap;
            x = toTheRight + window.getWidth() <= screen.x + screen.width ? toTheRight : screen.x;
        }
        int y = Math.min(Math.max(other.getY(), screen.y), screen.y + screen.height - window.getHeight());
        window.setLocation(x, y);
    }

    private static Rectangle centredOnThePointersScreen(Dimension preferredSize) {
        Rectangle screen = usableBounds(screenUnderPointer());
        int width = Math.min(preferredSize.width, screen.width);
        int height = Math.min(preferredSize.height, screen.height);
        return new Rectangle(screen.x + (screen.width - width) / 2, screen.y + (screen.height - height) / 2, width, height);
    }

    private static GraphicsConfiguration screenUnderPointer() {
        try {
            PointerInfo pointer = MouseInfo.getPointerInfo();
            if (pointer != null && pointer.getDevice() != null) {
                return pointer.getDevice().getDefaultConfiguration();
            }
        } catch (RuntimeException e) {
            Initializer.LOG.warning("Cant tell which screen the pointer is on: " + e);
        }
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    }

    /** Screen bounds without the menu bar and the dock. */
    private static Rectangle usableBounds(GraphicsConfiguration screen) {
        Rectangle bounds = new Rectangle(screen.getBounds());
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(screen);
        bounds.x += insets.left;
        bounds.y += insets.top;
        bounds.width -= insets.left + insets.right;
        bounds.height -= insets.top + insets.bottom;
        return bounds;
    }

    /** True while enough of the remembered window would still be reachable; screens do get unplugged. */
    private static boolean isVisibleOnSomeScreen(Rectangle bounds) {
        for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            Rectangle visible = device.getDefaultConfiguration().getBounds().intersection(bounds);
            if (visible.width >= MIN_VISIBLE_WIDTH && visible.height >= MIN_VISIBLE_HEIGHT) {
                return true;
            }
        }
        return false;
    }

    private static void rememberChanges(JFrame frame) {
        // moving a window fires a stream of events, so the write waits until the dragging stops
        Timer saveTimer = new Timer(SAVE_DELAY_MS, e -> save(frame));
        saveTimer.setRepeats(false);

        ComponentAdapter moved = new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                saveTimer.restart();
            }

            @Override
            public void componentResized(ComponentEvent e) {
                saveTimer.restart();
            }
        };
        frame.addComponentListener(moved);
        frame.addWindowStateListener(e -> saveTimer.restart());
    }

    private static void save(JFrame frame) {
        Preferences preferences = preferences();
        boolean maximized = (frame.getExtendedState() & Frame.MAXIMIZED_BOTH) != 0;
        preferences.putBoolean(key("maximized"), maximized);
        if (!maximized) {
            Rectangle bounds = frame.getBounds();
            preferences.put(key("bounds"), bounds.x + "," + bounds.y + "," + bounds.width + "," + bounds.height);
        }
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            Initializer.LOG.warning("Cant remember the window position: " + e);
        }
    }

    private static Rectangle readBounds() {
        String saved = preferences().get(key("bounds"), null);
        if (saved == null) {
            return null;
        }
        try {
            String[] parts = saved.split(",");
            return new Rectangle(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        } catch (RuntimeException e) {
            Initializer.LOG.warning("Cant read the remembered window position '" + saved + "'");
            return null;
        }
    }

    private static Preferences preferences() {
        return Preferences.userNodeForPackage(WindowPlacement.class);
    }

    /** Devel, test and production runs remember their own window, since they are usually different windows. */
    private static String key(String name) {
        return "window." + Initializer.CONTEXT + "." + name;
    }
}
