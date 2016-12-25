package org.kaleta.accountant.service;

import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.manager.jaxb.ConfigManager;
import org.kaleta.accountant.backend.manager.jaxb.ProceduresManager;
import org.kaleta.accountant.backend.manager.jaxb.SchemaManager;
import org.kaleta.accountant.backend.manager.jaxb.SemanticManager;
import org.kaleta.accountant.frontend.Initializer;
import org.kaleta.accountant.frontend.common.ErrorDialog;

import java.io.File;
import java.io.IOException;

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

        File journalDir = new File(Initializer.DATA_SOURCE + "journal/");
        if (!journalDir.exists()){
            boolean result = journalDir.mkdir();
            if (result) {
                System.out.println("# Journal directory \"" + dataSourceFile.getName() + "\" created!");
            } else {
                System.err.println("ERROR: Journal directory creation failed!");
                throw new ServiceFailureException("Journal directory creation failed!");
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
        File configFile = new File(Initializer.DATA_SOURCE + "config.xml");
        if (!configFile.exists()) {
            try {
                new ConfigManager().create();
                Initializer.LOG.info("File \"%DATA%/" + configFile.getName() + "\" created!");
            } catch (ManagerException e) {
                Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
                throw new ServiceFailureException(e);
            }
        }

        File schemaFile = new File(Initializer.DATA_SOURCE + "schema.xml");
        if (!schemaFile.exists()) {
            try {
                new SchemaManager().create();
                Initializer.LOG.info("File \"%DATA%/" + schemaFile.getName() + "\" created!");
            } catch (ManagerException e) {
                Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
                throw new ServiceFailureException(e);
            }
        }

        File semanticFile = new File(Initializer.DATA_SOURCE + "semantic.xml");
        if (!semanticFile.exists()) {
            try {
                new SemanticManager().create();
                Initializer.LOG.info("File \"%DATA%/" + semanticFile.getName() + "\" created!");
            } catch (ManagerException e) {
                Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
                throw new ServiceFailureException(e);
            }
        }

        File proceduresFile = new File(Initializer.DATA_SOURCE + "procedures.xml");
        if (!proceduresFile.exists()) {
            try {
                new ProceduresManager().create();
                Initializer.LOG.info("File \"%DATA%/" + proceduresFile.getName() + "\" created!");
            } catch (ManagerException e) {
                Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
                throw new ServiceFailureException(e);
            }
        }
    }

    /**
     * Loads active year from configuration data source.
     */
    public int getActiveYear(){
        try {
            return Integer.parseInt(new ConfigManager().retrieve().getYears().getActive());
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }



}
