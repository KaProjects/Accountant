package org.kaleta.accountant.service;

import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.backend.manager.*;
import org.kaleta.accountant.backend.model.ConfigModel;
import org.kaleta.accountant.common.ErrorHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides access to data source which is related to configuration.
 */
public class ConfigService {

    private ConfigModel configModel;

    ConfigService(){
        // package-private
    }

    private ConfigModel getModel() throws ManagerException {
        if (configModel == null) {
            configModel = new ConfigManager().retrieve();
        }
        return new ConfigModel(configModel);
    }

    public void invalidateModel(){
        configModel = null;
    }

    /**
     * Checks that every resource is alright, throws ServiceFailureException if not.
     */
    public void checkResources() {
        /*checks whether DATA dir is present, creates it if not*/
        File dataSourceFile = new File(Initializer.getDataSource());
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
        File logFile = new File(Initializer.getDataSource() + "log.log");
        if (!logFile.exists()) {
            try {
                boolean result = logFile.createNewFile();
                if (result) {
                    System.out.println("# Log file '%DATA_DIR%/" + logFile.getName() + "' created!");
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
    public void checkData() throws ManagerException {
        File configFile = new File(Initializer.getDataSource() + "config.xml");
        if (!configFile.exists()) {
            new ConfigManager().create();
            System.out.println("# File '%DATA_DIR%/config.xml' created!");
        }
        for (ConfigModel.Years.Year yearModel : getModel().getYears().getYearList()){
            String year = yearModel.getName();
            File yearDir = new File(Initializer.getDataSource() + year);
            if (!yearDir.exists()) {
                throw new ServiceFailureException("Directory '%DATA_DIR%/" + year +"' is missing!");
            }
            File schemaFile = new File(Initializer.getDataSource() + year + File.separator + "schema.xml");
            if (!schemaFile.exists()) {
                throw new ServiceFailureException("File '%DATA_DIR%/" + year + File.separator + "schema.xml' is missing!");
            }
            File trFile = new File(Initializer.getDataSource() + year + File.separator + "transactions.xml");
            if (!trFile.exists()) {
                throw new ServiceFailureException("File '%DATA_DIR%/" + year + File.separator + "transactions.xml' is missing!");
            }
            File accFile = new File(Initializer.getDataSource() + year + File.separator + "accounts.xml");
            if (!accFile.exists()) {
                throw new ServiceFailureException("File '%DATA_DIR%/" + year + File.separator + "accounts.xml' is missing!");
            }
            File prFile = new File(Initializer.getDataSource() + year + File.separator + "procedures.xml");
            if (!prFile.exists()) {
                throw new ServiceFailureException("File '%DATA_DIR%/" + year + File.separator + "procedures.xml' is missing!");
            }
        }
        System.out.println("# Data checked. Everything OK.");
    }

    /**
     * Registers year in configuration and creates year's data directory and files.
     */
    public void initYearData(String newYearName){
        File yearDir = new File(Initializer.getDataSource() + newYearName + File.separator);
        if (yearDir.exists()) {
            String msg = "Directory '" + newYearName + "' already exists";
            Initializer.LOG.severe(msg);
            throw new ServiceFailureException(msg);
        } else {
            if (yearDir.mkdir()) {
                Initializer.LOG.info("Directory created: '" + yearDir.getPath() + "'");
            } else {
                String msg = "Directory for year '" + newYearName + "' failed to create";
                Initializer.LOG.severe(msg);
                throw new ServiceFailureException(msg);
            }
        }

        try {
            new SchemaManager(newYearName).create();
            new TransactionsManager(newYearName).create();
            new AccountsManager(newYearName).create();
            new ProceduresManager(newYearName).create();

            Manager<ConfigModel> manager = new ConfigManager();
            ConfigModel model = manager.retrieve();

            ConfigModel.Years.Year configYear = new ConfigModel.Years.Year();
            configYear.setName(newYearName);
            model.getYears().getYearList().add(configYear);

            manager.update(model);
            Initializer.LOG.info("Year '" + newYearName + "' added to config");
            invalidateModel();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     *
     */
    public void setActiveYear(String year){
        try {
            Manager<ConfigModel> manager = new ConfigManager();
            ConfigModel model = manager.retrieve();

            model.getYears().setActive(year);

            manager.update(model);
            Initializer.LOG.info("Year '" + year + "' set to active");
            invalidateModel();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Loads active year from configuration data source.
     */
    public String getActiveYear(){
        try {
            return getModel().getYears().getActive();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Loads all years from configuration data source.
     */
    public List<String> getYears(){
        try {
            List<String> yearList = new ArrayList<>();
            for (ConfigModel.Years.Year year : getModel().getYears().getYearList()) {
                yearList.add(year.getName());
            }
            return yearList;
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }
}
