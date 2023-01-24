package org.kaleta.service;

import org.kaleta.model.BudgetComponent;

public interface BudgetingService
{
    /**
     * @return named budget component for specified ID and year
     */
    BudgetComponent getBudgetComponent(String year, String name, String idPrefix);
}
