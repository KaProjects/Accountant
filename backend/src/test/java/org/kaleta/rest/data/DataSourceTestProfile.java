package org.kaleta.rest.data;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

/**
 * Gives the tests in this package a database of their own, holding nothing but the datasource
 * they generate and sync.
 * <p>
 * The rest of the suite works against the fixtures in {@code createTestDb.sql}, whose years
 * would otherwise be mixed in with the generated ones. That matters because these tests assert
 * relationships across the whole timeline - how many months a chart covers, which years an
 * overall view has columns for - so a stray fixture year silently changes the answer.
 */
public class DataSourceTestProfile implements QuarkusTestProfile
{
    @Override
    public Map<String, String> getConfigOverrides()
    {
        return Map.of("quarkus.datasource.jdbc.url",
                "jdbc:h2:mem:datasource;MODE=MySQL;NON_KEYWORDS=YEAR,VALUE"
                        + ";INIT=RUNSCRIPT FROM 'sql/createTables.sql'");
    }
}
