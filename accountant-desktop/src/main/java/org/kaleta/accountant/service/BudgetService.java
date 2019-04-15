package org.kaleta.accountant.service;

import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.backend.manager.BudgetManager;
import org.kaleta.accountant.backend.manager.Manager;
import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.model.BudgetModel;
import org.kaleta.accountant.common.ErrorHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides access to data source which is related to budget.
 */
public class BudgetService {

    private BudgetModel budgetModel;

    BudgetService() {
        // package-private
    }

    private BudgetModel getModel(String year) throws ManagerException {
        if (budgetModel == null) {
            budgetModel = new BudgetManager(year).retrieve();
        }
        if (!budgetModel.getYear().equals(year)) {
            budgetModel = new BudgetManager(year).retrieve();
        }
        return new BudgetModel(budgetModel);
    }

    public void invalidateModel(){
        budgetModel = null;
    }

    /**
     * Returns rows mapped according to their type.
     */
    public Map<String, List<BudgetModel.Row>> getRowsByType(String year){
        try {
            Map<String, List<BudgetModel.Row>> map = new HashMap<>();

            for (BudgetModel.Row row : getModel(year).getRow()){
                if (!map.keySet().contains(row.getType())) {
                    map.put(row.getType(), new ArrayList<>());
                }
                map.get(row.getType()).add(row);
            }
            return map;
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    public void updateRowMonth(String year, BudgetModel.Row newRow){
        try {
            Manager<BudgetModel> manager = new BudgetManager(year);
            BudgetModel model = manager.retrieve();

            for (BudgetModel.Row row : model.getRow()){
                if (row.getId().equals(newRow.getId())){
                    row.getMonth().clear();
                    row.getMonth().addAll(newRow.getMonth());
                }
            }

            manager.update(model);

            invalidateModel();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

}
