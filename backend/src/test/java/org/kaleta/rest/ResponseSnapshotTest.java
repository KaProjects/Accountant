package org.kaleta.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Pins the JSON every read endpoint answers with on the test fixtures, so that a refactoring that
 * changes what the frontend receives - a field renamed, a null now written where it used to be
 * left out, a number written differently - fails here instead of in the browser.
 * <p>
 * Two answers are the same when their JSON is: objects compare by their fields whatever the order
 * of them, arrays by their elements in order, and a field holding null is not the same as a field
 * left out. The snapshots are the answers as the application gave them when they were recorded,
 * from the fixtures in createTestDb.sql, not figures anyone checked by hand; the tests that check
 * the figures are the resources' own.
 * <p>
 * After a change to an answer that is meant, record them again with
 * {@code ./mvnw test -Dtest=ResponseSnapshotTest -Dsnapshots.update=true} and review the
 * difference in the snapshot files before committing it.
 */
@QuarkusTest
public class ResponseSnapshotTest
{
    private static final Path SNAPSHOTS = Path.of("src", "test", "resources", "snapshots");
    private static final ObjectMapper JSON = new ObjectMapper();

    @ParameterizedTest
    @ValueSource(strings = {
            "/account/2019",
            "/account/2019/210",
            "/account/2020/700",
            "/accounting/profit/2019",
            "/accounting/profit",
            "/accounting/cashflow/2020",
            "/accounting/cashflow",
            "/accounting/balance/2020",
            "/accounting/balance",
            "/accounting/2023/transaction/000/month/5",
            "/budget/2019",
            "/budget/2019/transaction/i2/month/9",
            "/chart/config",
            "/financial/assets",
            "/schema/2020",
            "/schema/2023",
            "/transaction/2020/200.0",
            "/view/2018",
            "/view/2018/vacation",
    })
    public void answersAsRecorded(String path) throws IOException
    {
        String body = given().when().get(path).then().statusCode(200).extract().asString();
        Path snapshot = SNAPSHOTS.resolve(path.substring(1).replace('/', '_') + ".json");

        if (Boolean.getBoolean("snapshots.update")) {
            Files.createDirectories(SNAPSHOTS);
            Files.writeString(snapshot, JSON.writerWithDefaultPrettyPrinter().writeValueAsString(JSON.readTree(body)) + "\n");
            return;
        }
        if (!Files.exists(snapshot)) {
            fail("No snapshot of " + path + " - record it with -Dsnapshots.update=true");
        }

        JsonNode expected = JSON.readTree(Files.readString(snapshot));
        JsonNode actual = JSON.readTree(body);
        if (!expected.equals(actual)) {
            // the whole of both, pretty printed, so the report shows where they part
            assertEquals(pretty(expected), pretty(actual), "the answer of " + path + " changed");
        }
    }

    private static String pretty(JsonNode node) throws IOException
    {
        return JSON.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }
}
