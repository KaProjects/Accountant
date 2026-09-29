package org.kaleta.accountant.service;

import org.junit.Assert;
import org.junit.Test;
import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.core.TestParent;

import java.util.ArrayList;
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

    /**
     * An imported movement says which accounts it moved between; a procedure booking the same pair
     * is what that movement is, written down already.
     */
    @Test
    public void aProcedureIsFoundByThePairOfAccountsItBooks() {
        Service.PROCEDURES.createProcedure(YEAR, "rent", "household", transaction("520.0", "210.0"));

        ProceduresModel.Group.Procedure found = Service.PROCEDURES.getProcedureFor(YEAR, "520.0", "210.0");

        Assert.assertNotNull(found);
        Assert.assertEquals("rent", found.getName());
    }

    @Test
    public void aPairNothingBooksIsRecognisedAsNothing() {
        Service.PROCEDURES.createProcedure(YEAR, "rent", "household", transaction("520.0", "210.0"));

        Assert.assertNull("the other way round is not the same booking",
                Service.PROCEDURES.getProcedureFor(YEAR, "210.0", "520.0"));
        Assert.assertNull(Service.PROCEDURES.getProcedureFor(YEAR, "520.9", "210.0"));
        Assert.assertNull("a side that was never guessed recognises nothing",
                Service.PROCEDURES.getProcedureFor(YEAR, null, "210.0"));
    }

    /** Any of a procedure's transactions identifies it, not only the first. */
    @Test
    public void aProcedureIsFoundByAnyOfItsTransactions() {
        List<ProceduresModel.Group.Procedure.Transaction> salary = new ArrayList<>(transaction("302.0", "600.0"));
        salary.addAll(transaction("210.0", "302.0"));
        Service.PROCEDURES.createProcedure(YEAR, "salary", "income", salary);

        Assert.assertEquals("salary", Service.PROCEDURES.getProcedureFor(YEAR, "210.0", "302.0").getName());
    }

    /** A correction made while booking goes back into the one transaction it was made on. */
    @Test
    public void oneTransactionOfAProcedureCanBeReplaced() {
        List<ProceduresModel.Group.Procedure.Transaction> two = new ArrayList<>(transaction("520.0", "210.0"));
        two.addAll(transaction("520.5", "210.0"));
        Service.PROCEDURES.createProcedure(YEAR, "household", "household", two);
        String id = group("household").getProcedure().get(0).getId();

        ProceduresModel.Group.Procedure.Transaction corrected = new ProceduresModel.Group.Procedure.Transaction();
        corrected.setDescription("power");
        corrected.setAmount("250");
        corrected.setDebit("520.5");
        corrected.setCredit("210.1");
        Service.PROCEDURES.updateProcedureTransaction(YEAR, id, 1, corrected);

        List<ProceduresModel.Group.Procedure.Transaction> transactions =
                group("household").getProcedure().get(0).getTransaction();
        Assert.assertEquals(2, transactions.size());
        Assert.assertEquals("100", transactions.get(0).getAmount());
        Assert.assertEquals("250", transactions.get(1).getAmount());
        Assert.assertEquals("210.1", transactions.get(1).getCredit());
    }

    @Test(expected = IllegalArgumentException.class)
    public void aTransactionThatIsNotThereCannotBeReplaced() {
        Service.PROCEDURES.createProcedure(YEAR, "rent", "household", transaction("520.0", "210.0"));
        String id = group("household").getProcedure().get(0).getId();

        Service.PROCEDURES.updateProcedureTransaction(YEAR, id, 3, new ProceduresModel.Group.Procedure.Transaction());
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
