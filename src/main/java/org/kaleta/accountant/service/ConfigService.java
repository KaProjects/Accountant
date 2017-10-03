package org.kaleta.accountant.service;

import org.kaleta.accountant.backend.manager.*;
import org.kaleta.accountant.backend.model.ConfigModel;
import org.kaleta.accountant.frontend.Initializer;
import org.kaleta.accountant.frontend.common.ErrorDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 16.04.2016.
 *
 * Provides access to data source which is related to configuration.
 */
public class ConfigService {

    ConfigService(){
        // package-private
    }

    /**
     * Checks that every resource is alright, throws ServiceFailureException if not.
     */
    public void checkResources() {
        /*checks whether DATA dir is present, creates it if not*/
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

        /*checks whether log file is present, creates it if not*/
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
        File configFile = new File(Initializer.DATA_SOURCE + "config.xml");
        if (!configFile.exists()) {
            try {
                new ConfigManager().create();
            } catch (ManagerException e) {
                Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
                throw new ServiceFailureException(e);
            }
        }

        // TODO: 3.10.2017 check years data -> do something if corrupted
    }

    /**
     * Registers year in configuration and creates year's data directory and files.
     */
    public void initYearData(String newYearName){
        File yearDir = new File(Initializer.DATA_SOURCE + newYearName + File.separator);
        if (yearDir.exists()) {
            String msg = "Directory " + newYearName + " already exists";
            Initializer.LOG.severe(msg);
            throw new ServiceFailureException(msg);
        } else {
            if (yearDir.mkdir()) {
                Initializer.LOG.info("Directory \"" + yearDir.getPath() + "\" created!");
            } else {
                String msg = "Directory for year " + newYearName + " failed to create";
                Initializer.LOG.severe(msg);
                throw new ServiceFailureException(msg);
            }
        }

        try {
            new SchemaManager(newYearName).create();
            new TransactionsManager(newYearName).create();
            new AccountsManager(newYearName).create();
            // TODO: 3.10.2017 add if needed: procedures?,...

            ConfigManager configManager = new ConfigManager();
            ConfigModel configModel = configManager.retrieve();
            ConfigModel.Years.Year configYear = new ConfigModel.Years.Year();
            configYear.setName(newYearName);
            configModel.getYears().getYearList().add(configYear);
            configManager.update(configModel);
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Loads active year from configuration data source.
     */
    public String getActiveYear(){
        try {
            return new ConfigManager().retrieve().getYears().getActive();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Loads all years from configuration data source.
     */
    public List<String> getYears(){
        try {
            List<String> yearList = new ArrayList<>();
            for (ConfigModel.Years.Year year : new ConfigManager ().retrieve().getYears().getYearList()) {
                yearList.add(year.getName());
            }
            return yearList;
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

}
