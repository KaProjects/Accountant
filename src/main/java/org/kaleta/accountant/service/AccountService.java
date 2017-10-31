package org.kaleta.accountant.service;

import org.kaleta.accountant.backend.entity.Procedures;
import org.kaleta.accountant.backend.entity.Schema;
import org.kaleta.accountant.backend.entity.Semantic;
import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.manager.jaxb.ProceduresManager;
import org.kaleta.accountant.backend.manager.jaxb.SchemaManager;
import org.kaleta.accountant.backend.manager.jaxb.SemanticManager;
import org.kaleta.accountant.common.ErrorHandler;
import org.kaleta.accountant.frontend.Initializer;

@Deprecated
public class AccountService {

    AccountService(){
        // package-private
    }

    public Schema getSchema(){
        try {
            return new SchemaManager().retrieve();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    public void setSchema(Schema schema){
        try {
            new SchemaManager().update(schema);
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    public Semantic getSemanticAccounts(){
        try {
            return new SemanticManager().retrieve();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    public Procedures getProcedures(){
        try {
            return new ProceduresManager().retrieve();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    public String getAccountName(String schemaId){
        try {
            final String[] name = new String[]{""};
            Schema schema = new SchemaManager().retrieve();
            schema.getClazz().stream().filter(clazz -> clazz.getId().equals(schemaId.substring(0, 1))).forEach(clazz -> {
                clazz.getGroup().stream().filter(group -> group.getId().equals(schemaId.substring(1, 2))).forEach(group -> {
                    group.getAccount().stream().filter(acc -> acc.getId().equals(schemaId.substring(2, 3))).forEach(acc -> {
                        name[0] = acc.getName();
                    });
                });
            });
            if (name[0].equals("")) {
                throw new IllegalArgumentException("Account with schema id '" + schemaId + "' not found;");
            } else {
                return name[0];
            }
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    public String getAccountType(String schemaId){
        try {
            final String[] name = new String[]{""};
            Schema schema = new SchemaManager().retrieve();
            schema.getClazz().stream().filter(clazz -> clazz.getId().equals(schemaId.substring(0, 1))).forEach(clazz -> {
                clazz.getGroup().stream().filter(group -> group.getId().equals(schemaId.substring(1, 2))).forEach(group -> {
                    group.getAccount().stream().filter(acc -> acc.getId().equals(schemaId.substring(2, 3))).forEach(acc -> {
                        name[0] = acc.getType();
                    });
                });
            });
            if (name[0].equals("")) {
                throw new IllegalArgumentException("Account with schema id '" + schemaId + "' not found!");
            } else {
                return name[0];
            }
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }
}
