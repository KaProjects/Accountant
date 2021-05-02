package org.kaleta.accountant.service;

import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.backend.manager.Manager;
import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.manager.SchemaManager;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.common.ErrorHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Provides access to data source which is related to schema.
 */
public class SchemaService {
    private static final Object lock = new Object();
    private SchemaModel schemaModel;

    SchemaService(){
        // package-private
    }

    private SchemaModel getModel(String year) throws ManagerException {
        synchronized (lock) {
            if (schemaModel == null) {
                schemaModel = new SchemaManager(year).retrieve();
            }
            if (!schemaModel.getYear().equals(year)) {
                schemaModel = new SchemaManager(year).retrieve();
            }
            return new SchemaModel(schemaModel);
        }
    }

    public void invalidateModel(){
        schemaModel = null;
    }

    /**
     * Returns list of schema classes.
     */
    public List<SchemaModel.Class> getSchemaClassList(String year) {
        try {
            return getModel(year).getClazz();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns list of schema classes for specified account type.
     */
    public List<SchemaModel.Class> getSchemaClassListByAccountType(String year, String accountType) {
        try {
            List<SchemaModel.Class> allClasses = getModel(year).getClazz();
            List<SchemaModel.Class> filteredClasses = new ArrayList<>();
            for (SchemaModel.Class clazz : allClasses) {
                List<SchemaModel.Class.Group> groupList = new ArrayList<>();
                for (SchemaModel.Class.Group group : clazz.getGroup()) {
                    List<SchemaModel.Class.Group.Account> accountList = new ArrayList<>();
                    for (SchemaModel.Class.Group.Account account : group.getAccount()) {
                        if (account.getType().equals(accountType)){
                            accountList.add(account);
                        }
                    }
                    if (!accountList.isEmpty()){
                        group.getAccount().clear();
                        group.getAccount().addAll(accountList);
                        groupList.add(group);
                    }
                }
                if (!groupList.isEmpty()){
                    clazz.getGroup().clear();
                    clazz.getGroup().addAll(groupList);
                    filteredClasses.add(clazz);
                }
            }
            return filteredClasses;
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
            for ( SchemaModel.Class clazz : getModel(year).getClazz()){
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

    private SchemaModel.Class getClassById(SchemaModel model, String classId) {
        for (SchemaModel.Class clazz : model.getClazz()){
            if (clazz.getId().equals(classId)) return clazz;
        }
        throw new IllegalArgumentException("Not found: class id=" + classId + " in year=" + model.getYear());
    }

    private SchemaModel.Class.Group getGroupById(SchemaModel.Class clazz, String groupId){
        for (SchemaModel.Class.Group group : clazz.getGroup()){
            if (group.getId().equals(groupId)) return group;
        }
        throw new IllegalArgumentException("Not found: group id=" + groupId + " in class id=" + clazz.getId());
    }

    private SchemaModel.Class.Group.Account getAccountById(SchemaModel.Class.Group group, String accId){
        for (SchemaModel.Class.Group.Account acc : group.getAccount()){
            if (acc.getId().equals(accId)) return acc;
        }
        throw new IllegalArgumentException("Not found: account id=" + accId + " in group id=" + group.getId());
    }

    /**
     * Returns group according to class ID and group ID
     */
    public SchemaModel.Class.Group getGroup(String year, String classId, String groupId) {
        try {
            return getGroupById(getClassById(getModel(year),classId), groupId);
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns true if specified group exists, false otherwise.
     */
    public boolean checkGroupExists(String year, String classId, String groupId){
        try {
            SchemaModel.Class.Group group = this.getGroup(year, classId, groupId);
            return group != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns name of specified schema class.
     */
    public String getClassName(String year, String schemaId) {
        try {
            return getClassById(getModel(year), schemaId.substring(0,1)).getName();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns name of specified schema group.
     */
    public String getGroupName(String year, String classId, String groupId) {
        try {
            return getGroupById(getClassById(getModel(year), classId), groupId).getName();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns name of specified schema group.
     */
    public String getGroupName(String year, String schemaId) {
        return getGroupName(year, schemaId.substring(0,1), schemaId.substring(1,2));
    }

    /**
     * Returns name of specified schema account.
     */
    public String getAccountName(String year, String classId, String groupId, String accountId) {
        try {
            return getAccountById(getGroupById(getClassById(getModel(year), classId), groupId), accountId).getName();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns name of specified schema account.
     */
    public String getAccountName(String year, String schemaId) {
        return getAccountName(year, schemaId.substring(0,1), schemaId.substring(1,2), schemaId.substring(2,3));
    }

    /**
     * Returns true if schema group can be deleted, false otherwise.
     */
    public boolean isGroupDeletable(String year, String classId, String groupId) {
        return Service.ACCOUNT.getAccountsBySchemaId(year, classId + groupId).isEmpty();
    }

    /**
     * Returns true if schema account can be deleted, false otherwise.
     */
    public boolean isAccountDeletable(String year, String classId, String groupId, String accountId) {
        return Service.ACCOUNT.getAccountsBySchemaId(year, classId + groupId + accountId).isEmpty();
    }

    /**
     * Creates specified group. Also creates associated accounts where needed.
     */
    public void createGroup(String year, String classId, String groupId, String name) {
        try {
            Manager<SchemaModel> manager = new SchemaManager(year);
            SchemaModel model = manager.retrieve();

            SchemaModel.Class.Group newGroup = new SchemaModel.Class.Group();
            newGroup.setName(name);
            newGroup.setId(groupId);
            getClassById(model, classId).getGroup().add(newGroup);

            List<String> logMsgList = new ArrayList<>();
            logMsgList.add("Schema Group created: id=" + classId + groupId + " name='" + name + "'");
            switch (classId) {
                case "0": {
                    SchemaModel.Class.Group.Account accDepAccount = new SchemaModel.Class.Group.Account();
                    accDepAccount.setId(groupId);
                    accDepAccount.setName(Constants.Schema.ACCUMULATED_DEP_ACCOUNT_PREFIX + name);
                    accDepAccount.setType(Constants.AccountType.LIABILITY);
                    getGroupById(getClassById(model, "0"), Constants.Schema.ACCUMULATED_DEP_GROUP_ID).getAccount().add(accDepAccount);
                    logMsgList.add("Schema Account created: id=" + "0" + Constants.Schema.ACCUMULATED_DEP_GROUP_ID + groupId + " name='" + Constants.Schema.ACCUMULATED_DEP_ACCOUNT_PREFIX + name + "'");

                    SchemaModel.Class.Group.Account depAccount = new SchemaModel.Class.Group.Account();
                    depAccount.setId(groupId);
                    depAccount.setName(Constants.Schema.DEPRECIATION_ACCOUNT_PREFIX + name);
                    depAccount.setType(Constants.AccountType.EXPENSE);
                    getGroupById(getClassById(model, "5"), Constants.Schema.DEPRECIATION_GROUP_ID).getAccount().add(depAccount);
                    logMsgList.add("Schema Account created: id=" + "5" + Constants.Schema.DEPRECIATION_GROUP_ID + groupId + " name='" + Constants.Schema.DEPRECIATION_ACCOUNT_PREFIX + name + "'");
                    break;
                }
                case "1": {
                    SchemaModel.Class.Group.Account consumptionAccount = new SchemaModel.Class.Group.Account();
                    consumptionAccount.setId(groupId);
                    consumptionAccount.setName(Constants.Schema.CONSUMPTION_ACCOUNT_PREFIX + name);
                    consumptionAccount.setType(Constants.AccountType.EXPENSE);
                    getGroupById(getClassById(model, "5"), Constants.Schema.CONSUMPTION_GROUP_ID).getAccount().add(consumptionAccount);
                    logMsgList.add("Schema Account created: id=" + "5" + Constants.Schema.CONSUMPTION_GROUP_ID + groupId + " name='" + Constants.Schema.CONSUMPTION_ACCOUNT_PREFIX + name + "'");
                    break;
                }
                case "2":
                case "3":
                case "4":
                case "5":
                case "6": break;
                default: throw new IllegalArgumentException("Illegal class id!");
            }

            manager.update(model);
            for (String logMsg : logMsgList){
                Initializer.LOG.info(logMsg);
            }
            invalidateModel();
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
            Manager<SchemaModel> manager = new SchemaManager(year);
            SchemaModel model = manager.retrieve();

            getGroupById(getClassById(model, classId), groupId).setName(newName);

            List<String> logMsgList = new ArrayList<>();
            logMsgList.add("Schema Group id=" + classId + groupId + " renamed to '" + newName + "'");
            switch (classId) {
                case "0": {
                    getAccountById(getGroupById(getClassById(model, "0"), Constants.Schema.ACCUMULATED_DEP_GROUP_ID), groupId)
                            .setName(Constants.Schema.ACCUMULATED_DEP_ACCOUNT_PREFIX + newName);
                    logMsgList.add("Schema Account id=" + "0" + Constants.Schema.ACCUMULATED_DEP_GROUP_ID + groupId + " renamed to '" + Constants.Schema.ACCUMULATED_DEP_ACCOUNT_PREFIX + newName + "'");

                    getAccountById(getGroupById(getClassById(model, "5"), Constants.Schema.DEPRECIATION_GROUP_ID), groupId)
                            .setName(Constants.Schema.DEPRECIATION_ACCOUNT_PREFIX + newName);
                    logMsgList.add("Schema Account id=" + "5" + Constants.Schema.DEPRECIATION_GROUP_ID + groupId + " renamed to '" + Constants.Schema.DEPRECIATION_ACCOUNT_PREFIX + newName + "'");

                    break;
                }
                case "1": {
                    getAccountById(getGroupById(getClassById(model, "5"), Constants.Schema.CONSUMPTION_GROUP_ID), groupId)
                            .setName(Constants.Schema.CONSUMPTION_ACCOUNT_PREFIX + newName);
                    logMsgList.add("Schema Account id=" + "5" + Constants.Schema.CONSUMPTION_GROUP_ID + groupId + " renamed to '" + Constants.Schema.CONSUMPTION_ACCOUNT_PREFIX + newName + "'");
                    break;
                }
                case "2":
                case "3":
                case "4":
                case "5":
                case "6": break;
                default: throw new IllegalArgumentException("Illegal class id!");
            }

            manager.update(model);
            for (String logMsg : logMsgList){
                Initializer.LOG.info(logMsg);
            }
            invalidateModel();
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
            Manager<SchemaModel> manager = new SchemaManager(year);
            SchemaModel model = manager.retrieve();

            SchemaModel.Class clazz = getClassById(model, classId);
            clazz.getGroup().remove(getGroupById(clazz, groupId));

            List<String> logMsgList = new ArrayList<>();
            logMsgList.add("Schema Group id=" + classId + groupId + " deleted");
            switch (classId) {
                case "0": {
                    SchemaModel.Class.Group groupAccDep = getGroupById(getClassById(model, "0"), Constants.Schema.ACCUMULATED_DEP_GROUP_ID);
                    groupAccDep.getAccount().remove(getAccountById(groupAccDep, groupId));
                    logMsgList.add("Schema Account id=" + "0" + Constants.Schema.ACCUMULATED_DEP_GROUP_ID + groupId + " deleted");

                    SchemaModel.Class.Group groupDep = getGroupById(getClassById(model, "5"), Constants.Schema.DEPRECIATION_GROUP_ID);
                    groupDep.getAccount().remove(getAccountById(groupDep, groupId));
                    logMsgList.add("Schema Account id=" + "5" + Constants.Schema.DEPRECIATION_GROUP_ID + groupId + " deleted");
                    break;
                }
                case "1": {
                    SchemaModel.Class.Group groupCons = getGroupById(getClassById(model, "5"), Constants.Schema.CONSUMPTION_GROUP_ID);
                    groupCons.getAccount().remove(getAccountById(groupCons, groupId));
                    logMsgList.add("Schema Account id=" + "5" + Constants.Schema.CONSUMPTION_GROUP_ID + groupId + " deleted");
                    break;
                }
                case "2":
                case "3":
                case "4":
                case "5":
                case "6": break;
                default: throw new IllegalArgumentException("Illegal class id!");
            }

            manager.update(model);
            for (String logMsg : logMsgList){
                Initializer.LOG.info(logMsg);
            }
            invalidateModel();
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
            Manager<SchemaModel> manager = new SchemaManager(year);
            SchemaModel model = manager.retrieve();

            SchemaModel.Class.Group.Account newAcc = new SchemaModel.Class.Group.Account();
            newAcc.setName(name);
            newAcc.setId(accountId);
            newAcc.setType(type);
            getGroupById(getClassById(model, classId), groupId).getAccount().add(newAcc);

            manager.update(model);
            Initializer.LOG.info("Schema Account created: id=" + classId + groupId + accountId + " name='" + name + "'");
            invalidateModel();
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
            Manager<SchemaModel> manager = new SchemaManager(year);
            SchemaModel model = manager.retrieve();

            getAccountById(getGroupById(getClassById(model, classId), groupId), accountId).setName(newName);

            manager.update(model);
            Initializer.LOG.info("Schema Account id=" + classId + groupId + accountId + " renamed to '" + newName + "'");
            invalidateModel();
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
            Manager<SchemaModel> manager = new SchemaManager(year);
            SchemaModel model = manager.retrieve();

            SchemaModel.Class.Group group = getGroupById(getClassById(model, classId), groupId);
            group.getAccount().remove(getAccountById(group, accountId));

            manager.update(model);
            Initializer.LOG.info("Schema Account id=" + classId + groupId + accountId + " deleted");
            invalidateModel();
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
            return getAccountById(getGroupById(getClassById(getModel(year),
                    schemaId.substring(0, 1)),
                    schemaId.substring(1, 2)),
                    schemaId.substring(2, 3)).getType();
        } catch (ManagerException e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Imports schema from one year to another.
     */
    public void importSchema(String fromYear, String toYear) {
        try {
            SchemaModel fromModel = getModel(fromYear);
            fromModel.setYear(toYear);

            new SchemaManager(toYear).update(fromModel);

            invalidateModel();
        } catch (ManagerException e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }
}
