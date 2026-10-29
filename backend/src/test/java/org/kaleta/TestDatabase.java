package org.kaleta;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * Builds the in-memory test database once, each time a test application starts.
 * <p>
 * The tables used to be created by an INIT clause on the JDBC URL. H2 runs a URL's INIT on every
 * connection it opens, not once, and the table script starts by dropping every table: so whenever
 * the pool opened another connection - two requests at once, or a test that reads while another
 * connection is busy - the database was rebuilt underneath the test, losing whatever it had synced
 * or written. The same clause emptied the dev database (see DevDataInitializer), and it was fixed
 * there the same way. The URLs now keep their database open themselves, with DB_CLOSE_DELAY=-1.
 * <p>
 * Each test profile starts its own application with a database of its own name, and every start
 * rebuilds that database here, so a test class still begins on exactly the data it always has:
 * the tables, and the fixtures named by {@code test.database.fixture} - none for the profiles that
 * sync a generated datasource instead.
 */
@ApplicationScoped
public class TestDatabase
{
    @Inject
    DataSource dataSource;

    @ConfigProperty(name = "test.database.fixture")
    Optional<String> fixture;

    void onStart(@Observes @Priority(1) StartupEvent event) throws SQLException
    {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("RUNSCRIPT FROM 'sql/createTables.sql'");
            if (fixture.isPresent()) {
                statement.execute("RUNSCRIPT FROM '" + fixture.get() + "'");
            }
        }
    }
}
