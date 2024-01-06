package org.kaleta.service;

import org.kaleta.entity.Transaction;

import java.util.List;
import java.util.Map;

public interface ViewService
{
    /**
     * @return map of vacation's transactions for specified year
     */
    Map<String, List<Transaction>> getVacationMap(String year);

    /**
     * @return map of view's transactions for specified year
     */
    Map<String, List<Transaction>> getViewMap(String year);
}
