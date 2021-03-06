package org.kaleta.accountant.frontend.common;

import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.common.ErrorHandler;
import org.kaleta.accountant.service.ServiceFailureException;

import javax.swing.*;

/**
 * Handles executing in Swing Worker thread. Every GUI action should extend this class.
 */
public abstract class SwingWorkerHandler {

    public void execute(){
        new SwingWorker<Void,Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    runInBackground();
                } catch (ServiceFailureException e){
                    // No need to log here. Cause exc. is (should be) always logged before SFEx is thrown.
                    ErrorHandler.getThrowableDialog(e).setVisible(true);
                } catch (Exception e){
                    Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
                    ErrorHandler.getThrowableDialog(e).setVisible(true);
                }
                return null;
            }
        }.execute();
    }

    protected abstract void runInBackground();
}
