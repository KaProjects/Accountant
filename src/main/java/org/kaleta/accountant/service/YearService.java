package org.kaleta.accountant.service;

import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.frontend.Initializer;
import org.kaleta.accountant.frontend.common.ErrorDialog;
import org.kaleta.accountant.frontend.common.constants.AccountType;
import org.kaleta.accountant.frontend.component.year.model.AccountModel;
import org.kaleta.accountant.frontend.component.year.model.SchemaModel;
import org.kaleta.accountant.frontend.component.year.model.YearModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 05.01.2017.
 */
public class YearService {

    YearService(){
        // package-private
    }

    /**
     * todo doc.
     */
    public List<Integer> getYearIds(){
        try {
            List<Integer> years = new ArrayList<>();
            years.add(2010);
            years.add(2011);
            years.add(2012);
            years.add(2013);
            years.add(2014);
            years.add(2015);
            if (true) {
                return years;
            } else {
                throw new ManagerException();
            }
            // TODO: 1/5/17 impl. backend
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo doc.
     */
    public YearModel getYearModel(int yearId){
        try {
            YearModel model = new YearModel();
            model.setActive(yearId == 2015);
            model.setYearId(yearId);
            SchemaModel schemaModel = new SchemaModel();
            model.setSchemaModel(schemaModel);

            schemaModel.getClasses().put(0, new SchemaModel.Clazz(0,"Assets"));
            schemaModel.getClasses().get(0).getGroups().put(0, new SchemaModel.Clazz.Group(0,"TESTGROUP"));
            schemaModel.getClasses().get(0).getGroups().get(0).getAccounts().put(0,new SchemaModel.Clazz.Group.Account(0,"A", "TESTACC"));
            schemaModel.getClasses().get(0).getGroups().get(0).getAccounts().put(1,new SchemaModel.Clazz.Group.Account(1,"A", "asdasaaa111"));
            schemaModel.getClasses().get(0).getGroups().get(0).getAccounts().put(2,new SchemaModel.Clazz.Group.Account(2,"A", "asdasd222"));
            schemaModel.getClasses().get(0).getGroups().put(9, new SchemaModel.Clazz.Group(9,"Accumulated Depreciation"));

            schemaModel.getClasses().put(1, new SchemaModel.Clazz(1,"Resources"));
            schemaModel.getClasses().get(1).getGroups().put(0, new SchemaModel.Clazz.Group(0,"potr"));
            schemaModel.getClasses().get(1).getGroups().get(0).getAccounts().put(0,new SchemaModel.Clazz.Group.Account(0,"A", "msao"));
            schemaModel.getClasses().get(1).getGroups().get(0).getAccounts().put(1,new SchemaModel.Clazz.Group.Account(1,"A", "peciv"));
            schemaModel.getClasses().get(1).getGroups().put(1, new SchemaModel.Clazz.Group(1,"pochutasdsadasd"));
            schemaModel.getClasses().get(1).getGroups().get(1).getAccounts().put(0,new SchemaModel.Clazz.Group.Account(0,"A", "chips"));
            schemaModel.getClasses().get(1).getGroups().get(1).getAccounts().put(1,new SchemaModel.Clazz.Group.Account(1,"A", "clask"));

            schemaModel.getClasses().put(2, new SchemaModel.Clazz(2,"Finance"));

            schemaModel.getClasses().put(3, new SchemaModel.Clazz(3,"Zuc cstary"));
            schemaModel.getClasses().get(3).getGroups().put(1, new SchemaModel.Clazz.Group(1,"zavazky"));

            schemaModel.getClasses().put(4, new SchemaModel.Clazz(4,"kap"));

            schemaModel.getClasses().put(5, new SchemaModel.Clazz(5,"Expenses"));
            schemaModel.getClasses().get(5).getGroups().put(8, new SchemaModel.Clazz.Group(8,"Consumption"));
            schemaModel.getClasses().get(5).getGroups().put(9, new SchemaModel.Clazz.Group(9,"Depreciation"));

            schemaModel.getClasses().put(6, new SchemaModel.Clazz(6,"Revenues"));

            schemaModel.getClasses().put(7, new SchemaModel.Clazz(7,"podr"));

            AccountModel accountModel = new AccountModel();
            model.setAccountModel(accountModel);

            model.getAccountModel().getAccounts().add(new AccountModel.Account("000","0", AccountType.ASSET,"HAHA"));
            model.getAccountModel().getAccounts().add(new AccountModel.Account("000","1", AccountType.ASSET,"HAHA2"));
            model.getAccountModel().getAccounts().add(new AccountModel.Account("001","0", AccountType.ASSET,"dasdsad"));

            model.getAccountModel().getTransactions().add(new AccountModel.Transaction("1","01012017","zzz","100","000.0","711"));
            model.getAccountModel().getTransactions().add(new AccountModel.Transaction("2","01032017","yyy","50","000.1","000.0"));
            model.getAccountModel().getTransactions().add(new AccountModel.Transaction("3","01042017","www","30","001.0","000.1"));
            model.getAccountModel().getTransactions().add(new AccountModel.Transaction("4","01052017","vvv","30","000.0","001.0"));


            if (true) {
                return model;
            } else {
                throw new ManagerException();
            }
            // TODO: 1/5/17 impl. backend
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    public void updateSchema(SchemaModel model){
        try {
            if (true) {
            } else {
                throw new ManagerException();
            }
            // TODO: 1/5/17 impl. backend
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo
     */
    public void createYear(){
        try {
            //mgner create DS, add mandatory groups,acc.



            if (true) {
            } else {
                throw new ManagerException();
            }
            // TODO: 1/5/17 impl. backend
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

}
