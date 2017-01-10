package org.kaleta.accountant.service;

import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.frontend.Initializer;
import org.kaleta.accountant.frontend.common.ErrorDialog;
import org.kaleta.accountant.frontend.component.year.YearModel;

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
    public List<String> getYearNames(){
        try {
            List<String> years = new ArrayList<>();
            years.add("2010");
            years.add("2011");
            years.add("2012");
            years.add("2013");
            years.add("2014");
            years.add("2015");
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
    public YearModel getYearModel(int yearIndex){
        try {
            YearModel model = new YearModel();
            model.setActive(yearIndex == 5);
            model.getClasses().put(0, new YearModel.Clazz(0,"majetok"));
            model.getClasses().get(0).getGroups().put(0, new YearModel.Clazz.Group(0,"nwhm"));
            model.getClasses().get(0).getGroups().get(0).getAccounts().put(0,new YearModel.Clazz.Group.Account(0,"A", "asdasd"));
            model.getClasses().get(0).getGroups().get(0).getAccounts().put(1,new YearModel.Clazz.Group.Account(1,"A", "asdasaaa111"));
            model.getClasses().get(0).getGroups().get(0).getAccounts().put(2,new YearModel.Clazz.Group.Account(2,"A", "asdasd222"));

            model.getClasses().put(1, new YearModel.Clazz(1,"zasobuy"));
            model.getClasses().get(1).getGroups().put(0, new YearModel.Clazz.Group(0,"potr"));
            model.getClasses().get(1).getGroups().get(0).getAccounts().put(0,new YearModel.Clazz.Group.Account(0,"A", "msao"));
            model.getClasses().get(1).getGroups().get(0).getAccounts().put(1,new YearModel.Clazz.Group.Account(1,"A", "peciv"));
            model.getClasses().get(1).getGroups().put(1, new YearModel.Clazz.Group(1,"pochutasdsadasd"));
            model.getClasses().get(1).getGroups().get(1).getAccounts().put(0,new YearModel.Clazz.Group.Account(0,"A", "chips"));
            model.getClasses().get(1).getGroups().get(1).getAccounts().put(1,new YearModel.Clazz.Group.Account(1,"A", "clask"));

            model.getClasses().put(2, new YearModel.Clazz(2,"Finanace"));

            model.getClasses().put(3, new YearModel.Clazz(3,"Zuc cstary"));
            model.getClasses().get(3).getGroups().put(1, new YearModel.Clazz.Group(1,"zavazky"));

            model.getClasses().put(4, new YearModel.Clazz(2,"kap"));

            model.getClasses().put(5, new YearModel.Clazz(2,"N"));

            model.getClasses().put(6, new YearModel.Clazz(2,"V"));

            model.getClasses().put(7, new YearModel.Clazz(2,"podr"));

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

}
