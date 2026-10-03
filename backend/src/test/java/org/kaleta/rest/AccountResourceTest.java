package org.kaleta.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.kaleta.dto.YearAccountDto;
import org.kaleta.dto.YearAccountOverviewDto;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.kaleta.framework.Problems.assertInvalid;
import static org.kaleta.framework.Problems.assertNotFound;

@QuarkusTest
public class AccountResourceTest
{
    @Test
    public void getAllAccountsTest()
    {
        List<YearAccountDto> accounts = given().when()
                .get("/account/2019")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
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
    void getAccountsOverviewTest()
    {
        List<YearAccountOverviewDto> accounts = given().when()
                .get("/account/2019/210")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(3))
                .extract().response().jsonPath().getList("", YearAccountOverviewDto.class);

        assertThat(accounts, hasItem(YearAccountOverviewDto.from("210.0", "general", 0, 6000, 6000)));
        assertThat(accounts, hasItem(YearAccountOverviewDto.from("210.1", "generaly", 0, 0, -5000)));
        assertThat(accounts, hasItem(YearAccountOverviewDto.from("210.2", "generalz", 0, 5000, -1000)));
    }

    @Test
    void offBalanceSchemaIsNotAnError()
    {
        // 700 is an off-balance (type X) account, which has no debit or credit
        // orientation. Picking it in the accounting data view used to raise a 500.
        given().when()
                .get("/account/2020/700")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    void offBalanceAccountsAreListedWithoutATurnoverOrBalance()
    {
        // asserted on the wire rather than through the DTO, whose fields default to
        // zero and would mask the absence
        List<Map<String, Object>> accounts = given().when()
                .get("/account/2020/700")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getList("");

        assertThat(accounts.size(), is(1));
        assertThat(accounts.get(0).get("id"), is("700.0"));
        assertThat(accounts.get(0).get("name"), is("general"));
        // neither figure is meaningful for an off-balance account, so neither is sent
        assertThat(accounts.get(0).containsKey("turnover"), is(false));
        assertThat(accounts.get(0).containsKey("balance"), is(false));
    }

    @Test
    public void parameterValidatorTest()
    {
        assertInvalid("/account/2x20", "year");

        assertNotFound("/account/2014");

        assertNotFound("/account/" + (new GregorianCalendar().get(Calendar.YEAR) + 1));

        String validSchemaId = "123";

        assertInvalid("/account/2x20/" + validSchemaId, "year");

        assertNotFound("/account/2014/" + validSchemaId);

        assertNotFound("/account/" + (new GregorianCalendar().get(Calendar.YEAR) + 1) + "/" + validSchemaId);

        String validYear = "2019";

        assertInvalid("/account/" + validYear + "/" + "22", "schemaId");

        assertInvalid("/account/" + validYear + "/" + "2222", "schemaId");

        assertInvalid("/account/" + validYear + "/" + "xxx", "schemaId");
    }
}
