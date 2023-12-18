package org.kaleta.service;

import org.kaleta.entity.Schema;
import org.kaleta.model.SchemaClass;

import java.util.List;

public interface SchemaService
{
    /**
     * @return account name for specified ID and year
     */
    String getAccountName(String year, String accountId);

    /**
     * @return group name for specified ID and year
     */
    String getGroupName(String year, String groupId);

    /**
     * @return class name for specified ID and year
     */
    String getClassName(String year, String classId);

    /**
     * @return list of accounts for specified group
     */
    List<Schema> getSchemaAccountsByGroup(String year, String groupId);

    /**
     * @return account type for specified account ID and year
     */
    String getAccountType(String year, String accountId);

    /**
     * @return list of all schema objects (classes, groups, schema accounts) for specified year
     */
    List<Schema> list(String year);

    /**
     * @return class model specified by class ID and year
     */
    SchemaClass getClass(String year, String classId);

    /**
     * @return true if specified schema account is of type Asset or Expense
     */
    boolean isDebitType(String year, String accountId);

    /**
     * @return true if specified schema account is of type Liability or Revenue
     */
    boolean isCreditType(String year, String accountId);
}
