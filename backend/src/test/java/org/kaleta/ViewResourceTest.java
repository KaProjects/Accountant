package org.kaleta;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.kaleta.dto.VacationDto;
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
    public void getVacationTest()
    {
        VacationDto dto =
                given().when()
                        .get("/view/vacation/2018")
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
    public void parameterValidatorTest()
    {
        assertThat(given().when()
                .get("/view/vacation/2x20")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));

        assertThat(given().when()
                .get("/view/vacation/2014")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString(), containsString(" not found"));

        assertThat(given().when()
                .get("/view/vacation/" + (new GregorianCalendar().get(Calendar.YEAR) + 1))
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString(), containsString(" not found"));
    }
}
