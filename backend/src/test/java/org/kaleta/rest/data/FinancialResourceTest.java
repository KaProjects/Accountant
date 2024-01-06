package org.kaleta.rest.data;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.kaleta.dto.FinancialAssetsDto;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class FinancialResourceTest
{
    @Test
    @EnabledIf(value = "isDataLocationSet", disabledReason = "set data.location in test/resources/application.properties")
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

    boolean isDataLocationSet()
    {
        return !ConfigProvider.getConfig().getValue("data.location", String.class).equals("X");
    }
}
