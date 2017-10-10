package org.kaleta.accountant.service;

import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.manager.SchemaManager;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
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

    /**
     * Returns name of schema group specified by class and group id.
     */
    public String getSchemaName(String year, Integer classId, Integer groupId) {
        SchemaModel.Class clazz = getSchemaClassMap(year).get(classId);
        return getSchemaGroupMap(clazz).get(groupId).getName();
    }

    /**
     * Returns true if group can be deleted, false otherwise.
     */
    public boolean isGroupDeletable(String year, Integer classId, Integer groupId) {
        // TODO: 10.10.2017 TODO impl. this when conditions decided
        // TODO: 10.10.2017 e.g. associated opened accounts
        return true;
    }

    /**
     * Creates group for specified arguments. Also creates associated accounts where needed.
     */
    public void createGroup(String year, String classId, String groupId, String name) {
        try {
            SchemaManager schemaManager = new SchemaManager(year);
            SchemaModel schemaModel = schemaManager.retrieve();

            SchemaModel.Class.Group newGroup = new SchemaModel.Class.Group();
            newGroup.setName(name);
            newGroup.setId(String.valueOf(groupId));
            for (SchemaModel.Class clazz : schemaModel.getClazz()){
                if (clazz.getId().equals(classId)){
                    clazz.getGroup().add(newGroup);
                }
            }

            switch (classId) {
                case "0": {
                    SchemaModel.Class.Group.Account accDepAccount = new SchemaModel.Class.Group.Account();
                    accDepAccount.setId(groupId);
                    accDepAccount.setName(Constants.Schema.ACCUMULATED_DEP_ACCOUNT_PREFIX + name);
                    accDepAccount.setType(Constants.AccountType.LIABILITY);
                    for (SchemaModel.Class clazz : schemaModel.getClazz()){
                        if (clazz.getId().equals("0")){
                            for (SchemaModel.Class.Group group : clazz.getGroup()) {
                                if (group.getId().equals("9")) {
                                    group.getAccount().add(accDepAccount);
                                }
                            }
                        }
                    }
                    SchemaModel.Class.Group.Account depAccount = new SchemaModel.Class.Group.Account();
                    depAccount.setId(groupId);
                    depAccount.setName(Constants.Schema.DEPRECIATION_ACCOUNT_PREFIX + name);
                    depAccount.setType(Constants.AccountType.EXPENSE);
                    for (SchemaModel.Class clazz : schemaModel.getClazz()){
                        if (clazz.getId().equals("5")){
                            for (SchemaModel.Class.Group group : clazz.getGroup()) {
                                if (group.getId().equals("9")) {
                                    group.getAccount().add(depAccount);
                                }
                            }
                        }
                    }
                    break;
                }
                case "1": {
                    SchemaModel.Class.Group.Account consumptionAccount = new SchemaModel.Class.Group.Account();
                    consumptionAccount.setId(groupId);
                    consumptionAccount.setName(Constants.Schema.CONSUMPTION_ACCOUNT_PREFIX + name);
                    consumptionAccount.setType(Constants.AccountType.EXPENSE);
                    for (SchemaModel.Class clazz : schemaModel.getClazz()){
                        if (clazz.getId().equals("5")){
                            for (SchemaModel.Class.Group group : clazz.getGroup()) {
                                if (group.getId().equals("8")) {
                                    group.getAccount().add(consumptionAccount);
                                }
                            }
                        }
                    }
                    break;
                }
                case "2":
                case "3":
                case "4":
                case "5":
                case "6": break;
                default: throw new IllegalArgumentException("Illegal class id!");
            }

            schemaManager.update(schemaModel);
        } catch (ManagerException e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    
















}
