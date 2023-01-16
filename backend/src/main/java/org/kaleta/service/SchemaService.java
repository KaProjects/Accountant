package org.kaleta.service;

import org.kaleta.entity.Schema;

import java.util.List;

public interface SchemaService {

    String getAccountName(String year, String accountId);

    String getGroupName(String year, String groupId);

    String getClassName(String year, String classId);

    List<Schema> getSchemaAccountsByGroup(String year, String groupId);

    String getAccountType(String year, String accountId);

    List<Schema> list(String year);
}
