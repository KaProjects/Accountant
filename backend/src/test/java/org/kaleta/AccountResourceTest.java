package org.kaleta;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.kaleta.dto.YearAccountDto;
import org.springframework.http.HttpStatus;

import javax.ws.rs.core.MediaType;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class AccountResourceTest
{
    @Test
    public void getAllAccountsTest()
    {
        List<YearAccountDto> accounts =
                given().when()
                        .get("/account/2019")
                        .then()
                        .statusCode(200)
                        .header("Content-Type", containsString(MediaType.APPLICATION_JSON))
                        .body("size()", is(16))
                        .extract().response().jsonPath().getList("", YearAccountDto.class);

        assertThat(accounts, hasItem(YearAccountDto.from("600", "0", "general", "", "account600", "group60", "class6")));
        assertThat(accounts, hasItem(YearAccountDto.from("600", "1", "second", "aaaa", "account600", "group60", "class6")));
        assertThat(accounts, hasItem(YearAccountDto.from("550", "0", "general of x", "", "account550", "group55", "class5")));
        assertThat(accounts, hasItem(YearAccountDto.from("631", "0", "general", "", "account631", "group63", "class6")));
        assertThat(accounts, hasItem(YearAccountDto.from("520", "0", "general", "", "account520", "group52", "class5")));
        assertThat(accounts, hasItem(YearAccountDto.from("553", "0", "general", "", "account553", "group55", "class5")));
        assertThat(accounts, hasItem(YearAccountDto.from("554", "0", "sda ad", "", "account554", "group55", "class5")));
        assertThat(accounts, hasItem(YearAccountDto.from("630", "0", "general", "", "account630", "group63", "class6")));
        assertThat(accounts, hasItem(YearAccountDto.from("701", "0", "ds asd d", "ssss", "account701", "group70", "class7")));
        assertThat(accounts, hasItem(YearAccountDto.from("202", "10", "general", "", "account202", "group20", "class2")));
        assertThat(accounts, hasItem(YearAccountDto.from("202", "20", "general", "", "account202", "group20", "class2")));
        assertThat(accounts, hasItem(YearAccountDto.from("201", "0", "sads sd as", "", "account201", "group20", "class2")));
        assertThat(accounts, hasItem(YearAccountDto.from("201", "1", "sd s dasda s", "", "account201", "group20", "class2")));
        assertThat(accounts, hasItem(YearAccountDto.from("210", "0", "general", "a", "account210", "group21", "class2")));
        assertThat(accounts, hasItem(YearAccountDto.from("210", "1", "generaly", "b", "account210", "group21", "class2")));
        assertThat(accounts, hasItem(YearAccountDto.from("210", "2", "generalz", "c", "account210", "group21", "class2")));
    }

    @Test
    public void parameterValidatorTest()
    {
        assertThat(given().when()
                .get("/account/1999")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));

        assertThat(given().when()
                .get("/account/20")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));

        assertThat(given().when()
                .get("/account/xxx")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));
    }
}
