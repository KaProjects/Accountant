package org.kaleta.accountant.service;

import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.frontend.Initializer;
import org.kaleta.accountant.frontend.common.ErrorDialog;
import org.kaleta.accountant.frontend.common.constants.AccountType;
import org.kaleta.accountant.frontend.common.constants.DefaultSchemaId;
import org.kaleta.accountant.frontend.component.year.model.AccountModel;
import org.kaleta.accountant.frontend.component.year.model.SchemaModel;
import org.kaleta.accountant.frontend.component.year.model.YearModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 05.01.2017.
 */
@Deprecated
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
            schemaModel.getClasses().get(2).getGroups().put(1, new SchemaModel.Clazz.Group(1,"Penazne prostriedky"));
            schemaModel.getClasses().get(2).getGroups().get(1).getAccounts().put(1, new SchemaModel.Clazz.Group.Account(1,AccountType.ASSET, "Hotovost"));
            schemaModel.getClasses().get(2).getGroups().get(1).getAccounts().put(2, new SchemaModel.Clazz.Group.Account(2,AccountType.ASSET, "Stravenky"));
            schemaModel.getClasses().get(2).getGroups().put(2, new SchemaModel.Clazz.Group(2,"Bankove Ucty"));
            schemaModel.getClasses().get(2).getGroups().get(2).getAccounts().put(1, new SchemaModel.Clazz.Group.Account(1, AccountType.ASSET, "Debetne"));

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

            model.getAccountModel().getAccounts().add(new AccountModel.Account("000","0", AccountType.ASSET,"HAHA",""));
            model.getAccountModel().getTransactions().add(new AccountModel.Transaction("0","01012017","","0","000.0",DefaultSchemaId.INIT_ACC));
            model.getAccountModel().getAccounts().add(new AccountModel.Account("090","0-0", AccountType.LIABILITY,"ac dep of HAHA",""));
            model.getAccountModel().getTransactions().add(new AccountModel.Transaction("1","01012017","","0",DefaultSchemaId.INIT_ACC,"090.0-0"));
            model.getAccountModel().getAccounts().add(new AccountModel.Account("590","0-0", AccountType.EXPENSE,"dep of HAHA",""));
            model.getAccountModel().getTransactions().add(new AccountModel.Transaction("2","01012017","","0","590.0-0",DefaultSchemaId.INIT_ACC));

            model.getAccountModel().getAccounts().add(new AccountModel.Account("000","1", AccountType.ASSET,"HAHA2",""));
            model.getAccountModel().getTransactions().add(new AccountModel.Transaction("3","01012017","","0","000.1",DefaultSchemaId.INIT_ACC));
            model.getAccountModel().getAccounts().add(new AccountModel.Account("090","0-1", AccountType.LIABILITY,"ac dep of HAHA2",""));
            model.getAccountModel().getTransactions().add(new AccountModel.Transaction("4","01012017","","0",DefaultSchemaId.INIT_ACC,"090.0-1"));
            model.getAccountModel().getAccounts().add(new AccountModel.Account("590","0-1", AccountType.EXPENSE,"dep of HAHA2",""));
            model.getAccountModel().getTransactions().add(new AccountModel.Transaction("5","01012017","","0","590.0-1",DefaultSchemaId.INIT_ACC));

            model.getAccountModel().getAccounts().add(new AccountModel.Account("001","0", AccountType.ASSET,"dasdsad",""));
            model.getAccountModel().getTransactions().add(new AccountModel.Transaction("6","01012017","","0","001.0",DefaultSchemaId.INIT_ACC));
            model.getAccountModel().getAccounts().add(new AccountModel.Account("090","1-0", AccountType.LIABILITY,"ac dep of dasdsad",""));
            model.getAccountModel().getTransactions().add(new AccountModel.Transaction("7","01012017","","0",DefaultSchemaId.INIT_ACC,"090.1-0"));
            model.getAccountModel().getAccounts().add(new AccountModel.Account("590","1-0", AccountType.EXPENSE,"dep of dasdsad",""));
            model.getAccountModel().getTransactions().add(new AccountModel.Transaction("8","01012017","","0","590.1-0",DefaultSchemaId.INIT_ACC));

            model.getAccountModel().getTransactions().add(new AccountModel.Transaction("9","01012017","zzz","1000000","000.0","711"));
            model.getAccountModel().getTransactions().add(new AccountModel.Transaction("10","01032017","yyy","50","000.1","000.0"));
            model.getAccountModel().getTransactions().add(new AccountModel.Transaction("11","01042017","www","30","001.0","000.1"));
            model.getAccountModel().getTransactions().add(new AccountModel.Transaction("12","01052017","vvv","30","000.0","001.0"));
            model.getAccountModel().getTransactions().add(new AccountModel.Transaction("13","01052017","dep","75","590.0-0","090.0-0"));

            model.getAccountModel().getAccounts().add(new AccountModel.Account("100","0", AccountType.ASSET,"default",""));
            model.getAccountModel().getAccounts().add(new AccountModel.Account("580","0-0", AccountType.ASSET,"default-exp",""));

            model.getAccountModel().getAccounts().add(new AccountModel.Account("211","0", AccountType.ASSET,"koruny",""));
            model.getAccountModel().getAccounts().add(new AccountModel.Account("211","1", AccountType.ASSET,"eura",""));
            model.getAccountModel().getAccounts().add(new AccountModel.Account("212","0", AccountType.ASSET,"default",""));


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

    /**
     * todo doc.
     */
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
     * todo doc.
     */
    public void updateAccount(AccountModel model){
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
