package org.kaleta.accountant.frontend.component.accounts;

import org.junit.Assert;
import org.junit.Test;

/**
 * Which schema accounts may be added to from a list of accounts. Most may, through whichever dialog
 * that kind of account needs; an asset may not, because it is a purchase with a depreciation plan
 * rather than an account, and is created from the tab that owns it.
 */
public class AccountsEditorRulesTest {

    @Test
    public void theUsersOwnChartCanBeAddedTo() {
        Assert.assertTrue(AccountsEditorRules.canCreateAccount("112"));  // a resource, with its consumption mirror
        Assert.assertTrue(AccountsEditorRules.canCreateAccount("210"));  // a current account, with its withdrawal
        Assert.assertTrue(AccountsEditorRules.canCreateAccount("220"));  // a loan, with its repayment
        Assert.assertTrue(AccountsEditorRules.canCreateAccount("231"));  // a financial asset, through its dialog
        Assert.assertTrue(AccountsEditorRules.canCreateAccount("520"));
    }

    @Test
    public void whatTheAppWritesItselfCannot() {
        Assert.assertFalse(AccountsEditorRules.canCreateAccount("020"));  // an asset, created from the assets tab
        Assert.assertFalse(AccountsEditorRules.canCreateAccount("093"));  // a. d. of an asset group
        Assert.assertFalse(AccountsEditorRules.canCreateAccount("503"));  // d. of an asset group
        Assert.assertFalse(AccountsEditorRules.canCreateAccount("512"));  // c. of a resource group
        Assert.assertFalse(AccountsEditorRules.canCreateAccount("549"));  // creation of a financial asset
        Assert.assertFalse(AccountsEditorRules.canCreateAccount("548"));
        Assert.assertFalse(AccountsEditorRules.canCreateAccount("629"));
        Assert.assertFalse(AccountsEditorRules.canCreateAccount("700"));  // off balance
    }
}
