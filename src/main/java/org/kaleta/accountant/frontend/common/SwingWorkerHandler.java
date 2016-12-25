package org.kaleta.accountant.frontend.common;

import org.kaleta.accountant.frontend.Initializer;
import org.kaleta.accountant.service.ServiceFailureException;

import javax.swing.*;

/**
 * Created by Stanislav Kaleta on 16.04.2016.
 *
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
                    new ErrorDialog(e).setVisible(true);
                } catch (Exception e){
                    Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
                    new ErrorDialog(e).setVisible(true);
                }
                return null;
            }
        }.execute();
    }

    protected abstract void runInBackground();
}
