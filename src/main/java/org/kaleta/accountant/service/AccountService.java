package org.kaleta.accountant.service;

import org.kaleta.accountant.backend.entity.Procedures;
import org.kaleta.accountant.backend.entity.Schema;
import org.kaleta.accountant.backend.entity.Semantic;
import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.manager.jaxb.ProceduresManager;
import org.kaleta.accountant.backend.manager.jaxb.SchemaManager;
import org.kaleta.accountant.backend.manager.jaxb.SemanticManager;
import org.kaleta.accountant.frontend.Initializer;
import org.kaleta.accountant.frontend.common.ErrorDialog;

/**
 * Created by Stanislav Kaleta on 20.04.2016.
 *
 * todo doc
 */
public class AccountService {

    AccountService(){
        // package-private
    }

    /**
     * todo doc
     */
    public Schema getSchema(){
        try {
            return new SchemaManager().retrieve();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo doc
     */
    public void setSchema(Schema schema){
        try {
            new SchemaManager().update(schema);
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo doc
     */
    public Semantic getSemanticAccounts(){
        try {
            return new SemanticManager().retrieve();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo doc
     */
    public void setSemanticAccounts(Semantic semantic){
        try {
            new SemanticManager().update(semantic);
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo doc
     */
    public Procedures getProcedures(){
        try {
            return new ProceduresManager().retrieve();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo doc
     */
    public void setProcedures(Procedures procedures){
        try {
            new ProceduresManager().update(procedures);
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    public void createProcedure(Procedures.Procedure procedure){
        try {
            ProceduresManager manager = new ProceduresManager();
            Procedures procedures = manager.retrieve();
            procedures.getProcedure().add(procedure);
            manager.update(procedures);
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }
}
