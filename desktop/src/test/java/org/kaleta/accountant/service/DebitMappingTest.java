package org.kaleta.accountant.service;

import org.junit.Assert;
import org.junit.Test;
import org.kaleta.accountant.backend.model.ConfigModel;
import org.kaleta.accountant.core.TestParent;

/**
 * The mappings that decide what an imported transaction is booked against: a piece of its
 * description, and the account for it.
 */
public class DebitMappingTest extends TestParent {

    @Test
    public void aDescriptionIsMatchedOnAnyPartOfIt() {
        Service.CONFIG.addDebitMapping("KAVARNA", "532.0");

        ConfigModel.Mapping.Debit mapping = Service.CONFIG.getMatchingDebitMapping("24.09. KAVARNA PRAHA 4 CZK");

        Assert.assertNotNull(mapping);
        Assert.assertEquals("532.0", mapping.getAccount());
    }

    @Test
    public void aDescriptionNothingMatchesIsMappedByNothing() {
        Service.CONFIG.addDebitMapping("KAVARNA", "532.0");

        Assert.assertNull(Service.CONFIG.getMatchingDebitMapping("24.09. LEKAREN"));
        Assert.assertNull(Service.CONFIG.getMatchingDebitMapping(null));
    }

    /** The later line is the more specific correction of the earlier one, so it is the one that wins. */
    @Test
    public void theLastMappingThatMatchesWins() {
        Service.CONFIG.addDebitMapping("KAVARNA", "532.0");
        Service.CONFIG.addDebitMapping("KAVARNA LETISTE", "532.9");

        Assert.assertEquals("532.9",
                Service.CONFIG.getMatchingDebitMapping("24.09. KAVARNA LETISTE PRAHA").getAccount());
    }

    @Test
    public void aMappingIsCorrectedInPlace() {
        Service.CONFIG.addDebitMapping("KAVARNA", "532.0");

        Service.CONFIG.updateDebitMapping("KAVARNA", "KAVARNA PRAHA", "533.1");

        Assert.assertNull("it no longer matches what it used to", Service.CONFIG.getMatchingDebitMapping("KAVARNA BRNO"));
        ConfigModel.Mapping.Debit mapping = Service.CONFIG.getMatchingDebitMapping("KAVARNA PRAHA 4");
        Assert.assertEquals("KAVARNA PRAHA", mapping.getSubstring());
        Assert.assertEquals("533.1", mapping.getAccount());
        Assert.assertEquals("one mapping was changed, not added to", 1, Service.CONFIG.getDebitMappings().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void aMappingThatIsNotThereCannotBeCorrected() {
        Service.CONFIG.updateDebitMapping("NOTHING", "SOMETHING", "532.0");
    }
}
