package org.kaleta.accountant.service;

import org.junit.Assert;
import org.junit.Test;
import org.kaleta.accountant.backend.model.ConfigModel;
import org.kaleta.accountant.core.TestParent;

/**
 * The mappings that decide what an imported transaction is booked against: a piece of its
 * description, and the account for it. Money spent is booked against a debit account, money
 * received against a credit one, and the two are kept apart.
 */
public class AccountMappingTest extends TestParent {
    private static final boolean DEBIT = true;
    private static final boolean CREDIT = false;

    @Test
    public void aDescriptionIsMatchedOnAnyPartOfIt() {
        Service.CONFIG.addMapping("KAVARNA", "532.0", DEBIT);

        ConfigModel.Mapping.Entry mapping = Service.CONFIG.getMatchingMapping("24.09. KAVARNA PRAHA 4 CZK", DEBIT);

        Assert.assertNotNull(mapping);
        Assert.assertEquals("532.0", mapping.getAccount());
    }

    @Test
    public void aDescriptionNothingMatchesIsMappedByNothing() {
        Service.CONFIG.addMapping("KAVARNA", "532.0", DEBIT);

        Assert.assertNull(Service.CONFIG.getMatchingMapping("24.09. LEKAREN", DEBIT));
        Assert.assertNull(Service.CONFIG.getMatchingMapping(null, DEBIT));
    }

    /** The later line is the more specific correction of the earlier one, so it is the one that wins. */
    @Test
    public void theLastMappingThatMatchesWins() {
        Service.CONFIG.addMapping("KAVARNA", "532.0", DEBIT);
        Service.CONFIG.addMapping("KAVARNA LETISTE", "532.9", DEBIT);

        Assert.assertEquals("532.9",
                Service.CONFIG.getMatchingMapping("24.09. KAVARNA LETISTE PRAHA", DEBIT).getAccount());
    }

    @Test
    public void aMappingIsCorrectedInPlace() {
        Service.CONFIG.addMapping("KAVARNA", "532.0", DEBIT);

        Service.CONFIG.updateMapping("KAVARNA", "KAVARNA PRAHA", "533.1", DEBIT);

        Assert.assertNull("it no longer matches what it used to", Service.CONFIG.getMatchingMapping("KAVARNA BRNO", DEBIT));
        ConfigModel.Mapping.Entry mapping = Service.CONFIG.getMatchingMapping("KAVARNA PRAHA 4", DEBIT);
        Assert.assertEquals("KAVARNA PRAHA", mapping.getSubstring());
        Assert.assertEquals("533.1", mapping.getAccount());
        Assert.assertEquals("one mapping was changed, not added to", 1, Service.CONFIG.getDebitMappings().size());
    }

    /** An income is not booked against an expense account, so the two sides never see each other. */
    @Test
    public void theTwoSidesAreKeptApart() {
        Service.CONFIG.addMapping("AN EMPLOYER", "600.1", CREDIT);

        Assert.assertNull(Service.CONFIG.getMatchingMapping("AN EMPLOYER - salary", DEBIT));
        Assert.assertEquals("600.1", Service.CONFIG.getMatchingMapping("AN EMPLOYER - salary", CREDIT).getAccount());
        Assert.assertTrue(Service.CONFIG.getDebitMappings().isEmpty());
        Assert.assertEquals(1, Service.CONFIG.getCreditMappings().size());
    }

    @Test
    public void aCreditMappingIsCorrectedLikeAnyOther() {
        Service.CONFIG.addMapping("AN EMPLOYER", "600.1", CREDIT);

        Service.CONFIG.updateMapping("AN EMPLOYER", "AN EMPLOYER GLOBAL", "600.2", CREDIT);

        Assert.assertEquals("600.2", Service.CONFIG.getMatchingMapping("AN EMPLOYER GLOBAL SERVIC", CREDIT).getAccount());
        Assert.assertEquals(1, Service.CONFIG.getCreditMappings().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void aCreditMappingThatIsNotThereCannotBeCorrected() {
        Service.CONFIG.addMapping("SOMETHING", "600.1", DEBIT);

        Service.CONFIG.updateMapping("SOMETHING", "SOMETHING ELSE", "600.2", CREDIT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void aMappingThatIsNotThereCannotBeCorrected() {
        Service.CONFIG.updateMapping("NOTHING", "SOMETHING", "532.0", DEBIT);
    }
}
