package org.kaleta.accountant.service;

import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.backend.manager.Manager;
import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.manager.ProceduresManager;
import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.common.Constants;
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
        return groupByName(model, groupName, false);
    }

    /**
     * Group of that name, created when there is none yet. A group the app maintains itself is
     * created at the front of the file, so that what the app writes stays above the user's own
     * groups instead of appearing wherever the first one happened to be created.
     */
    private ProceduresModel.Group groupByName(ProceduresModel model, String groupName, boolean managed) {
        for (ProceduresModel.Group group : model.getGroup()){
            if (group.getName().equals(groupName)) return group;
        }
        ProceduresModel.Group newGroup = new ProceduresModel.Group();
        newGroup.setName(groupName);
        if (managed) {
            model.getGroup().add(0, newGroup);
        } else {
            model.getGroup().add(newGroup);
        }
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
        create(name, groupName, transactions, false);
    }

    /**
     * Creates a procedure the app writes for itself, in a group of its own kept at the front of the
     * file. Such a procedure is a by-product of creating something else - a financial asset, say -
     * so it is not composed by hand and its group is shown locked in the editor.
     */
    public void createManagedProcedure(String year, String name, String groupName, List<ProceduresModel.Group.Procedure.Transaction> transactions){
        create(name, groupName, transactions, true);
    }

    private void create(String name, String groupName, List<ProceduresModel.Group.Procedure.Transaction> transactions, boolean managed){
        try {
            if (!managed) {
                refuseAppMaintained(groupName);
            }
            Manager<ProceduresModel> manager = new ProceduresManager();
            ProceduresModel model = manager.retrieve();

            ProceduresModel.Group.Procedure procedure = new ProceduresModel.Group.Procedure();
            procedure.setName(name);
            procedure.setId(nextProcedureId(model));
            procedure.getTransaction().addAll(transactions);
            groupByName(model, groupName, managed).getProcedure().add(procedure);

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
                refuseAppMaintained(groupName); // a procedure is never moved into what the app writes
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
     * Creates an empty procedure group. A name that is already taken is left as it is: there is
     * nothing to create, and the group the user meant is already on screen.
     */
    public void createProcedureGroup(String year, String groupName){
        try {
            Manager<ProceduresModel> manager = new ProceduresManager();
            ProceduresModel model = manager.retrieve();

            for (ProceduresModel.Group group : model.getGroup()){
                if (group.getName().equals(groupName)) return;
            }
            groupByName(model, groupName);

            manager.update(model);
            Initializer.LOG.info("Procedure group created: '" + groupName + "'");
            invalidateModel();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Replaces one transaction of a procedure, leaving the rest of it alone.
     * <p>
     * This is how a correction made while booking finds its way back: the amount usually paid has
     * changed, or the account it comes from has, and the procedure is brought up to date from the
     * transaction that was just entered instead of being edited separately.
     */
    public void updateProcedureTransaction(String year, String procedureId, int index,
                                           ProceduresModel.Group.Procedure.Transaction transaction){
        try {
            Manager<ProceduresModel> manager = new ProceduresManager();
            ProceduresModel model = manager.retrieve();

            ProceduresModel.Group.Procedure procedure = null;
            for (ProceduresModel.Group group : model.getGroup()){
                for (ProceduresModel.Group.Procedure candidate : group.getProcedure()){
                    if (candidate.getId().equals(procedureId)) procedure = candidate;
                }
            }
            if (procedure == null) {
                throw new IllegalArgumentException("Procedure id=" + procedureId + " not found!");
            }
            if (index < 0 || index >= procedure.getTransaction().size()) {
                throw new IllegalArgumentException("Procedure id=" + procedureId + " has no transaction " + index);
            }
            procedure.getTransaction().set(index, transaction);

            manager.update(model);
            Initializer.LOG.info("Procedure id=" + procedureId + " transaction " + index + " updated");
            invalidateModel();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Deletes the procedure with this id. The group it was in is kept even when it is left empty:
     * the user deletes a group deliberately, and an emptied one is usually about to be filled again.
     */
    public void deleteProcedure(String year, String id){
        try {
            Manager<ProceduresModel> manager = new ProceduresManager();
            ProceduresModel model = manager.retrieve();

            ProceduresModel.Group.Procedure procedure = null;
            ProceduresModel.Group group = null;
            for (ProceduresModel.Group candidate : model.getGroup()){
                for (ProceduresModel.Group.Procedure procedureCandidate : candidate.getProcedure()){
                    if (procedureCandidate.getId().equals(id)){
                        group = candidate;
                        procedure = procedureCandidate;
                    }
                }
            }
            if (procedure == null) {
                throw new IllegalArgumentException("Procedure id=" + id + " not found!");
            }
            refuseAppMaintained(group.getName());
            group.getProcedure().remove(procedure);

            manager.update(model);
            Initializer.LOG.info("Procedure deleted: id=" + id + " name='" + procedure.getName() + "'");
            invalidateModel();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Deletes a procedure group and every procedure in it. Recorded transactions are untouched:
     * a procedure is only a template of what to book, never the booking itself.
     */
    public void deleteProcedureGroup(String year, String groupName){
        try {
            refuseAppMaintained(groupName);
            Manager<ProceduresModel> manager = new ProceduresManager();
            ProceduresModel model = manager.retrieve();

            ProceduresModel.Group group = null;
            for (ProceduresModel.Group candidate : model.getGroup()){
                if (candidate.getName().equals(groupName)) group = candidate;
            }
            if (group == null) {
                throw new IllegalArgumentException("Group '" + groupName + "' not found!");
            }
            int count = group.getProcedure().size();
            model.getGroup().remove(group);

            manager.update(model);
            Initializer.LOG.info("Procedure group '" + groupName + "' deleted with " + count + " procedure(s)");
            invalidateModel();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /** What the app writes for itself it also owns: those procedures go when their account goes, not before. */
    private void refuseAppMaintained(String groupName){
        if (Constants.Procedure.DERIVED_GROUP_NAMES.contains(groupName)){
            throw new IllegalArgumentException("Group '" + groupName + "' is maintained by the app and cannot be deleted");
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
