package org.kaleta.rest.data;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

/**
 * A database of its own that nothing ever writes to, so the application can be asked what it does
 * before any data has been synced into it.
 */
public class EmptyDataSourceTestProfile implements QuarkusTestProfile
{
    @Override
    public Map<String, String> getConfigOverrides()
    {
        return Map.of("quarkus.datasource.jdbc.url",
                "jdbc:h2:mem:emptydatasource;MODE=MySQL;NON_KEYWORDS=YEAR,VALUE"
                        + ";INIT=RUNSCRIPT FROM 'sql/createTables.sql'");
    }
}
