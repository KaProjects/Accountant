package org.kaleta.accountant.service;

import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.manager.SchemaManager;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.ErrorHandler;
import org.kaleta.accountant.frontend.Initializer;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Provides access to data source which is related to schema.
 */
public class SchemaService {

    SchemaService(){
        // package-private
    }

    /**
     * Returns list of schema classes.
     */
    public List<SchemaModel.Class> getSchemaClassList(String year) {
        try {
            return new SchemaManager(year).retrieve().getClazz();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns map of schema classes, where key is their id.
     */
    public Map<Integer, SchemaModel.Class> getSchemaClassMap(String year) {
        try {
            Map<Integer, SchemaModel.Class> classMap = new TreeMap<>();
            for ( SchemaModel.Class clazz : new SchemaManager(year).retrieve().getClazz()){
                classMap.put(Integer.parseInt(clazz.getId()), clazz);
            }
            return classMap;
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns map of groups for the class, where key is group id.
     */
    public Map<Integer, SchemaModel.Class.Group> getSchemaGroupMap(SchemaModel.Class clazz) {
        Map<Integer, SchemaModel.Class.Group> groupMap = new TreeMap<>();
        for (SchemaModel.Class.Group group : clazz.getGroup()){
            groupMap.put(Integer.parseInt(group.getId()), group);
        }
        return groupMap;
    }

    /**
     * Returns map of accounts for the group, where key is account id.
     */
    public Map<Integer, SchemaModel.Class.Group.Account> getSchemaAccountMap(SchemaModel.Class.Group group) {
        Map<Integer, SchemaModel.Class.Group.Account> accountMap = new TreeMap<>();
        for (SchemaModel.Class.Group.Account account : group.getAccount()){
            accountMap.put(Integer.parseInt(account.getId()), account);
        }
        return accountMap;
    }
}
