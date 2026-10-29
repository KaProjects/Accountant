package org.kaleta;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class TestDatabaseTest
{
    @Inject
    DataSource dataSource;

    @ConfigProperty(name = "quarkus.datasource.jdbc.url")
    String url;

    @Test
    public void anotherConnectionLeavesWhatWasWrittenInPlace() throws SQLException
    {
        // the connection pool opens connections as it needs them; with an INIT clause on the URL
        // each one rebuilt the tables and lost this row
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO Transaction (id, year, date, description, amount, debit, credit)"
                    + " VALUES ('test-database-marker', '1999', '0101', 'marker', 1, '000.0', '000.0')");
        }
        try {
            try (Connection another = DriverManager.getConnection(url)) {
                assertThat(another.isValid(1), is(true));
            }

            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
                 ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM Transaction WHERE id='test-database-marker'")) {
                rows.next();
                assertThat(rows.getInt(1), is(1));
            }
        } finally {
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                statement.execute("DELETE FROM Transaction WHERE id='test-database-marker'");
            }
        }
    }
}
