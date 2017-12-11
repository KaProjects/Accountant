package org.kaleta.accountant.service;

import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.backend.manager.Manager;
import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.manager.ProceduresManager;
import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.common.ErrorHandler;

import java.util.List;

/**
 * Provides access to data source which is related to procedures.
 */
public class ProceduresService {

    private ProceduresModel proceduresModel;

    ProceduresService(){
        // package-private
    }

    private ProceduresModel getModel(String year) throws ManagerException {
        if (proceduresModel == null) {
            proceduresModel = new ProceduresManager(year).retrieve();
        }
        return new ProceduresModel(proceduresModel);
    }

    public void invalidateModel(){
        proceduresModel = null;
    }

    /**
     * Returns list of schema classes.
     */
    public List<ProceduresModel.Procedure> getProcedureList(String year) {
        try {
            return getModel(year).getProcedure();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Creates procedure according to specified values.
     */
    public void createProcedure(String year, String name, List<ProceduresModel.Procedure.Transaction> transactions){
        try {
            Manager<ProceduresModel> manager = new ProceduresManager(year);
            ProceduresModel model = manager.retrieve();

            ProceduresModel.Procedure procedure = new ProceduresModel.Procedure();
            procedure.setName(name);
            procedure.setId(String.valueOf(model.getProcedure().size()));
            procedure.getTransaction().addAll(transactions);
            model.getProcedure().add(procedure);

            manager.update(model);
            Initializer.LOG.info("Procedure created: id=" + procedure.getId() + " name='" + procedure.getName() + "'");
            invalidateModel();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Updates procedure specified by 'id' according to new values.
     */
    public void updateProcedure(String year, String id, String newName, List<ProceduresModel.Procedure.Transaction> newTransactions){
        try {
            Manager<ProceduresModel> manager = new ProceduresManager(year);
            ProceduresModel model = manager.retrieve();

            for (ProceduresModel.Procedure procedure : model.getProcedure()){
                if (procedure.getId().equals(id)){
                    procedure.setName(newName);
                    procedure.getTransaction().clear();
                    procedure.getTransaction().addAll(newTransactions);
                }
            }

            manager.update(model);
            Initializer.LOG.info("Procedure updated: id=" + id + " name='" + newName + "'");
            invalidateModel();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

}
