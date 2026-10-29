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
        // the tables alone, built by TestDatabase, with no fixtures in them
        return Map.of(
                "quarkus.datasource.jdbc.url", "jdbc:h2:mem:emptydatasource;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=YEAR,VALUE",
                "test.database.fixture", "");
    }
}
