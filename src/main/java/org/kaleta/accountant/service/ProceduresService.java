package org.kaleta.accountant.service;

import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.manager.ProceduresManager;
import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.common.ErrorHandler;
import org.kaleta.accountant.frontend.Initializer;

import java.util.List;

/**
 * Provides access to data source which is related to procedures.
 */
public class ProceduresService {

    ProceduresService(){
        // package-private
    }

    /**
     * Returns list of schema classes.
     */
    public List<ProceduresModel.Procedure> getProcedureList(String year) {
        try {
            return new ProceduresManager(year).retrieve().getProcedure();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Creates procedure according to specified values
     */
    public void createProcedure(String year, String name, List<ProceduresModel.Procedure.Transaction> transactions){
        try {
            ProceduresManager manager = new ProceduresManager(year);
            ProceduresModel model = manager.retrieve();

            ProceduresModel.Procedure procedure = new ProceduresModel.Procedure();
            procedure.setName(name);
            procedure.setId(String.valueOf(model.getProcedure().size()));
            procedure.getTransaction().addAll(transactions);
            model.getProcedure().add(procedure);

            manager.update(model);
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }
}
