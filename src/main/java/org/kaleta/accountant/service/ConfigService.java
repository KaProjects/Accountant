package org.kaleta.accountant.service;

import org.kaleta.accountant.frontend.Initializer;

import java.io.File;
import java.io.IOException;

/**
 * Created by Stanislav Kaleta on 16.04.2016.
 *
 * Provides access to data source which is related to configuration.
 */
public class ConfigService {

    /**
     * Checks that every resource is alright, throws ServiceFailureException if not.
     */
    public void checkResources() {
        File dataSourceFile = new File(Initializer.DATA_SOURCE);
        if (!dataSourceFile.exists()) {
            boolean result = dataSourceFile.mkdir();
            if (result) {
                System.out.println("# Data directory \"" + dataSourceFile.getName() + "\" created!");
            } else {
                System.err.println("ERROR: Data directory creation failed!");
                throw new ServiceFailureException("Data directory creation failed!");
            }
        }

        File logFile = new File(Initializer.DATA_SOURCE + "log.log");
        if (!logFile.exists()) {
            try {
                boolean result = logFile.createNewFile();
                if (result) {
                    System.out.println("# Log file \"%DATA%/" + logFile.getName() + "\" created!");
                } else {
                    System.err.println("ERROR: Log file creation failed!");
                    throw new ServiceFailureException("Log file creation failed!");
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
                throw new ServiceFailureException(e);
            }
        }

        System.out.println("# Resources checked. Everything OK.");
    }

    /**
     * Checks that data are valid, throws ServiceFailureException if not.
     */
    public void checkData() {
        File settingsFile = new File(Initializer.DATA_SOURCE + "schema.xml");
        if (!settingsFile.exists()) {
//            try {
//                SettingsManager manager = new JaxbSettingsManager();
//                manager.createSettings();
//                Initializer.LOG.info("Settings file \"%DATA%/" + settingsFile.getName() + "\" created!");
//            } catch (ManagerException e) {
//                Initializer.LOG.severe(e.getMessage());
//                throw new ServiceFailureException(e);
//            }
        }
    }





}
