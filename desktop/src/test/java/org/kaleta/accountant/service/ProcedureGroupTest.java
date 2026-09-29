package org.kaleta.accountant.service;

import org.junit.Assert;
import org.junit.Test;
import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.core.TestParent;

import java.util.Collections;
import java.util.List;

/**
 * The procedure file holds two kinds of group: the user's own, which are created, renamed and
 * deleted by hand, and the one the app writes for itself, which it keeps at the front and owns.
 */
public class ProcedureGroupTest extends TestParent {

    private static final String MANAGED = Constants.Procedure.FIN_CREATION_GROUP_NAME;

    private List<ProceduresModel.Group> groups() {
        return Service.PROCEDURES.getProcedureGroupList(YEAR);
    }

    private ProceduresModel.Group group(String name) {
        for (ProceduresModel.Group group : groups()) {
            if (group.getName().equals(name)) return group;
        }
        return null;
    }

    private static List<ProceduresModel.Group.Procedure.Transaction> transaction(String debit, String credit) {
        ProceduresModel.Group.Procedure.Transaction transaction = new ProceduresModel.Group.Procedure.Transaction();
        transaction.setDescription("");
        transaction.setAmount("100");
        transaction.setDebit(debit);
        transaction.setCredit(credit);
        return Collections.singletonList(transaction);
    }

    /** What the app writes belongs above the user's own groups, whenever it happens to be written. */
    @Test
    public void managedGroupIsKeptAtTheFront() {
        Service.PROCEDURES.createProcedure(YEAR, "rent", "household", transaction("520.0", "210.0"));
        Service.PROCEDURES.createManagedProcedure(YEAR, "creation of fund", MANAGED, transaction("549.0-0", "210.0"));

        Assert.assertEquals(MANAGED, groups().get(0).getName());
        Assert.assertEquals("household", groups().get(1).getName());
    }

    @Test
    public void everyManagedProcedureJoinsTheSameGroup() {
        Service.PROCEDURES.createManagedProcedure(YEAR, "creation of first", MANAGED, transaction("549.0-0", "210.0"));
        Service.PROCEDURES.createManagedProcedure(YEAR, "creation of second", MANAGED, transaction("549.0-1", "210.0"));

        Assert.assertEquals(1, groups().size());
        Assert.assertEquals(2, group(MANAGED).getProcedure().size());
    }

    /** An emptied group stays: it is deleted deliberately, not as a side effect of the last delete. */
    @Test
    public void deletingAProcedureKeepsItsGroup() {
        Service.PROCEDURES.createProcedure(YEAR, "rent", "household", transaction("520.0", "210.0"));
        String id = group("household").getProcedure().get(0).getId();

        Service.PROCEDURES.deleteProcedure(YEAR, id);

        Assert.assertNotNull(group("household"));
        Assert.assertTrue(group("household").getProcedure().isEmpty());
    }

    @Test
    public void deletingAGroupTakesItsProceduresWithIt() {
        Service.PROCEDURES.createProcedure(YEAR, "rent", "household", transaction("520.0", "210.0"));
        Service.PROCEDURES.createProcedure(YEAR, "power", "household", transaction("520.5", "210.0"));

        Service.PROCEDURES.deleteProcedureGroup(YEAR, "household");

        Assert.assertNull(group("household"));
    }

    /** The "new group" card names a group and nothing else; it is filled from its own card after. */
    @Test
    public void anEmptyGroupCanBeCreatedAndFilledAfterwards() {
        Service.PROCEDURES.createProcedureGroup(YEAR, "household");

        Assert.assertNotNull(group("household"));
        Assert.assertTrue(group("household").getProcedure().isEmpty());

        Service.PROCEDURES.createProcedure(YEAR, "rent", "household", transaction("520.0", "210.0"));

        Assert.assertEquals(1, groups().size());
        Assert.assertEquals(1, group("household").getProcedure().size());
    }

    @Test
    public void creatingAGroupThatExistsChangesNothing() {
        Service.PROCEDURES.createProcedure(YEAR, "rent", "household", transaction("520.0", "210.0"));

        Service.PROCEDURES.createProcedureGroup(YEAR, "household");

        Assert.assertEquals(1, groups().size());
        Assert.assertEquals(1, group("household").getProcedure().size());
    }

    /** Every derived group is the app's, not only the first one that was added. */
    @Test
    public void noDerivedGroupCanBeDeleted() {
        for (String name : Constants.Procedure.DERIVED_GROUP_NAMES) {
            Service.PROCEDURES.createManagedProcedure(YEAR, "written by the app", name, transaction("220.0", "210.0"));
            try {
                Service.PROCEDURES.deleteProcedureGroup(YEAR, name);
                Assert.fail("deleting the derived group '" + name + "' should have been refused");
            } catch (IllegalArgumentException expected) {
                // that is the point
            }
            Assert.assertNotNull(group(name));
        }
    }

    /** A derived group is the app's to fill: nothing is composed into it, by the dialog or by hand. */
    @Test(expected = IllegalArgumentException.class)
    public void aProcedureCannotBeCreatedInADerivedGroup() {
        Service.PROCEDURES.createProcedure(YEAR, "by hand", MANAGED, transaction("549.0-0", "210.0"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void aProcedureCannotBeMovedIntoADerivedGroup() {
        Service.PROCEDURES.createProcedure(YEAR, "rent", "household", transaction("520.0", "210.0"));
        String id = group("household").getProcedure().get(0).getId();

        Service.PROCEDURES.updateProcedure(YEAR, id, "rent", MANAGED, transaction("520.0", "210.0"));
    }

    /** Editing one in place is what the group is opened for, and stays allowed. */
    @Test
    public void aDerivedProcedureCanBeEditedWhereItIs() {
        Service.PROCEDURES.createManagedProcedure(YEAR, "creation of fund", MANAGED, transaction("549.0-0", "210.0"));
        String id = group(MANAGED).getProcedure().get(0).getId();

        Service.PROCEDURES.updateProcedure(YEAR, id, "creation of fund", MANAGED, transaction("549.0-0", "211.7"));

        Assert.assertEquals("211.7", group(MANAGED).getProcedure().get(0).getTransaction().get(0).getCredit());
    }

    @Test(expected = IllegalArgumentException.class)
    public void theManagedGroupCannotBeDeleted() {
        Service.PROCEDURES.createManagedProcedure(YEAR, "creation of fund", MANAGED, transaction("549.0-0", "210.0"));

        Service.PROCEDURES.deleteProcedureGroup(YEAR, MANAGED);
    }

    @Test(expected = IllegalArgumentException.class)
    public void aManagedProcedureCannotBeDeletedOnItsOwn() {
        Service.PROCEDURES.createManagedProcedure(YEAR, "creation of fund", MANAGED, transaction("549.0-0", "210.0"));

        Service.PROCEDURES.deleteProcedure(YEAR, group(MANAGED).getProcedure().get(0).getId());
    }
}
