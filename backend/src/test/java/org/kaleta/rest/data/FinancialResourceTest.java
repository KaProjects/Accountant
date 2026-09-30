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
                .get("/financial/assets/" + new GregorianCalendar().get(Calendar.YEAR))
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", FinancialAssetsDto.class);

        for (FinancialAssetsDto.Group group : dto.getGroups())
        {
            for (FinancialAssetsDto.Group.Account account : group.getAccounts())
            {
                int month = new GregorianCalendar().get(Calendar.MONTH) + 1;
                assertThat(account.getLabels().length, is(month));
                assertThat(account.getDeposits().length, is(month));
                assertThat(account.getWithdrawals().length, is(month));
                assertThat(account.getRevaluations().length, is(month));
                assertThat(account.getBalances().length, is(month));
                assertThat(account.getFunding().length, is(month));
                assertThat(account.getCumulativeDeposits().length, is(month));
                assertThat(account.getCumulativeWithdrawals().length, is(month));
            }
        }
    }

}
