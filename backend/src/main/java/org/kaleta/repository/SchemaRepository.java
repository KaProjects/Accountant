package org.kaleta.repository;

import org.kaleta.entity.Schema;

import java.util.List;

public interface SchemaRepository {

    void syncSchema(org.kaleta.entity.xml.Schema data);

    String getNameById(String year, String accountId);

    List<Schema> getAccountByGroup(String year, String groupId);

    Schema getAccountById(String year, String accountId);
}
