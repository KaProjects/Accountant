package org.kaleta.accountant.service;

import org.junit.Assert;
import org.junit.Test;
import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.core.TestParent;

/**
 * An account may be renamed, and only renamed: its id is what every transaction refers to, so a
 * rename must leave the id, and therefore the account's whole history, exactly as it was.
 */
public class RenameAccountTest extends TestParent {

    private static final String SCHEMA_ID = "300";

    @Test
    public void renamesWithoutTouchingTheId() {
        AccountsModel.Account created = Service.ACCOUNT.createAccount(YEAR, "old name", SCHEMA_ID, "0", "");
        String fullId = created.getFullId();

        Service.ACCOUNT.renameAccount(YEAR, fullId, "new name");

        AccountsModel.Account reloaded = Service.ACCOUNT.getAccount(YEAR, fullId);
        Assert.assertEquals("new name", reloaded.getName());
        Assert.assertEquals(fullId, reloaded.getFullId());
        Assert.assertEquals(SCHEMA_ID, reloaded.getSchemaId());
        Assert.assertEquals("0", reloaded.getSemanticId());
    }

    @Test
    public void leavesEveryOtherAccountAlone() {
        Service.ACCOUNT.createAccount(YEAR, "first", SCHEMA_ID, "0", "");
        Service.ACCOUNT.createAccount(YEAR, "second", SCHEMA_ID, "1", "");

        Service.ACCOUNT.renameAccount(YEAR, SCHEMA_ID + ".0", "renamed");

        Assert.assertEquals("renamed", Service.ACCOUNT.getAccount(YEAR, SCHEMA_ID + ".0").getName());
        Assert.assertEquals("second", Service.ACCOUNT.getAccount(YEAR, SCHEMA_ID + ".1").getName());
    }

    /**
     * An asset's accumulated depreciation and depreciation accounts are named after it, so a rename
     * that stopped at the asset itself would leave the same asset under two names in the reports.
     */
    @Test
    public void renamesTheAccountsNamedAfterAnAsset() {
        AccountsModel.Account asset = Service.ACCOUNT.createAccount(YEAR, "old car", "022", "0", "");
        String accDepId = Service.ACCOUNT.getAccumulatedDepAccountId("022", "0");
        String depId = Service.ACCOUNT.getDepreciationAccountId("022", "0");
        Service.ACCOUNT.createAccount(YEAR, "a. d. of old car", accDepId.split("\\.")[0], accDepId.split("\\.")[1], "");
        Service.ACCOUNT.createAccount(YEAR, "d. of old car", depId.split("\\.")[0], depId.split("\\.")[1], "");

        Service.ACCOUNT.renameAccountWithRelated(YEAR, asset, "new car");

        Assert.assertEquals("new car", Service.ACCOUNT.getAccount(YEAR, "022.0").getName());
        Assert.assertEquals("a. d. of new car", Service.ACCOUNT.getAccount(YEAR, accDepId).getName());
        Assert.assertEquals("d. of new car", Service.ACCOUNT.getAccount(YEAR, depId).getName());
    }

    /** The same for the three accounts a long-term financial asset is booked through. */
    @Test
    public void renamesTheAccountsNamedAfterAFinancialAsset() {
        AccountsModel.Account fin = Service.ACCOUNT.createAccount(YEAR, "old fund", "231", "0", "");
        String creationId = Service.ACCOUNT.getFinCreationAccountId("231", "0");
        String revRevalId = Service.ACCOUNT.getFinRevRevaluationAccountId("231", "0");
        String expRevalId = Service.ACCOUNT.getFinExpRevaluationAccountId("231", "0");
        for (String id : new String[]{creationId, revRevalId, expRevalId}) {
            Service.ACCOUNT.createAccount(YEAR, "of old fund", id.split("\\.")[0], id.split("\\.")[1], "");
        }

        Service.ACCOUNT.renameAccountWithRelated(YEAR, fin, "new fund");

        Assert.assertEquals("new fund", Service.ACCOUNT.getAccount(YEAR, "231.0").getName());
        Assert.assertEquals("creation of new fund", Service.ACCOUNT.getAccount(YEAR, creationId).getName());
        Assert.assertEquals("revaluation of new fund", Service.ACCOUNT.getAccount(YEAR, revRevalId).getName());
        Assert.assertEquals("revaluation of new fund", Service.ACCOUNT.getAccount(YEAR, expRevalId).getName());
    }

    /** Nothing is required to exist: an asset with no depreciation account is renamed on its own. */
    @Test
    public void skipsRelatedAccountsThatWereNeverCreated() {
        AccountsModel.Account asset = Service.ACCOUNT.createAccount(YEAR, "bare", "022", "1", "");

        Service.ACCOUNT.renameAccountWithRelated(YEAR, asset, "still bare");

        Assert.assertEquals("still bare", Service.ACCOUNT.getAccount(YEAR, "022.1").getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void refusesAnAccountThatDoesNotExist() {
        Service.ACCOUNT.renameAccount(YEAR, SCHEMA_ID + ".7", "nowhere");
    }
}
