package org.kaleta;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.kaleta.dto.YearTransactionDto;
import org.springframework.http.HttpStatus;

import javax.ws.rs.core.MediaType;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class BudgetResourceTest
{
    @Test
    public void getTransactionsTest()
    {
        String year = "2019";
        String budgetId = "i2";
        String month = "9";

        List<YearTransactionDto> transactions =
                given().when()
                        .get("/budget/" + year + "/transaction/" + budgetId + "/month/" + month)
                        .then()
                        .statusCode(200)
                        .header("Content-Type", containsString(MediaType.APPLICATION_JSON))
                        .body("size()", is(5))
                        .body("[0].date", is("0909"))
                        .body("[1].date", is("1009"))
                        .body("[2].date", is("1509"))
                        .body("[3].date", is("2009"))
                        .body("[4].date", is("2109"))
                        .extract().response().jsonPath().getList("", YearTransactionDto.class);

        assertThat(transactions, hasItem(YearTransactionDto.from("1509", "10", "account553", "account554", "same group")));
        assertThat(transactions, hasItem(YearTransactionDto.from("1009", "10", "account553", "account554", "same group")));
        assertThat(transactions, hasItem(YearTransactionDto.from("0909", "10", "account553", "account554", "same group")));
        assertThat(transactions, hasItem(YearTransactionDto.from("2009", "10", "account553", "account554", "same group")));
        assertThat(transactions, hasItem(YearTransactionDto.from("2109", "10", "account553", "account554", "same group")));
    }

    @Test
    public void parameterValidatorTest()
    {
        String validYear = "2019";
        String validBudgetId = "i2";
        String validMonth = "9";

        assertThat(given().when()
                .get("/budget/" + "1999" + "/transaction/" + validBudgetId + "/month/" + validMonth)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));


        assertThat(given().when()
                .get("/budget/" + "20" + "/transaction/" + validBudgetId + "/month/" + validMonth)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));

        assertThat(given().when()
                .get("/budget/" + "xxxx" + "/transaction/" + validBudgetId + "/month/" + validMonth)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));

        assertThat(given().when()
                .get("/budget/" + validYear + "/transaction/" + "x1" + "/month/" + validMonth)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Budget ID Parameter"));

        assertThat(given().when()
                .get("/budget/" + validYear + "/transaction/" + validBudgetId + "/month/" + "0")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Month Parameter"));

        assertThat(given().when()
                .get("/budget/" + validYear + "/transaction/" + validBudgetId + "/month/" + "13")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Month Parameter"));

        assertThat(given().when()
                .get("/budget/" + validYear + "/transaction/" + validBudgetId + "/month/" + "x")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Month Parameter"));

        assertThat(given().when()
                .get("/budget/" + "1999")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));


        assertThat(given().when()
                .get("/budget/" + "20")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));

        assertThat(given().when()
                .get("/budget/" + "xxxx")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));
    }
}
