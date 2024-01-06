package org.kaleta.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.kaleta.dto.YearAccountTransactionDto;
import org.kaleta.dto.YearTransactionDto;
import org.springframework.http.HttpStatus;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class TransactionResourceTest
{

    @Test
    public void getTransactionsMatchingTest()
    {
        List<YearTransactionDto> transactions = given().when()
                .get("/transaction/2020/2/7")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(4))
                .extract().response().jsonPath().getList("", YearTransactionDto.class);

        assertThat(transactions, hasItem(YearTransactionDto.from("0101", "1000", "200.0", "700.0", "init")));
        assertThat(transactions, hasItem(YearTransactionDto.from("0101", "0", "201.0", "700.0", "init")));
        assertThat(transactions, hasItem(YearTransactionDto.from("0101", "2000", "210.0", "700.0", "init")));
        assertThat(transactions, hasItem(YearTransactionDto.from("0101", "4000", "230.0", "700.0", "init")));
    }

    @Test
    public void getTransactionsTest()
    {
        List<YearTransactionDto> transactions = given().when()
                .get("/transaction/2020")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(33))
                .extract().response().jsonPath().getList("", YearTransactionDto.class);

        assertThat(transactions, hasItem(YearTransactionDto.from("0101", "1000", "200.0", "700.0", "init")));
        assertThat(transactions, hasItem(YearTransactionDto.from("0505", "100", "200.0", "220.0", "x")));
        assertThat(transactions, hasItem(YearTransactionDto.from("1508", "100", "210.0", "200.0", "x")));
        assertThat(transactions, hasItem(YearTransactionDto.from("1209", "1000", "510.0", "520.0", "")));
    }

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
        assertThat(given().when()
                .get("/transaction/2x20")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));

        assertThat(given().when()
                .get("/transaction/2014")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString(), containsString(" not found"));

        assertThat(given().when()
                .get("/transaction/" + (new GregorianCalendar().get(Calendar.YEAR) + 1))
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString(), containsString(" not found"));

        String validSchemaId = "222";

        assertThat(given().when()
                .get("/transaction/2x20/" + validSchemaId + "/" + validSchemaId)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));

        assertThat(given().when()
                .get("/transaction/2014/" + validSchemaId + "/" + validSchemaId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString(), containsString(" not found"));

        assertThat(given().when()
                .get("/transaction/" + (new GregorianCalendar().get(Calendar.YEAR) + 1) + "/" + validSchemaId + "/" + validSchemaId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString(), containsString(" not found"));

        String validYear = "2020";

        assertThat(given().when()
                .get("/transaction/"+ validYear + "/2222/" + validSchemaId)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Debit Prefix Parameter"));

        assertThat(given().when()
                .get("/transaction/"+ validYear + "/22x/" + validSchemaId)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Debit Prefix Parameter"));

        assertThat(given().when()
                .get("/transaction/" + validYear + "/" + validSchemaId + "/2222")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Credit Prefix Parameter"));

        assertThat(given().when()
                .get("/transaction/" + validYear + "/" + validSchemaId + "/22x")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Credit Prefix Parameter"));

        String validAccountId = "200.11";

        assertThat(given().when()
                .get("/transaction/2x20/" + validAccountId)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));

        assertThat(given().when()
                .get("/transaction/2014/" + validAccountId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString(), containsString(" not found"));

        assertThat(given().when()
                .get("/transaction/" + (new GregorianCalendar().get(Calendar.YEAR) + 1) + "/" + validAccountId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString(), containsString(" not found"));

        assertThat(given().when()
                .get("/transaction/" + validYear + "/200")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Account ID Parameter"));

        assertThat(given().when()
                .get("/transaction/" + validYear + "/200.")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Account ID Parameter"));

        assertThat(given().when()
                .get("/transaction/" + validYear + "/200.x")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Account ID Parameter"));

        assertThat(given().when()
                .get("/transaction/" + validYear + "/20x.10")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Account ID Parameter"));

        assertThat(given().when()
                .get("/transaction/" + validYear + "/20.10")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Account ID Parameter"));

        assertThat(given().when()
                .get("/transaction/" + validYear + "/2022.10")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Account ID Parameter"));

        assertThat(given().when()
                .get("/transaction/" + validYear + "/202.10-")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Account ID Parameter"));

        assertThat(given().when()
                .get("/transaction/" + validYear + "/202.10-x")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Account ID Parameter"));

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
