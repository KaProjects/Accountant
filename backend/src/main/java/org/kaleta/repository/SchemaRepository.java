package org.kaleta.repository;

import org.kaleta.entity.xml.Schema;

public interface SchemaRepository {

    void syncSchema(Schema data);

    String getNameById(String year, String accountId);
}
