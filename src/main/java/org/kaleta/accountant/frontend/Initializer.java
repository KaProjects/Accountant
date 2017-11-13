package org.kaleta.accountant.frontend;

import org.kaleta.accountant.common.ErrorHandler;
import org.kaleta.accountant.common.LogFormatter;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Performs initialization of this app. Includes data and resources checks, app. wide constants and default logger.
 */
public class Initializer {
    public static final String NAME = "Accountant";
    public static final String VERSION = "1.0";
    public static final String DATA_SOURCE = new File(Initializer.class.getProtectionDomain().getCodeSource().getLocation()
            .getPath()).getParentFile().getPath() + File.separator + NAME + "-" + VERSION + "-DATA" + File.separator;
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
                Service.CONFIG.checkData();
                initLogger();
                if (Service.CONFIG.getActiveYear().equals("-1")){
                    String name = JOptionPane.showInputDialog(null, "Set First Year Name");
                    if (name != null && !name.trim().isEmpty()) {
                        Service.CONFIG.initYearData(name);
                        Service.CONFIG.setActiveYear(name);
                    }
                }

                new AppFrame().setVisible(true);

            } catch (Throwable e) {
                ErrorHandler.getThrowableDialog(e).setVisible(true);
                System.exit(1);
            }
        });
    }
}
