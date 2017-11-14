package org.kaleta.accountant.test;

import org.junit.Before;
import org.junit.Test;
import org.kaleta.accountant.backend.manager.Manager;
import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.manager.ProceduresManager;
import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.core.TestParent;

public class ProceduresServiceTest extends TestParent {
    private Manager<ProceduresModel> manager;

    @Before
    public void prepare() throws ManagerException {
        manager = new ProceduresManager(YEAR);
        ProceduresModel model = manager.retrieve();
        ProceduresModel.Procedure procedure = new ProceduresModel.Procedure();
        procedure.setName("p1");
        procedure.setId("-1");
        ProceduresModel.Procedure.Transaction transaction = new ProceduresModel.Procedure.Transaction();
        transaction.setAmount("100");
        transaction.setDebit("123.4");
        transaction.setCredit("567.8");
        transaction.setDescription("default procedure");
        procedure.getTransaction().add(transaction);
        model.getProcedure().add(procedure);
        manager.update(model);
    }


    @Test
    public void testGetProceduresList() throws ManagerException{



        //Service.PROCEDURES.getProcedureList(YEAR);
    }

    @Test
    public void testCreateProcedure() throws ManagerException{




    }
}
