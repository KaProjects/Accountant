package org.kaleta.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.kaleta.dto.YearAccountTransactionDto;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.kaleta.framework.Problems.assertInvalid;
import static org.kaleta.framework.Problems.assertNotFound;

@QuarkusTest
public class TransactionResourceTest
{

    @Test
    public void getAccountTransactions()
    {
        List<YearAccountTransactionDto> transactions = given().when()
                .get("/transaction/2020/200.0")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(25))
                .body("[0].date", is("1508"))
                .body("[6].date", is("1006"))
                .extract().response().jsonPath().getList("", YearAccountTransactionDto.class);

        assertThat(transactions, hasItem(YearAccountTransactionDto.from("0101", "1000", null, "700.0 account700", "init")));
        assertThat(transactions, hasItem(YearAccountTransactionDto.from("0505", "100", null, "220.0 account220", "x")));
    }

    @Test
    public void parameterValidatorTest()
    {
        String validYear = "2020";

        String validAccountId = "200.11";

        assertInvalid("/transaction/2x20/" + validAccountId, "year");

        assertNotFound("/transaction/2014/" + validAccountId);

        assertNotFound("/transaction/" + (new GregorianCalendar().get(Calendar.YEAR) + 1) + "/" + validAccountId);

        assertInvalid("/transaction/" + validYear + "/200", "accountId");

        assertInvalid("/transaction/" + validYear + "/200.", "accountId");

        assertInvalid("/transaction/" + validYear + "/200.x", "accountId");

        assertInvalid("/transaction/" + validYear + "/20x.10", "accountId");

        assertInvalid("/transaction/" + validYear + "/20.10", "accountId");

        assertInvalid("/transaction/" + validYear + "/2022.10", "accountId");

        assertInvalid("/transaction/" + validYear + "/202.10-", "accountId");

        assertInvalid("/transaction/" + validYear + "/202.10-x", "accountId");

        given().when()
                .get("/transaction/" + validYear + "/202.10-123")
                .then().statusCode(200);

        given().when()
                .get("/transaction/" + validYear + "/202.103")
                .then().statusCode(200);

        given().when()
                .get("/transaction/" + validYear + "/202.0")
                .then().statusCode(200);
    }
}
