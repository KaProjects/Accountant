package org.kaleta;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.kaleta.dto.VacationDto;
import org.kaleta.dto.ViewDto;
import org.springframework.http.HttpStatus;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class ViewResourceTest
{
    @Test
    public void getVacationsTest()
    {
        VacationDto dto =
                given().when()
                        .get("/view/2018/vacation")
                        .then()
                        .statusCode(200)
                        .extract().response().jsonPath().getObject("", VacationDto.class);

        assertThat(dto.getColumns().size(), is(5));
        assertThat(dto.getColumns().get(2), is("Debit"));

        assertThat(dto.getVacations().size(), is(2));
        assertThat(dto.getVacations().get(0).getName(), is("xxx"));
        assertThat(dto.getVacations().get(0).getExpenses(), is("6000"));
        assertThat(dto.getVacations().get(0).getTransactions().size(), is(3));
        assertThat(dto.getVacations().get(0).getTransactions().get(0).getDate(), is("2001"));
        assertThat(dto.getVacations().get(0).getTransactions().get(1).getDate(), is("0402"));
        assertThat(dto.getVacations().get(0).getTransactions().get(2).getDate(), is("1102"));
        assertThat(dto.getVacations().get(0).getChartData().size(), is(2));
        assertThat(dto.getVacations().get(0).getChartData(), hasItem(new VacationDto.Vacation.ChartData("account500", 4000)));
        assertThat(dto.getVacations().get(0).getChartData(), hasItem(new VacationDto.Vacation.ChartData("account510", 2000)));

        assertThat(dto.getVacations().get(1).getName(), is("yyy"));
        assertThat(dto.getVacations().get(1).getExpenses(), is("5000"));
        assertThat(dto.getVacations().get(1).getTransactions().size(), is(4));
        assertThat(dto.getVacations().get(1).getTransactions().get(0).getAmount(), is("3000"));
        assertThat(dto.getVacations().get(1).getTransactions().get(0).getDebit(), is("account500"));
        assertThat(dto.getVacations().get(1).getTransactions().get(1).getAmount(), is("3000"));
        assertThat(dto.getVacations().get(1).getTransactions().get(1).getDebit(), is("doprava"));
        assertThat(dto.getVacations().get(1).getTransactions().get(2).getAmount(), is("~1000"));
        assertThat(dto.getVacations().get(1).getTransactions().get(3).getAmount(), is("-1000"));
        assertThat(dto.getVacations().get(1).getChartData().size(), is(3));
        assertThat(dto.getVacations().get(1).getChartData(), hasItem(new VacationDto.Vacation.ChartData("account500", 2000)));
        assertThat(dto.getVacations().get(1).getChartData(), hasItem(new VacationDto.Vacation.ChartData("account510", 2000)));
        assertThat(dto.getVacations().get(1).getChartData(), hasItem(new VacationDto.Vacation.ChartData("account520", 1000)));
    }

    @Test
    public void getViewsTest()
    {
        ViewDto dto =
                given().when()
                        .get("/view/2018")
                        .then()
                        .statusCode(200)
                        .extract().response().jsonPath().getObject("", ViewDto.class);

        assertThat(dto.getColumns().size(), is(5));
        assertThat(dto.getColumns().get(2), is("Debit"));

        assertThat(dto.getViews().size(), is(2));
        assertThat(dto.getViews().get(0).getName(), is("zzz"));
        assertThat(dto.getViews().get(0).getTransactions().size(), is(5));
        assertThat(dto.getViews().get(0).getTransactions().get(0).getDate(), is("1102"));
        assertThat(dto.getViews().get(0).getTransactions().get(0).getDebit(), is("doprava"));
        assertThat(dto.getViews().get(0).getTransactions().get(1).getDate(), is("0503"));
        assertThat(dto.getViews().get(0).getTransactions().get(1).getDebit(), is("doprava"));
        assertThat(dto.getViews().get(0).getTransactions().get(2).getDate(), is("0603"));
        assertThat(dto.getViews().get(0).getTransactions().get(2).getCredit(), is("doprava"));
        assertThat(dto.getViews().get(0).getTransactions().get(3).getDate(), is("0704"));
        assertThat(dto.getViews().get(0).getTransactions().get(3).getAmount(), is("100"));
        assertThat(dto.getViews().get(0).getTransactions().get(4).getDate(), is("0905"));
        assertThat(dto.getViews().get(0).getTransactions().get(4).getAmount(), is("100"));
        assertThat(dto.getViews().get(1).getName(), is("ww"));
        assertThat(dto.getViews().get(1).getTransactions().size(), is(1));
        assertThat(dto.getViews().get(1).getTransactions().get(0).getAmount(), is("1"));
    }

    @Test
    public void parameterValidatorTest()
    {
        assertThat(given().when()
                .get("/view/2x20/vacation")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));

        assertThat(given().when()
                .get("/view/2014/vacation")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString(), containsString(" not found"));

        assertThat(given().when()
                .get("/view/" + (new GregorianCalendar().get(Calendar.YEAR) + 1) + "/vacation")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString(), containsString(" not found"));

        assertThat(given().when()
                .get("/view/2x20")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));

        assertThat(given().when()
                .get("/view/2014")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString(), containsString(" not found"));

        assertThat(given().when()
                .get("/view/" + (new GregorianCalendar().get(Calendar.YEAR) + 1))
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString(), containsString(" not found"));
    }
}
