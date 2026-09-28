package org.kaleta.accountant.frontend.action.menu;

import org.junit.Assert;
import org.junit.Test;
import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.core.TestParent;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.service.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Invalidating the models is meant to show what is on disk right now. Dropping the caches is not
 * enough on its own: a view keeps what it was last given until it is told the model changed, which
 * is why every kind of model is announced afterwards.
 */
public class InvalidateModelsTest extends TestParent {

    @Test
    public void announcesEveryKindOfModelAfterDroppingTheCaches() {
        RecordingConfiguration configuration = new RecordingConfiguration();

        new InvalidateModels(configuration).actionPerformed();

        Assert.assertEquals(Arrays.asList(Configuration.SCHEMA_UPDATED, Configuration.ACCOUNT_UPDATED,
                Configuration.TRANSACTION_UPDATED, Configuration.PROCEDURE_UPDATED), configuration.commands);
    }

    /** The procedures are read from a file shared by every year, and must be re-read as well. */
    @Test
    public void theProceduresAreReadAgainAfterwards() {
        ProceduresModel.Group.Procedure.Transaction transaction = new ProceduresModel.Group.Procedure.Transaction();
        transaction.setDescription("rent");
        transaction.setAmount("100");
        transaction.setDebit("520.0");
        transaction.setCredit("210.0");
        Service.PROCEDURES.createProcedure(YEAR, "rent", "household", Collections.singletonList(transaction));
        Assert.assertEquals(1, Service.PROCEDURES.getProcedureGroupList(YEAR).size());

        new InvalidateModels(new RecordingConfiguration()).actionPerformed();

        Assert.assertEquals(1, Service.PROCEDURES.getProcedureGroupList(YEAR).size());
        Assert.assertEquals("household", Service.PROCEDURES.getProcedureGroupList(YEAR).get(0).getName());
    }

    private static class RecordingConfiguration implements Configuration {
        private final List<Integer> commands = new ArrayList<>();

        @Override
        public void update(int command) {
            commands.add(command);
        }

        @Override
        public void selectYear(String yearId) {
            // not used here
        }

        @Override
        public String getSelectedYear() {
            return YEAR;
        }
    }
}
