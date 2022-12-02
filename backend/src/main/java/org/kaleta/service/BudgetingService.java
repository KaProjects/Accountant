package org.kaleta.service;

import org.kaleta.model.BudgetComponent;

public interface BudgetingService {

    BudgetComponent getBudgetComponent(String year, String name, String idPrefix);
}
