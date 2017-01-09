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
            model.getSchema().getClasses().put(0, new YearModel.Schema.Clazz("majetok"));
            model.getSchema().getClasses().get(0).getGroups().put(0, new YearModel.Schema.Clazz.Group("nwhm"));
            model.getSchema().getClasses().get(0).getGroups().get(0).getAccounts().put(0,new YearModel.Schema.Clazz.Group.Account("A", "asdasd"));
            model.getSchema().getClasses().get(0).getGroups().get(0).getAccounts().put(1,new YearModel.Schema.Clazz.Group.Account("A", "asdasaaa111"));
            model.getSchema().getClasses().get(0).getGroups().get(0).getAccounts().put(2,new YearModel.Schema.Clazz.Group.Account("A", "asdasd222"));

            model.getSchema().getClasses().put(1, new YearModel.Schema.Clazz("zasobuy"));
            model.getSchema().getClasses().get(1).getGroups().put(0, new YearModel.Schema.Clazz.Group("potr"));
            model.getSchema().getClasses().get(1).getGroups().get(0).getAccounts().put(0,new YearModel.Schema.Clazz.Group.Account("A", "msao"));
            model.getSchema().getClasses().get(1).getGroups().get(0).getAccounts().put(1,new YearModel.Schema.Clazz.Group.Account("A", "peciv"));
            model.getSchema().getClasses().get(1).getGroups().put(1, new YearModel.Schema.Clazz.Group("pochutasdsadasd"));
            model.getSchema().getClasses().get(1).getGroups().get(1).getAccounts().put(0,new YearModel.Schema.Clazz.Group.Account("A", "chips"));
            model.getSchema().getClasses().get(1).getGroups().get(1).getAccounts().put(1,new YearModel.Schema.Clazz.Group.Account("A", "clask"));

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
