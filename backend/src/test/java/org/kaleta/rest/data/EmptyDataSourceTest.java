package org.kaleta.rest.data;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

/**
 * An empty database is an ordinary state, not a fault: it is what production looks like before the
 * first sync, and what any environment looks like after the database is recreated. Every view has
 * to answer with an empty view of itself rather than failing.
 * <p>
 * Four of them did not. The balance sheet failed for a single year and overall, and the overall
 * income statement and cash flow failed, all with the container's own 500 and a stack trace.
 */
@QuarkusTest
@TestProfile(EmptyDataSourceTestProfile.class)
public class EmptyDataSourceTest
{
    private static final List<String> EVERY_VIEW = List.of(
            "/accounting/balance/2023",
            "/accounting/balance",
            "/accounting/profit/2023",
            "/accounting/profit",
            "/accounting/cashflow/2023",
            "/accounting/cashflow",
            "/financial/assets/2023",
            "/financial/assets",
            "/budget/2023",
            "/chart/config",
            "/schema/2023",
            "/account/2023",
            "/transaction/2023/200.0",
            "/view/2023");

    @Test
    public void everyViewAnswersWithoutData()
    {
        List<String> failed = EVERY_VIEW.stream()
                .filter(path -> given().when().get(path).statusCode() != 200)
                .map(path -> path + " -> " + given().when().get(path).statusCode())
                .toList();

        assertThat("these fail when the database is empty: " + failed, failed, is(empty()));
    }

    @Test
    public void anEmptyStatementHasItsHeadingAndNoRows()
    {
        given().when().get("/accounting/balance/2023").then().statusCode(200)
                .body("columns[0]", is("Balance Sheet"));

        given().when().get("/accounting/profit").then().statusCode(200)
                .body("columns[0]", is("Yearly Income Statement"))
                .body("rows", is(empty()));
    }
}
