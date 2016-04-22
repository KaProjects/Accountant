package org.kaleta.accountant.service;

import org.kaleta.accountant.backend.entity.Schema;
import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.manager.jaxb.SchemaManager;
import org.kaleta.accountant.frontend.Initializer;
import org.kaleta.accountant.frontend.common.ErrorDialog;

/**
 * Created by Stanislav Kaleta on 20.04.2016.
 *
 * todo doc
 */
public class SchemaService {

    SchemaService(){
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
}
