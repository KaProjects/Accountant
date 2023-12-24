package org.kaleta;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.kaleta.dto.AccountingDto;
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
public class AccountingResourceTest
{
    @Test
    public void getTransactionsTest()
    {
        List<YearTransactionDto> transactions =
                given().when()
                        .get("/accounting/2023/transaction/000/month/5")
                        .then()
                        .statusCode(200)
                        .header("Content-Type", containsString(MediaType.APPLICATION_JSON))
                        .body("size()", is(4))
                        .body("[0].date", is("0505"))
                        .body("[1].date", is("1005"))
                        .body("[2].date", is("1505"))
                        .body("[3].date", is("2005"))
                        .extract().response().jsonPath().getList("", YearTransactionDto.class);

        assertThat(transactions, hasItem(YearTransactionDto.from("2005", "500", "account000", "account003", "month5 000")));
        assertThat(transactions, hasItem(YearTransactionDto.from("0505", "600", "account000", "account001", "month5 000")));
        assertThat(transactions, hasItem(YearTransactionDto.from("1505", "-700", "account001", "account000", "month5 000 -amount")));
        assertThat(transactions, hasItem(YearTransactionDto.from("1005", "800", "account000.2", "account001.2", "month5 000")));
    }

    @Test
    public void getCashFlowTest()
    {
        Response response = given().when()
                .get("/accounting/cashflow/2020")
                .then()
                .statusCode(200)
                .extract().response();

        System.out.println("response time: " + response.time() + "ms");

        AccountingDto dto = response.jsonPath().getObject("", AccountingDto.class);

        assertThat(dto.getColumns().size(), is(15));
        assertThat(dto.getColumns().get(0), is("Cash Flow Statement 2020"));
        assertThat(dto.getRows().size(), is(5));

        assertThat(dto.getRows().get(0).getSchemaId(), is("20"));
        assertThat(dto.getRows().get(0).getType(), is(AccountingDto.Type.CASH_FLOW_GROUP));
        assertThat(dto.getRows().get(0).getInitial(), is(1000));
        assertThat(dto.getRows().get(0).getMonthlyValues(), is(new Integer[]{0, 0, 0, 0, 1000, 200, 0, -600, 0, 0, 0, 0}));
        assertThat(dto.getRows().get(0).getTotal(), is(1600));
        assertThat(dto.getRows().get(0).getAccounts().size(), is(2));
        assertThat(dto.getRows().get(0).getAccounts().get(0).getSchemaId(), is("200"));
        assertThat(dto.getRows().get(0).getAccounts().get(0).getType(), is(AccountingDto.Type.CASH_FLOW_ACCOUNT));
        assertThat(dto.getRows().get(0).getAccounts().get(0).getInitial(), is(1000));
        assertThat(dto.getRows().get(0).getAccounts().get(0).getMonthlyValues(), is(new Integer[]{0, 0, 0, -200, 1000, 200, 0, -600, 0, 0, 0, 0}));
        assertThat(dto.getRows().get(0).getAccounts().get(0).getTotal(), is(1400));
        assertThat(dto.getRows().get(0).getAccounts().get(1).getSchemaId(), is("201"));
        assertThat(dto.getRows().get(0).getAccounts().get(1).getType(), is(AccountingDto.Type.CASH_FLOW_ACCOUNT));
        assertThat(dto.getRows().get(0).getAccounts().get(1).getInitial(), is(0));
        assertThat(dto.getRows().get(0).getAccounts().get(1).getMonthlyValues(), is(new Integer[]{0, 0, 0, 200, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getRows().get(0).getAccounts().get(1).getTotal(), is(200));

        assertThat(dto.getRows().get(3).getSchemaId(), is("22"));
        assertThat(dto.getRows().get(3).getType(), is(AccountingDto.Type.CASH_FLOW_GROUP));
        assertThat(dto.getRows().get(3).getInitial(), is(-3000));
        assertThat(dto.getRows().get(3).getMonthlyValues(), is(new Integer[]{0, 0, 0, 0, -1000, -400, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getRows().get(3).getTotal(), is(-4400));
        assertThat(dto.getRows().get(3).getAccounts().size(), is(1));

        assertThat(dto.getRows().get(4).getType(), is(AccountingDto.Type.CASH_FLOW_SUMMARY));
        assertThat(dto.getRows().get(4).getInitial(), is(4000));
        assertThat(dto.getRows().get(4).getMonthlyValues(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getRows().get(4).getTotal(), is(4000));
        assertThat(dto.getRows().get(4).getAccounts().size(), is(0));
    }

    @Test
    public void getYearlyCashFlowTest()
    {
        Response response = given().when()
                .get("/accounting/cashflow")
                .then()
                .statusCode(200)
                .extract().response();

        System.out.println("response time: " + response.time() + "ms");

        AccountingDto dto = response.jsonPath().getObject("", AccountingDto.class);

        assertThat(dto.getColumns().size(), is(5));
        assertThat(dto.getColumns().get(0), is("Yearly Cash Flow Statement"));
        assertThat(dto.getColumns().get(1), is("2017"));
        assertThat(dto.getColumns().get(2), is("2018"));
        assertThat(dto.getColumns().get(3), is("2019"));
        assertThat(dto.getColumns().get(4), is("2020"));
        assertThat(dto.getRows().size(), is(5));

        assertThat(dto.getRows().get(0).getSchemaId(), is("20"));
        assertThat(dto.getRows().get(0).getType(), is(AccountingDto.Type.CASH_FLOW_GROUP));
        assertThat(dto.getRows().get(0).getYearlyValues(), is(new Integer[]{1000, 1100, 1500, 1600}));

        assertThat(dto.getRows().get(1).getSchemaId(), is("21"));
        assertThat(dto.getRows().get(1).getType(), is(AccountingDto.Type.CASH_FLOW_GROUP));
        assertThat(dto.getRows().get(1).getYearlyValues(), is(new Integer[]{2000, 2200, 2500, 2800}));

        assertThat(dto.getRows().get(2).getSchemaId(), is("23"));
        assertThat(dto.getRows().get(2).getType(), is(AccountingDto.Type.CASH_FLOW_GROUP));
        assertThat(dto.getRows().get(2).getYearlyValues(), is(new Integer[]{4000, 4400, 4500, 4000}));

        assertThat(dto.getRows().get(3).getSchemaId(), is("22"));
        assertThat(dto.getRows().get(3).getType(), is(AccountingDto.Type.CASH_FLOW_GROUP));
        assertThat(dto.getRows().get(3).getYearlyValues(), is(new Integer[]{-3000, -3300, -3500, -4400}));

        assertThat(dto.getRows().get(4).getSchemaId(), is("cf"));
        assertThat(dto.getRows().get(4).getType(), is(AccountingDto.Type.CASH_FLOW_SUMMARY));
        assertThat(dto.getRows().get(4).getYearlyValues(), is(new Integer[]{4000, 4400, 5000, 4000}));
    }

    @Test
    public void getProfitTest()
    {
        Response response = given().when()
                .get("/accounting/profit/2019")
                .then()
                .statusCode(200)
                .extract().response();

        System.out.println("response time: " + response.time() + "ms");

        AccountingDto dto = response.jsonPath().getObject("", AccountingDto.class);

        assertThat(dto.getColumns().size(), is(14));
        assertThat(dto.getColumns().get(0), is("Income Statement 2019"));
        assertThat(dto.getRows().size(), is(16));

        assertThat(dto.getRows().get(0).getSchemaId(), is("60"));
        assertThat(dto.getRows().get(0).getType(), is(AccountingDto.Type.INCOME_GROUP));
        assertThat(dto.getRows().get(0).getMonthlyValues(), is(new Integer[]{0, 800, 0, 0, 7000, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getRows().get(0).getTotal(), is(7800));
        assertThat(dto.getRows().get(0).getAccounts().size(), is(1));

        assertThat(dto.getRows().get(1).getSchemaId(), is("55"));
        assertThat(dto.getRows().get(1).getType(), is(AccountingDto.Type.EXPENSE_GROUP));
        assertThat(dto.getRows().get(1).getMonthlyValues(), is(new Integer[]{0, 800, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getRows().get(1).getTotal(), is(800));
        assertThat(dto.getRows().get(1).getAccounts().size(), is(3));
        assertThat(dto.getRows().get(1).getAccounts().get(0).getSchemaId(), is("550"));
        assertThat(dto.getRows().get(1).getAccounts().get(0).getType(), is(AccountingDto.Type.EXPENSE_ACCOUNT));
        assertThat(dto.getRows().get(1).getAccounts().get(0).getMonthlyValues(), is(new Integer[]{0, 800, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getRows().get(1).getAccounts().get(0).getTotal(), is(800));
        assertThat(dto.getRows().get(1).getAccounts().get(1).getSchemaId(), is("551"));
        assertThat(dto.getRows().get(1).getAccounts().get(1).getType(), is(AccountingDto.Type.EXPENSE_ACCOUNT));
        assertThat(dto.getRows().get(1).getAccounts().get(1).getMonthlyValues(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getRows().get(1).getAccounts().get(1).getTotal(), is(0));
        assertThat(dto.getRows().get(1).getAccounts().get(2).getSchemaId(), is("552"));
        assertThat(dto.getRows().get(1).getAccounts().get(2).getType(), is(AccountingDto.Type.EXPENSE_ACCOUNT));
        assertThat(dto.getRows().get(1).getAccounts().get(2).getMonthlyValues(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getRows().get(1).getAccounts().get(2).getTotal(), is(0));

        assertThat(dto.getRows().get(2).getSchemaId(), is("63"));
        assertThat(dto.getRows().get(2).getType(), is(AccountingDto.Type.INCOME_GROUP));
        assertThat(dto.getRows().get(2).getMonthlyValues(), is(new Integer[]{0, 0, 900, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getRows().get(2).getTotal(), is(900));
        assertThat(dto.getRows().get(2).getAccounts().size(), is(3));

        assertThat(dto.getRows().get(3).getSchemaId(), is("ni"));
        assertThat(dto.getRows().get(3).getType(), is(AccountingDto.Type.PROFIT_SUMMARY));
        assertThat(dto.getRows().get(3).getMonthlyValues(), is(new Integer[]{0, 0, 900, 0, 7000, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getRows().get(3).getTotal(), is(7900));
        assertThat(dto.getRows().get(3).getAccounts().size(), is(0));

        assertThat(dto.getRows().get(5).getSchemaId(), is("52"));
        assertThat(dto.getRows().get(5).getType(), is(AccountingDto.Type.EXPENSE_GROUP));
        assertThat(dto.getRows().get(5).getMonthlyValues(), is(new Integer[]{0, 0, 0, 0, 7000, 0, 100, 100, 0, 0, 0, 0}));
        assertThat(dto.getRows().get(5).getTotal(), is(7200));
        assertThat(dto.getRows().get(5).getAccounts().size(), is(1));

        assertThat(dto.getRows().get(7).getSchemaId(), is("op"));
        assertThat(dto.getRows().get(7).getType(), is(AccountingDto.Type.PROFIT_SUMMARY));
        assertThat(dto.getRows().get(7).getMonthlyValues(), is(new Integer[]{0, 0, 900, 0, 0, 0, -100, -100, 0, 0, 0, 0}));
        assertThat(dto.getRows().get(7).getTotal(), is(700));
        assertThat(dto.getRows().get(7).getAccounts().size(), is(0));

        assertThat(dto.getRows().get(13).getSchemaId(), is("63"));
        assertThat(dto.getRows().get(13).getType(), is(AccountingDto.Type.INCOME_GROUP));
        assertThat(dto.getRows().get(13).getMonthlyValues(), is(new Integer[]{0, 0, 0, 0, 0, 0, 300, 300, 0, 0, 0, 0}));
        assertThat(dto.getRows().get(13).getTotal(), is(600));
        assertThat(dto.getRows().get(13).getAccounts().size(), is(1));

        assertThat(dto.getRows().get(14).getSchemaId(), is("55"));
        assertThat(dto.getRows().get(14).getType(), is(AccountingDto.Type.EXPENSE_GROUP));
        assertThat(dto.getRows().get(14).getMonthlyValues(), is(new Integer[]{0, 0, 900, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getRows().get(14).getTotal(), is(900));
        assertThat(dto.getRows().get(14).getAccounts().size(), is(3));
        assertThat(dto.getRows().get(14).getAccounts().get(0).getSchemaId(), is("553"));
        assertThat(dto.getRows().get(14).getAccounts().get(0).getType(), is(AccountingDto.Type.EXPENSE_ACCOUNT));
        assertThat(dto.getRows().get(14).getAccounts().get(0).getMonthlyValues(), is(new Integer[]{0, 0, 900, 0, 0, 0, 0, 0, 50, 0, 0, 0}));
        assertThat(dto.getRows().get(14).getAccounts().get(0).getTotal(), is(950));
        assertThat(dto.getRows().get(14).getAccounts().get(1).getSchemaId(), is("554"));
        assertThat(dto.getRows().get(14).getAccounts().get(1).getType(), is(AccountingDto.Type.EXPENSE_ACCOUNT));
        assertThat(dto.getRows().get(14).getAccounts().get(1).getMonthlyValues(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, -50, 0, 0, 0}));
        assertThat(dto.getRows().get(14).getAccounts().get(1).getTotal(), is(-50));
        assertThat(dto.getRows().get(14).getAccounts().get(2).getSchemaId(), is("555"));
        assertThat(dto.getRows().get(14).getAccounts().get(2).getType(), is(AccountingDto.Type.EXPENSE_ACCOUNT));
        assertThat(dto.getRows().get(14).getAccounts().get(2).getMonthlyValues(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getRows().get(14).getAccounts().get(2).getTotal(), is(0));

        assertThat(dto.getRows().get(15).getSchemaId(), is("np"));
        assertThat(dto.getRows().get(15).getType(), is(AccountingDto.Type.PROFIT_SUMMARY));
        assertThat(dto.getRows().get(15).getMonthlyValues(), is(new Integer[]{0, 0, 0, 0, 0, 0, 200, 200, 0, 0, 0, 0}));
        assertThat(dto.getRows().get(15).getTotal(), is(400));
        assertThat(dto.getRows().get(15).getAccounts().size(), is(0));
    }

    @Test
    public void getYearlyProfitTest()
    {
        Response response = given().when()
                .get("/accounting/profit")
                .then()
                .statusCode(200)
                .extract().response();

        System.out.println("response time: " + response.time() + "ms");

        AccountingDto dto = response.jsonPath().getObject("", AccountingDto.class);

        assertThat(dto.getColumns().size(), is(6));
        assertThat(dto.getColumns().get(0), is("Yearly Income Statement"));
        assertThat(dto.getColumns().get(1), is("2017"));
        assertThat(dto.getColumns().get(2), is("2018"));
        assertThat(dto.getColumns().get(3), is("2019"));
        assertThat(dto.getColumns().get(4), is("2020"));
        assertThat(dto.getColumns().get(5), is("Total"));
        assertThat(dto.getRows().size(), is(16));

        assertThat(dto.getRows().get(0).getSchemaId(), is("60"));
        assertThat(dto.getRows().get(0).getType(), is(AccountingDto.Type.INCOME_GROUP));
        assertThat(dto.getRows().get(0).getYearlyValues(), is(new Integer[]{1000, 100, 10000, 1000}));
        assertThat(dto.getRows().get(0).getTotal(), is(12100));

        assertThat(dto.getRows().get(1).getSchemaId(), is("55"));
        assertThat(dto.getRows().get(1).getType(), is(AccountingDto.Type.EXPENSE_GROUP));
        assertThat(dto.getRows().get(1).getYearlyValues(), is(new Integer[]{2000, 200, 10000, 1000}));
        assertThat(dto.getRows().get(1).getTotal(), is(13200));

        assertThat(dto.getRows().get(2).getSchemaId(), is("63"));
        assertThat(dto.getRows().get(2).getType(), is(AccountingDto.Type.INCOME_GROUP));
        assertThat(dto.getRows().get(2).getYearlyValues(), is(new Integer[]{3000, 300, 30, 0}));
        assertThat(dto.getRows().get(2).getTotal(), is(3330));

        assertThat(dto.getRows().get(3).getSchemaId(), is("ni"));
        assertThat(dto.getRows().get(3).getType(), is(AccountingDto.Type.PROFIT_SUMMARY));
        assertThat(dto.getRows().get(3).getYearlyValues(), is(new Integer[]{2000, 200, 30, 0}));
        assertThat(dto.getRows().get(3).getTotal(), is(2230));

        assertThat(dto.getRows().get(4).getSchemaId(), is("51"));
        assertThat(dto.getRows().get(4).getType(), is(AccountingDto.Type.EXPENSE_GROUP));
        assertThat(dto.getRows().get(4).getYearlyValues(), is(new Integer[]{4000, 400, 40, 1000}));
        assertThat(dto.getRows().get(4).getTotal(), is(5440));

        assertThat(dto.getRows().get(5).getSchemaId(), is("52"));
        assertThat(dto.getRows().get(5).getType(), is(AccountingDto.Type.EXPENSE_GROUP));
        assertThat(dto.getRows().get(5).getYearlyValues(), is(new Integer[]{5000, 500, 50, -1000}));
        assertThat(dto.getRows().get(5).getTotal(), is(4550));

        assertThat(dto.getRows().get(6).getSchemaId(), is("53"));
        assertThat(dto.getRows().get(6).getType(), is(AccountingDto.Type.EXPENSE_GROUP));
        assertThat(dto.getRows().get(6).getYearlyValues(), is(new Integer[]{6000, 600, 60, 0}));
        assertThat(dto.getRows().get(6).getTotal(), is(6660));

        assertThat(dto.getRows().get(7).getSchemaId(), is("op"));
        assertThat(dto.getRows().get(7).getType(), is(AccountingDto.Type.PROFIT_SUMMARY));
        assertThat(dto.getRows().get(7).getYearlyValues(), is(new Integer[]{-13000, -1300, -120, 0}));
        assertThat(dto.getRows().get(7).getTotal(), is(-14420));

        assertThat(dto.getRows().get(10).getSchemaId(), is("56"));
        assertThat(dto.getRows().get(10).getType(), is(AccountingDto.Type.EXPENSE_GROUP));
        assertThat(dto.getRows().get(10).getYearlyValues(), is(new Integer[]{0, 0, 0, 0}));
        assertThat(dto.getRows().get(10).getTotal(), is(0));

        assertThat(dto.getRows().get(13).getSchemaId(), is("63"));
        assertThat(dto.getRows().get(13).getType(), is(AccountingDto.Type.INCOME_GROUP));
        assertThat(dto.getRows().get(13).getYearlyValues(), is(new Integer[]{7000, 700, 70, 1000}));
        assertThat(dto.getRows().get(13).getTotal(), is(8770));

        assertThat(dto.getRows().get(14).getSchemaId(), is("55"));
        assertThat(dto.getRows().get(14).getType(), is(AccountingDto.Type.EXPENSE_GROUP));
        assertThat(dto.getRows().get(14).getYearlyValues(), is(new Integer[]{8000, 800, 80, 0}));
        assertThat(dto.getRows().get(14).getTotal(), is(8880));

        assertThat(dto.getRows().get(15).getSchemaId(), is("np"));
        assertThat(dto.getRows().get(15).getType(), is(AccountingDto.Type.PROFIT_SUMMARY));
        assertThat(dto.getRows().get(15).getYearlyValues(), is(new Integer[]{-14000, -1400, -130, 1000}));
        assertThat(dto.getRows().get(15).getTotal(), is(-14530));
    }

    @Test
    public void parameterValidatorTest()
    {
        String validYear = "2023";
        String validAccountId = "000";
        String validMonth = "5";

        assertThat(given().when()
                .get("/accounting/" + "1999" + "/transaction/" + validAccountId + "/month/" + validMonth)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));


        assertThat(given().when()
                .get("/accounting/" + "20" + "/transaction/" + validAccountId + "/month/" + validMonth)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));

        assertThat(given().when()
                .get("/accounting/" + "xxxx" + "/transaction/" + validAccountId + "/month/" + validMonth)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));

        assertThat(given().when()
                .get("/accounting/" + validYear + "/transaction/" + "22" + "/month/" + validMonth)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Schema Account ID Parameter"));

        assertThat(given().when()
                .get("/accounting/" + validYear + "/transaction/" + "2222" + "/month/" + validMonth)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Schema Account ID Parameter"));

        assertThat(given().when()
                .get("/accounting/" + validYear + "/transaction/" + "xxx" + "/month/" + validMonth)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Schema Account ID Parameter"));

        assertThat(given().when()
                .get("/accounting/" + validYear + "/transaction/" + validAccountId + "/month/" + "0")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Month Parameter"));

        assertThat(given().when()
                .get("/accounting/" + validYear + "/transaction/" + validAccountId + "/month/" + "13")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Month Parameter"));

        assertThat(given().when()
                .get("/accounting/" + validYear + "/transaction/" + validAccountId + "/month/" + "x")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Month Parameter"));

        assertThat(given().when()
                .get("/accounting/cashflow/" + "1999")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));


        assertThat(given().when()
                .get("/accounting/cashflow/" + "20")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));

        assertThat(given().when()
                .get("/accounting/cashflow/" + "xxxx")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));

        assertThat(given().when()
                .get("/accounting/profit/" + "1999")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));

        assertThat(given().when()
                .get("/accounting/profit/" + "20")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));

        assertThat(given().when()
                .get("/accounting/profit/" + "xxxx")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));
    }
}
