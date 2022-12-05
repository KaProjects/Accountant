package org.kaleta.accountant.service;

import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.backend.manager.Manager;
import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.manager.ProceduresManager;
import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.common.ErrorHandler;

import java.util.ArrayList;
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
        if (!proceduresModel.getYear().equals(year)) {
            proceduresModel = new ProceduresManager(year).retrieve();
        }
        return new ProceduresModel(proceduresModel);
    }

    public void invalidateModel(){
        proceduresModel = null;
    }

    /**
     * Returns list of procedure groups.
     */
    public List<ProceduresModel.Group> getProcedureGroupList(String year) {
        try {
            return getModel(year).getGroup();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns list of procedure group names.
     */
    public List<String> getProcedureGroupNameList(String year) {
        try {
            List<String> names = new ArrayList<>();
            for (ProceduresModel.Group group : getModel(year).getGroup()){
                names.add(group.getName());
            }
            return names;
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Creates procedure according to specified values.
     */
    public void createProcedure(String year, String name, String groupName, List<ProceduresModel.Group.Procedure.Transaction> transactions){
        try {
            Manager<ProceduresModel> manager = new ProceduresManager(year);
            ProceduresModel model = manager.retrieve();

            ProceduresModel.Group group = null;
            for (ProceduresModel.Group groupModel : model.getGroup()){
                if (groupModel.getName().equals(groupName)) group = groupModel;
            }
            if (group == null) {
                ProceduresModel.Group newGroup = new ProceduresModel.Group();
                newGroup.setName(groupName);
                model.getGroup().add(newGroup);
                group = newGroup;
            }

            ProceduresModel.Group.Procedure procedure = new ProceduresModel.Group.Procedure();
            procedure.setName(name);
            procedure.setId(String.valueOf(group.getProcedure().size()));
            procedure.getTransaction().addAll(transactions);
            group.getProcedure().add(procedure);

            manager.update(model);
            Initializer.LOG.info("Procedure created: group=" + groupName + " id=" + procedure.getId() + " name='" + procedure.getName() + "'");
            invalidateModel();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Updates procedure specified by 'id' and 'type' according to new values.
     */
    public void updateProcedure(String year, String id, String newName, String groupName,  List<ProceduresModel.Group.Procedure.Transaction> newTransactions){
        try {
            Manager<ProceduresModel> manager = new ProceduresManager(year);
            ProceduresModel model = manager.retrieve();

            ProceduresModel.Group group = null;
            for (ProceduresModel.Group groupModel : model.getGroup()){
                if (groupModel.getName().equals(groupName)) group = groupModel;
            }
            if (group == null) {
                throw new ManagerException("Group '" + groupName + "' not found!");
            }

            for (ProceduresModel.Group.Procedure procedure : group.getProcedure()){
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
