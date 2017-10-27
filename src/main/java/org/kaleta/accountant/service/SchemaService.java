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

    private SchemaModel.Class getClassById(SchemaModel  schemaModel, String classId){
        for (SchemaModel.Class clazz : schemaModel.getClazz()){
            if (clazz.getId().equals(classId)) return clazz;
        }
        throw new IllegalArgumentException("Illegal class id!");
    }

    private SchemaModel.Class.Group getGroupById(SchemaModel.Class clazz, String groupId){
        for (SchemaModel.Class.Group group : clazz.getGroup()){
            if (group.getId().equals(groupId)) return group;
        }
        throw new IllegalArgumentException("Illegal group id!");
    }

    private SchemaModel.Class.Group.Account getAccountById(SchemaModel.Class.Group group, String accId){
        for (SchemaModel.Class.Group.Account acc : group.getAccount()){
            if (acc.getId().equals(accId)) return acc;
        }
        throw new IllegalArgumentException("Illegal account id!");
    }

    /**
     * Returns name of specified schema group.
     */
    public String getGroupName(String year, String classId, String groupId) {
        try {
            SchemaModel schemaModel = new SchemaManager(year).retrieve();
            return getGroupById(getClassById(schemaModel, classId), groupId).getName();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns name of specified schema account.
     */
    public String getAccountName(String year, String classId, String groupId, String accountId) {
        try {
            SchemaModel schemaModel = new SchemaManager(year).retrieve();
            return getAccountById(getGroupById(getClassById(schemaModel, classId), groupId), accountId).getName();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns true if schema group can be deleted, false otherwise.
     */
    public boolean isGroupDeletable(String year, String classId, String groupId) {
        // TODO: 10.10.2017 TODO impl. this when conditions decided
        // TODO: 10.10.2017 e.g. associated opened accounts
        // TODO: 15.10.2017 dont forget to check associated groups/accounts
        return true;
    }

    /**
     * Returns true if schema account can be deleted, false otherwise.
     */
    public boolean isAccountDeletable(String year, String classId, String groupId, String accountId) {
        // TODO: 10.10.2017 TODO impl. this when conditions decided
        // TODO: 10.10.2017 e.g. associated opened accounts
        // TODO: 15.10.2017 dont forget to check associated groups/accounts
        return true;
    }

    /**
     * Creates specified group. Also creates associated accounts where needed.
     */
    public void createGroup(String year, String classId, String groupId, String name) {
        try {
            SchemaManager schemaManager = new SchemaManager(year);
            SchemaModel schemaModel = schemaManager.retrieve();

            SchemaModel.Class.Group newGroup = new SchemaModel.Class.Group();
            newGroup.setName(name);
            newGroup.setId(groupId);
            getClassById(schemaModel, classId).getGroup().add(newGroup);

            switch (classId) {
                case "0": {
                    SchemaModel.Class.Group.Account accDepAccount = new SchemaModel.Class.Group.Account();
                    accDepAccount.setId(groupId);
                    accDepAccount.setName(Constants.Schema.ACCUMULATED_DEP_ACCOUNT_PREFIX + name);
                    accDepAccount.setType(Constants.AccountType.LIABILITY);
                    getGroupById(getClassById(schemaModel, "0"), Constants.Schema.ACCUMULATED_DEP_GROUP_ID).getAccount().add(accDepAccount);

                    SchemaModel.Class.Group.Account depAccount = new SchemaModel.Class.Group.Account();
                    depAccount.setId(groupId);
                    depAccount.setName(Constants.Schema.DEPRECIATION_ACCOUNT_PREFIX + name);
                    depAccount.setType(Constants.AccountType.EXPENSE);
                    getGroupById(getClassById(schemaModel, "5"), Constants.Schema.DEPRECIATION_GROUP_ID).getAccount().add(depAccount);
                    break;
                }
                case "1": {
                    SchemaModel.Class.Group.Account consumptionAccount = new SchemaModel.Class.Group.Account();
                    consumptionAccount.setId(groupId);
                    consumptionAccount.setName(Constants.Schema.CONSUMPTION_ACCOUNT_PREFIX + name);
                    consumptionAccount.setType(Constants.AccountType.EXPENSE);
                    getGroupById(getClassById(schemaModel, "5"), Constants.Schema.CONSUMPTION_GROUP_ID).getAccount().add(consumptionAccount);
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

    /**
     * Renames specified group. Also renames associated accounts where needed.
     */
    public void renameGroup(String year, String classId, String groupId, String newName){
        try {
            SchemaManager schemaManager = new SchemaManager(year);
            SchemaModel schemaModel = schemaManager.retrieve();

            getGroupById(getClassById(schemaModel, classId), groupId).setName(newName);

            switch (classId) {
                case "0": {
                    getAccountById(getGroupById(getClassById(schemaModel, "0"), Constants.Schema.ACCUMULATED_DEP_GROUP_ID), groupId)
                            .setName(Constants.Schema.ACCUMULATED_DEP_ACCOUNT_PREFIX + newName);

                    getAccountById(getGroupById(getClassById(schemaModel, "5"), Constants.Schema.DEPRECIATION_GROUP_ID), groupId)
                            .setName(Constants.Schema.DEPRECIATION_ACCOUNT_PREFIX + newName);
                    break;
                }
                case "1": {
                    getAccountById(getGroupById(getClassById(schemaModel, "5"), Constants.Schema.CONSUMPTION_GROUP_ID), groupId)
                            .setName(Constants.Schema.CONSUMPTION_ACCOUNT_PREFIX + newName);
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

    /**
     * Deletes specified group. Also renames associated accounts where needed.
     */
    public void deleteGroup(String year, String classId, String groupId){
        try {
            SchemaManager schemaManager = new SchemaManager(year);
            SchemaModel schemaModel = schemaManager.retrieve();

            SchemaModel.Class clazz = getClassById(schemaModel, classId);
            clazz.getGroup().remove(getGroupById(clazz, groupId));

            switch (classId) {
                case "0": {
                    SchemaModel.Class.Group groupAccDep = getGroupById(getClassById(schemaModel, "0"), Constants.Schema.ACCUMULATED_DEP_GROUP_ID);
                    groupAccDep.getAccount().remove(getAccountById(groupAccDep, groupId));

                    SchemaModel.Class.Group groupDep = getGroupById(getClassById(schemaModel, "5"), Constants.Schema.DEPRECIATION_GROUP_ID);
                    groupDep.getAccount().remove(getAccountById(groupDep, groupId));
                    break;
                }
                case "1": {
                    SchemaModel.Class.Group groupCons = getGroupById(getClassById(schemaModel, "5"), Constants.Schema.CONSUMPTION_GROUP_ID);
                    groupCons.getAccount().remove(getAccountById(groupCons, groupId));
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

    /**
     * Creates specified account.
     */
    public void createAccount(String year, String classId, String groupId, String accountId, String name, String type) {
        try {
            SchemaManager schemaManager = new SchemaManager(year);
            SchemaModel schemaModel = schemaManager.retrieve();

            SchemaModel.Class.Group.Account newAcc = new SchemaModel.Class.Group.Account();
            newAcc.setName(name);
            newAcc.setId(accountId);
            newAcc.setType(type);
            getGroupById(getClassById(schemaModel, classId), groupId).getAccount().add(newAcc);

            schemaManager.update(schemaModel);
        } catch (ManagerException e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Renames specified account.
     */
    public void renameAccount(String year, String classId, String groupId, String accountId, String newName){
        try {
            SchemaManager schemaManager = new SchemaManager(year);
            SchemaModel schemaModel = schemaManager.retrieve();

            getAccountById(getGroupById(getClassById(schemaModel, classId), groupId), accountId).setName(newName);

            schemaManager.update(schemaModel);
        } catch (ManagerException e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Deletes specified account.
     */
    public void deleteAccount(String year, String classId, String groupId, String accountId){
        try {
            SchemaManager schemaManager = new SchemaManager(year);
            SchemaModel schemaModel = schemaManager.retrieve();

            SchemaModel.Class.Group group = getGroupById(getClassById(schemaModel, classId), groupId);
            group.getAccount().remove(getAccountById(group, accountId));

            schemaManager.update(schemaModel);
        } catch (ManagerException e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns account type for schema id.
     */
    public String getSchemaAccountType(String year, String schemaId) {
        try {
            SchemaModel schemaModel = new SchemaManager(year).retrieve();
            return getAccountById(getGroupById(getClassById(schemaModel,
                    schemaId.substring(0, 1)),
                    schemaId.substring(1, 2)),
                    schemaId.substring(2, 3)).getType();
        } catch (ManagerException e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }






}
