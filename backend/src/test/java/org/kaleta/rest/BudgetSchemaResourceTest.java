package org.kaleta.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.kaleta.dto.BudgetDto;
import org.kaleta.dto.BudgetSchemaDto;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

/**
 * The stored rows of a year's budget, as the budget editor reads and replaces them. The rows are
 * written into 2018, where the fixtures have no budget, and taken out again after each test.
 */
@QuarkusTest
public class BudgetSchemaResourceTest
{
    private static final String YEAR = "2018";

    @AfterEach
    void removeTheWrittenRows()
    {
        save(List.of()).then().statusCode(204);
    }

    private static io.restassured.response.Response save(List<Map<String, String>> rows)
    {
        return given().contentType(ContentType.JSON).body(rows).when().put("/budget/" + YEAR + "/schema");
    }

    private static List<BudgetSchemaDto> stored(String year)
    {
        return given().when().get("/budget/" + year + "/schema").then().statusCode(200)
                .extract().response().jsonPath().getList("", BudgetSchemaDto.class);
    }

    @Test
    void answersTheStoredRowsOfTheYear_inTheOrderOfTheirIds()
    {
        List<String> ids = stored("2019").stream().map(BudgetSchemaDto::getId).toList();

        assertThat(ids.size(), is(15));
        assertThat(ids.subList(0, 3), is(List.of("e1", "e1.1", "e1.2")));
    }

    private static BudgetSchemaDto row(String year, String id)
    {
        return stored(year).stream().filter(row -> row.getId().equals(id)).findFirst().orElseThrow();
    }

    @Test
    void marksTheRowsEveryYearHas_asFixed()
    {
        // the fixtures' 2019 has the row of the vacations, labelled vac=
        assertThat(row("2019", "of1").isFixed(), is(true));
        assertThat(row("2019", "of2").isFixed(), is(false));
        assertThat(row("2019", "i1").isFixed(), is(false));
    }

    @Test
    void givesAYearTheFixedRowsItLacks_asTheLatestEarlierYearHasThem_withNothingPlanned()
    {
        BudgetSchemaDto vacations = row("2020", "of1");
        assertThat(vacations.getName(), is("of1"));
        assertThat(vacations.getDebit(), is("202%"));
        assertThat(vacations.getCredit(), is("201%"));
        assertThat(vacations.getDescription(), is("vac="));
        assertThat(vacations.getPlanning(), is("all=0"));
        assertThat(vacations.isFixed(), is(true));

        // 2021 has the row of every financial asset as of1, which 2022 gets too - as of2, since the
        // vacations, from 2019, come first and take of1
        assertThat(row("2022", "of1").getDescription(), is("vac="));
        BudgetSchemaDto investments = row("2022", "of2");
        assertThat(investments.getDebit(), is("549%"));
        assertThat(investments.getCredit(), is("23%"));
        assertThat(investments.getDescription(), is((String) null));
        assertThat(investments.getPlanning(), is("all=0"));
        assertThat(investments.isFixed(), is(true));
    }

    @Test
    void givesNoFixedRowsToAYearBeforeTheFirstThatHasThem()
    {
        assertThat(stored(YEAR), is(List.of()));
    }

    @Test
    void readsTheBudgetOfAYearWithTheFixedRowsItLacks()
    {
        BudgetDto budget = given().when().get("/budget/2022").then().statusCode(200)
                .extract().response().jsonPath().getObject("", BudgetDto.class);

        BudgetDto.Row investments = budget.getRows().stream().filter(row -> row.getId().equals("of2")).findFirst().orElseThrow();
        assertThat(investments.getSubRows().stream().map(BudgetDto.Row::getId).toList(),
                is(List.of("of2.230.0", "of2.230.1", "of2.231.0")));
        assertThat(investments.getPlannedSum(), is(0));
    }

    @Test
    void ordersTheRowsByTheirIds_numberByNumber()
    {
        save(List.of(
                Map.of("id", "e1.10", "name", "c", "debit", "510%", "credit", "510%"),
                Map.of("id", "e10", "name", "d"),
                Map.of("id", "e1", "name", "a"),
                Map.of("id", "e1.2", "name", "b", "debit", "510%", "credit", "510%")))
                .then().statusCode(204);

        assertThat(stored(YEAR).stream().map(BudgetSchemaDto::getId).toList(), is(List.of("e1", "e1.2", "e1.10", "e10")));
    }

    @Test
    void replacesTheRowsOfTheYear_withTheOnesGiven()
    {
        save(List.of(
                Map.of("id", "i1", "name", "salary", "planning", "all=100"),
                Map.of("id", "i1.1", "name", "employer", "debit", "200.0", "credit", "5%", "planning", " "),
                Map.of("id", "of1", "name", "assets", "debit", "510%", "credit", "20%", "description", "!dane")))
                .then().statusCode(204);

        List<BudgetSchemaDto> rows = stored(YEAR);
        assertThat(rows.stream().map(BudgetSchemaDto::getId).toList(), is(List.of("i1", "i1.1", "of1")));
        assertThat(rows.get(1).getDebit(), is("200.0"));
        // a blank value is stored as none
        assertThat(rows.get(1).getPlanning(), is((String) null));

        BudgetDto budget = given().when().get("/budget/" + YEAR).then().statusCode(200)
                .extract().response().jsonPath().getObject("", BudgetDto.class);
        assertThat(budget.getRows().get(0).getName(), is("salary"));
        assertThat(budget.getRows().get(0).getPlannedSum(), is(1200));

        save(List.of(Map.of("id", "e1", "name", "other"))).then().statusCode(204);
        assertThat(stored(YEAR).stream().map(BudgetSchemaDto::getId).toList(), is(List.of("e1")));
    }

    @Test
    void refusesRowsThatDoNotMakeABudget_andKeepsTheStoredOnes()
    {
        save(List.of(Map.of("id", "i1", "name", "salary"))).then().statusCode(204);

        refused(List.of(Map.of("id", "x1", "name", "a")), "no valid id");
        refused(List.of(Map.of("id", "i1", "name", "a"), Map.of("id", "i1", "name", "b")), "more than once");
        refused(List.of(Map.of("id", "e1.1", "name", "a", "debit", "510%", "credit", "510%")), "which is not there");
        refused(List.of(Map.of("id", "e1", "name", "a", "debit", "510%")), "both its debit and its credit");
        refused(List.of(Map.of("id", "e1", "name", "a", "planning", "1|2|3")), "planning");
        refused(List.of(Map.of("id", "e1", "name", "a"), Map.of("id", "e1.1", "name", "b", "planning", "all=1")), "only a row has a planning");
        refused(List.of(Map.of("id", "e1", "name", "a", "debit", "510%", "credit", "510%"),
                Map.of("id", "e1.1", "name", "b", "debit", "520%", "credit", "520%")), "made of theirs");

        assertThat(stored(YEAR).stream().map(BudgetSchemaDto::getId).toList(), is(List.of("i1")));
    }

    @Test
    void refusesADebitOrCreditThatIsNoAccountOfTheYear()
    {
        save(List.of(Map.of("id", "e1", "name", "a", "debit", "510.0", "credit", "200.0"))).then().statusCode(204);
        save(List.of(Map.of("id", "e1", "name", "a", "debit", "51%", "credit", "200.%"))).then().statusCode(204);

        refused(List.of(Map.of("id", "e1", "name", "a", "debit", "510.1", "credit", "200.0")), "is no account of the year");
        refused(List.of(Map.of("id", "e1", "name", "a", "debit", "510.0", "credit", "53%")), "begins no account of the year");
        refused(List.of(Map.of("id", "e1", "name", "a", "debit", "510", "credit", "200.0")), "neither an account");
        refused(List.of(Map.of("id", "e1", "name", "a", "debit", "510.0-", "credit", "200.0")), "neither an account");
        refused(List.of(Map.of("id", "e1", "name", "a", "debit", "51.0", "credit", "200.0")), "neither an account");
    }

    @Test
    void refusesARowWithoutAName()
    {
        save(List.of(Map.of("id", "i1", "name", " "))).then().statusCode(400);
    }

    private static void refused(List<Map<String, String>> rows, String why)
    {
        save(rows).then().statusCode(422).body("detail", containsString(why));
    }
}
