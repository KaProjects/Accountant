package org.kaleta.accountant.service;

import org.junit.Assert;
import org.junit.Test;
import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.core.TestParent;

import java.util.List;

/**
 * What an app started without any data has to come with. The accumulated earnings is the one
 * account the books cannot be closed without - the profit of every year is booked onto it - so a
 * freshly initialised app has to hold it rather than wait for it to be created by hand.
 */
public class DataInitializationTest extends TestParent {

    private static final String SECOND_YEAR = "test2";

    @Test
    public void theSchemaHoldsTheAccumulatedEarnings() {
        SchemaModel.Class.Group.Account account = null;
        for (SchemaModel.Class clazz : Service.SCHEMA.getSchemaClassList(YEAR)) {
            if (!clazz.getId().equals("4")) continue;
            for (SchemaModel.Class.Group group : clazz.getGroup()) {
                if (!group.getId().equals(Constants.Schema.CAPITAL_GROUP_ID)) continue;
                for (SchemaModel.Class.Group.Account candidate : group.getAccount()) {
                    if (candidate.getId().equals(Constants.Schema.ACCUMULATED_EARNINGS_ACCOUNT_ID)) {
                        account = candidate;
                    }
                }
            }
        }
        Assert.assertNotNull("the capital group holds no accumulated earnings account", account);
        Assert.assertEquals(Constants.Schema.ACCUMULATED_EARNINGS_ACCOUNT_NAME, account.getName());
        Assert.assertEquals(Constants.AccountType.LIABILITY, account.getType());
    }

    @Test
    public void theFirstYearHoldsTheAccumulatedEarningsAccount() {
        Assert.assertTrue(Service.ACCOUNT.checkAccountExists(YEAR, Constants.Account.ACCUMULATED_EARNINGS_ACC_ID));
    }

    @Test
    public void aLaterYearDoesNotGetASecondOne() {
        // a year after the first one is opened by the closing, which carries the account over itself
        Service.CONFIG.initYearData(SECOND_YEAR);

        List<AccountsModel.Account> accounts = Service.ACCOUNT.getAllAccounts(SECOND_YEAR);
        long earnings = accounts.stream().filter(account -> account.getFullId()
                .equals(Constants.Account.ACCUMULATED_EARNINGS_ACC_ID)).count();
        Assert.assertEquals(0, earnings);
    }
}
