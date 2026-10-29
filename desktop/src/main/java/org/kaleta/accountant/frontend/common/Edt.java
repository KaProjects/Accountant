package org.kaleta.accountant.frontend.common;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

/**
 * Runs a piece of user interface work on the event dispatch thread and waits for it.
 * <p>
 * Every action in this app runs inside a {@link SwingWorkerHandler}, on a worker thread, which is
 * right for reading and writing the data but wrong for anything that builds or shows a component.
 * Swing may only be touched from the event thread, and doing it from a worker deadlocks: a file
 * chooser laid out on a worker holds the AWT tree lock while waiting for its file model, whose lock
 * the event thread holds while waiting for the tree lock. Neither can continue and the whole
 * application stops.
 * <p>
 * So a worker does its own work, then hands the dialog over here.
 */
public final class Edt {

    private Edt() {
        // static members only
    }

    /** Runs the task on the event thread and waits for it to finish. */
    public static void run(Runnable task) {
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while waiting for the event thread", e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new IllegalStateException(cause);
        }
    }

    /** The same, for a dialog that answers something: the user's choice comes back to the worker. */
    public static <T> T get(Supplier<T> task) {
        Object[] result = new Object[1];
        run(() -> result[0] = task.get());
        @SuppressWarnings("unchecked")
        T value = (T) result[0];
        return value;
    }
}
