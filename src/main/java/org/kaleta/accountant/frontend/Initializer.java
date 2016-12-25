package org.kaleta.accountant.frontend;

import org.kaleta.accountant.frontend.common.ErrorDialog;
import org.kaleta.accountant.frontend.common.LogFormatter;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Stanislav Kaleta on 16.04.2016.
 *
 * Performs initialization of this app. Includes data and resources checks, app. wide constants and default logger.
 */
public class Initializer {
    public static final String NAME = "Accountant";
    public static final String VERSION = "0.1-snapshot";
    public static final String DATA_SOURCE = new File(Initializer.class.getProtectionDomain().getCodeSource().getLocation()
            .getPath()).getParentFile().getPath() + "/" + NAME + "-" + VERSION + "-DATA/";
    public static final Logger LOG = Logger.getLogger("Logger");

    private static void initLogger(){
        try {
            File logFile = new File(DATA_SOURCE + "log.log");
            FileHandler fileHandler = new FileHandler(logFile.getCanonicalPath(), true);
            fileHandler.setFormatter(new LogFormatter());
            LOG.addHandler(fileHandler);
            LOG.addHandler(new ConsoleHandler());
            LOG.setLevel(Level.INFO);
            LOG.setUseParentHandlers(false);
        } catch (IOException e){
            System.err.println("ERROR: Setting the logger failed!");
            throw new ExceptionInInitializerError("Setting the logger failed!");
        }
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            try {
                Service.CONFIG.checkResources();
                initLogger();
                Service.CONFIG.checkData();
                //Service.configService().checkFirstUse();


                new AppFrame().setVisible(true);

            } catch (Throwable e) {
                JDialog errorDialog = new ErrorDialog(e);
                errorDialog.setVisible(true);
                System.exit(1);
            }
        });
    }
}
