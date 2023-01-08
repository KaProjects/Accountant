package org.kaleta.service;

public interface SchemaService {

    String getAccountName(String year, String accountId);

    String getGroupName(String year, String groupId);

    String getClassName(String year, String classId);
}
