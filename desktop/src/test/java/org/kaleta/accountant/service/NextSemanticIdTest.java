package org.kaleta.accountant.service;

import org.junit.Assert;
import org.junit.Test;
import org.kaleta.accountant.core.TestParent;

/**
 * A semantic id must never be handed out twice, even after the account holding it has been
 * deleted from a later year. Before the all-years lookup, deleting the highest-numbered
 * account made its id available again and the same id came to mean two different things.
 */
public class NextSemanticIdTest extends TestParent {

    private static final String SCHEMA_ID = "012";
    private static final String NEXT_YEAR = "test2";

    @Test
    public void reservesIdsFromOtherYears() {
        Service.ACCOUNT.createAccount(YEAR, "first", SCHEMA_ID, "0", "");
        Service.ACCOUNT.createAccount(YEAR, "second", SCHEMA_ID, "1", "");
        Service.ACCOUNT.createAccount(YEAR, "third", SCHEMA_ID, "2", "");
        Assert.assertEquals("3", Service.ACCOUNT.getNextSemanticId(YEAR, SCHEMA_ID));

        // a fresh year holds none of them - the ids must still be reserved
        Service.CONFIG.initYearData(NEXT_YEAR);
        Assert.assertEquals("3", Service.ACCOUNT.getNextSemanticId(NEXT_YEAR, SCHEMA_ID));
    }

    @Test
    public void countsOnlyTheRequestedSchemaAccount() {
        Service.ACCOUNT.createAccount(YEAR, "here", SCHEMA_ID, "0", "");
        Service.ACCOUNT.createAccount(YEAR, "elsewhere", "013", "7", "");
        Assert.assertEquals("1", Service.ACCOUNT.getNextSemanticId(YEAR, SCHEMA_ID));
        Assert.assertEquals("8", Service.ACCOUNT.getNextSemanticId(YEAR, "013"));
    }

    @Test
    public void startsAtZeroWhenNothingExists() {
        Assert.assertEquals("0", Service.ACCOUNT.getNextSemanticId(YEAR, SCHEMA_ID));
    }

    @Test
    public void aRetiredIdStillReservesItsNumber() {
        Service.ACCOUNT.createAccount(YEAR, "retired", SCHEMA_ID, "5-2020", "");
        Assert.assertEquals("6", Service.ACCOUNT.getNextSemanticId(YEAR, SCHEMA_ID));
    }

    @Test
    public void reservedNumberReadsTheLeadingNumber() {
        Assert.assertEquals(Integer.valueOf(7), AccountsService.reservedNumber("7"));
        Assert.assertEquals(Integer.valueOf(7), AccountsService.reservedNumber("7-2020"));
        Assert.assertEquals(Integer.valueOf(3), AccountsService.reservedNumber("3-6"));
        Assert.assertEquals(Integer.valueOf(12), AccountsService.reservedNumber("12"));
        Assert.assertNull(AccountsService.reservedNumber("general"));
        Assert.assertNull(AccountsService.reservedNumber(""));
        Assert.assertNull(AccountsService.reservedNumber(null));
    }
}
