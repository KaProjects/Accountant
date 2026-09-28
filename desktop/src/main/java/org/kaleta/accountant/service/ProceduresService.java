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

    /**
     * Procedures are shared by every year, so the year is accepted for call-site compatibility
     * but is not used to choose a file.
     */
    private ProceduresModel getModel(String year) throws ManagerException {
        if (proceduresModel == null) {
            proceduresModel = new ProceduresManager().retrieve();
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
     * Group of that name, created at the end of the file when there is none yet.
     */
    private ProceduresModel.Group groupByName(ProceduresModel model, String groupName) {
        for (ProceduresModel.Group group : model.getGroup()){
            if (group.getName().equals(groupName)) return group;
        }
        ProceduresModel.Group newGroup = new ProceduresModel.Group();
        newGroup.setName(groupName);
        model.getGroup().add(newGroup);
        return newGroup;
    }

    /**
     * Ids are unique across the whole file, not per group. Numbering them within a group produced
     * the same id several times over, and since a procedure is looked up by its id alone, editing
     * one of them silently overwrote the other.
     */
    private String nextProcedureId(ProceduresModel model) {
        int highest = -1;
        for (ProceduresModel.Group group : model.getGroup()){
            for (ProceduresModel.Group.Procedure procedure : group.getProcedure()){
                try {
                    highest = Math.max(highest, Integer.parseInt(procedure.getId()));
                } catch (NumberFormatException e) {
                    // a non-numeric id cannot collide with the numbers we hand out
                }
            }
        }
        return String.valueOf(highest + 1);
    }

    /**
     * Creates procedure according to specified values.
     */
    public void createProcedure(String year, String name, String groupName, List<ProceduresModel.Group.Procedure.Transaction> transactions){
        try {
            Manager<ProceduresModel> manager = new ProceduresManager();
            ProceduresModel model = manager.retrieve();

            ProceduresModel.Group.Procedure procedure = new ProceduresModel.Group.Procedure();
            procedure.setName(name);
            procedure.setId(nextProcedureId(model));
            procedure.getTransaction().addAll(transactions);
            groupByName(model, groupName).getProcedure().add(procedure);

            manager.update(model);
            Initializer.LOG.info("Procedure created: group=" + groupName + " id=" + procedure.getId() + " name='" + procedure.getName() + "'");
            invalidateModel();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Updates the procedure with this id, moving it to another group when the group name changed.
     * The procedure is looked up across every group, because its id identifies it on its own.
     */
    public void updateProcedure(String year, String id, String newName, String groupName,  List<ProceduresModel.Group.Procedure.Transaction> newTransactions){
        try {
            Manager<ProceduresModel> manager = new ProceduresManager();
            ProceduresModel model = manager.retrieve();

            ProceduresModel.Group sourceGroup = null;
            ProceduresModel.Group.Procedure procedure = null;
            for (ProceduresModel.Group group : model.getGroup()){
                for (ProceduresModel.Group.Procedure candidate : group.getProcedure()){
                    if (candidate.getId().equals(id)){
                        sourceGroup = group;
                        procedure = candidate;
                    }
                }
            }
            if (procedure == null) {
                throw new ManagerException("Procedure id=" + id + " not found!");
            }

            procedure.setName(newName);
            procedure.getTransaction().clear();
            procedure.getTransaction().addAll(newTransactions);

            if (!sourceGroup.getName().equals(groupName)) {
                sourceGroup.getProcedure().remove(procedure);
                groupByName(model, groupName).getProcedure().add(procedure);
                if (sourceGroup.getProcedure().isEmpty()) {
                    model.getGroup().remove(sourceGroup); // an empty group is only an empty card
                }
                Initializer.LOG.info("Procedure id=" + id + " moved from group '" + sourceGroup.getName() + "' to '" + groupName + "'");
            }

            manager.update(model);
            Initializer.LOG.info("Procedure updated: id=" + id + " name='" + newName + "'");
            invalidateModel();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Renames a procedure group. Renaming it onto an existing group merges the two, which is the
     * only sensible reading of giving it a name that is already taken.
     */
    public void renameProcedureGroup(String year, String groupName, String newName){
        try {
            Manager<ProceduresModel> manager = new ProceduresManager();
            ProceduresModel model = manager.retrieve();

            ProceduresModel.Group group = null;
            for (ProceduresModel.Group candidate : model.getGroup()){
                if (candidate.getName().equals(groupName)) group = candidate;
            }
            if (group == null) {
                throw new ManagerException("Group '" + groupName + "' not found!");
            }

            ProceduresModel.Group existing = null;
            for (ProceduresModel.Group candidate : model.getGroup()){
                if (candidate != group && candidate.getName().equals(newName)) existing = candidate;
            }
            if (existing == null) {
                group.setName(newName);
                Initializer.LOG.info("Procedure group '" + groupName + "' renamed to '" + newName + "'");
            } else {
                existing.getProcedure().addAll(group.getProcedure());
                model.getGroup().remove(group);
                Initializer.LOG.info("Procedure group '" + groupName + "' merged into '" + newName + "'");
            }

            manager.update(model);
            invalidateModel();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

}
