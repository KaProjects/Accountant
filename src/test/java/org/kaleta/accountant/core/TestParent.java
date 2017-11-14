package org.kaleta.accountant.core;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.service.Service;
import org.kaleta.accountant.service.ServiceFailureException;

import java.io.File;
import java.io.IOException;

public class TestParent {
    public static final String YEAR = "test";

    @Before
    public void setupTestWorkspace() throws ManagerException, IOException {
        Initializer.CONTEXT = Constants.Context.TEST;
        FileUtils.deleteDirectory(new File(Initializer.getDataSource()));
        try {
            Service.CONFIG.checkResources();
            Service.CONFIG.checkData();
            Service.CONFIG.initYearData(YEAR);
            Service.CONFIG.setActiveYear(YEAR);
        } catch (ServiceFailureException e) {
            e.printStackTrace();
            Assert.fail("unable to prepare test workspace");
        }
    }
}
