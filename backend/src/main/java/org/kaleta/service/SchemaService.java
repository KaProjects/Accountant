package org.kaleta.service;

import org.kaleta.model.SchemaClass;

import java.util.List;
import java.util.Map;

public interface SchemaService
{
    /**
     * @return names of all schema elements for specified year
     */
    Map<String, String> getSchemaNames(String year);

    /**
     * @return names of all schema elements of the latest year
     */
    Map<String, String> getLatestSchemaNames();

    /**
     * @return account type for specified account ID and year
     */
    String getAccountType(String year, String accountId);

    /**
     * @return class model specified by class ID and year
     */
    SchemaClass getClass(String year, String classId);

    /**
     * @return models of all classes for specified year
     */
    Map<String, SchemaClass> getSchema(String year);

    /**
     * @return true if specified schema account is of type Asset or Expense
     */
    boolean isDebitType(String year, String accountId);

    /**
     * @return true if specified schema account is of type Liability or Revenue
     */
    boolean isCreditType(String year, String accountId);

    /**
     * @return all years from data
     */
    List<String> getYears();
}
