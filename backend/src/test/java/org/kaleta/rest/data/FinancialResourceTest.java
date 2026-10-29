package org.kaleta.rest.data;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kaleta.dto.FinancialAssetsDto;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@TestProfile(DataSourceTestProfile.class)
public class FinancialResourceTest
{
    // RestAssured only learns the application's port per test method, so the datasource is
    // written and synced here rather than once for the class.
    @BeforeEach
    void syncGeneratedDataSource()
    {
        FakeDataSource.generate();
        given().when().get("/sync/all").then().statusCode(200);
    }

    @Test
    public void testTrimFutureMonths()
    {
        given().when().get("/sync/all").then().statusCode(200);

        FinancialAssetsDto dto = given().when()
                .get("/financial/assets")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", FinancialAssetsDto.class);

        // every asset's timeline ends at the current month, never in the months still to come
        String currentMonth = (new GregorianCalendar().get(Calendar.MONTH) + 1) + "/" + (new GregorianCalendar().get(Calendar.YEAR) % 100);
        boolean anyHeldNow = false;
        for (FinancialAssetsDto.Group group : dto.getGroups())
        {
            for (FinancialAssetsDto.Group.Account account : group.getAccounts())
            {
                int length = account.getLabels().length;
                if (account.getActive()) {
                    anyHeldNow = true;
                    assertThat(account.getLabels()[length - 1], is(currentMonth));
                }
                assertThat(account.getDeposits().length, is(length));
                assertThat(account.getWithdrawals().length, is(length));
                assertThat(account.getRevaluations().length, is(length));
                assertThat(account.getBalances().length, is(length));
                assertThat(account.getFunding().length, is(length));
                assertThat(account.getCumulativeDeposits().length, is(length));
                assertThat(account.getCumulativeWithdrawals().length, is(length));
            }
        }
        assertThat(anyHeldNow, is(true));
    }

}
