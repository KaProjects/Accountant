package org.kaleta.accountant;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.common.ErrorHandler;
import org.kaleta.accountant.common.LogFormatter;
import org.kaleta.accountant.frontend.AppFrame;
import org.kaleta.accountant.service.Service;

/**
 * Performs initialization of this app. Includes data and resources checks, app. wide constants and default logger.
 */
public class Initializer {
    public static final String NAME = "Accountant";
    public static final String VERSION = "2.1";
    public static final Logger LOG = Logger.getLogger("Logger");

    /**
     * The property that says which books the app opens: "production", "devel" or "test". Left
     * unsaid it is the production ones, so a released jar is started with no argument at all,
     * while dev.sh asks for the devel ones. The tests set {@link #CONTEXT} themselves.
     */
    public static final String CONTEXT_PROPERTY = "accountant.context";

    public static int CONTEXT;

    /** Where the file chooser starts when a statement is imported: this machine's own downloads. */
    public static String DEFAULT_FILES_DIR = System.getProperty("user.home") + File.separator + "Downloads";

    /**
     * Reads the context out of the system property.
     * <p>
     * A value that is not one of the three is an error rather than something to fall back from:
     * the fallback would be production, and a mistyped flag would quietly have the app open the
     * real books and write into them.
     */
    static int contextFromProperty(){
        String value = System.getProperty(CONTEXT_PROPERTY, "production").trim().toLowerCase();
        switch (value) {
            case "production": return Constants.Context.PRODUCTION;
            case "devel": return Constants.Context.DEVEL;
            case "test": return Constants.Context.TEST;
            default: throw new IllegalArgumentException("Unknown -D" + CONTEXT_PROPERTY + "='" + value
                    + "'; it is one of 'production', 'devel' or 'test'.");
        }
    }

    public static String getDataSource(){
        String appParentPath = new File(Initializer.class.getProtectionDomain().getCodeSource().getLocation().getPath())
                .getParentFile().getPath() + File.separator;
        switch (CONTEXT){
            case Constants.Context.PRODUCTION: return appParentPath + NAME + "-" + VERSION + "-DATA" + File.separator;
            case Constants.Context.DEVEL: return appParentPath + "DEVEL-DATA" + File.separator;
            case Constants.Context.TEST: return appParentPath + "TEST-DATA" + File.separator;
            default: throw new IllegalArgumentException("illegal context");
        }
    }

    private static void initLogger(){
        try {
            File logFile = new File(getDataSource() + "log.log");
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
                CONTEXT = contextFromProperty();
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
                e.printStackTrace();
                ErrorHandler.getThrowableDialog(e).setVisible(true);
                System.exit(1);
            }
        });
    }
}
