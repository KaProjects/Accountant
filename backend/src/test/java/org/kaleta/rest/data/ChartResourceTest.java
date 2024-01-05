package org.kaleta.rest.data;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.kaleta.dto.AccountingDto;
import org.kaleta.dto.ChartDto;
import org.kaleta.model.ChartData;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.kaleta.Constants.Label.NET_INCOME;
import static org.kaleta.Constants.Label.NET_PROFIT;
import static org.kaleta.Constants.Label.OPERATING_PROFIT;

@QuarkusTest
public class ChartResourceTest
{
    @Test
    @EnabledIf(value = "isDataLocationSet", disabledReason = "set data.location in test/resources/application.properties")
    public void getChartConfigs()
    {
        given().when().get("/sync/all").then().statusCode(200);

        List<ChartData.Config> chartConfig = given().when()
                .get("/chart/config")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getList("", ChartData.Config.class);

        assertThat(chartConfig.get(0).getId(), is("60"));
        assertThat(chartConfig.get(0).getName(), is("Pracovne"));
        assertThat(chartConfig.get(0).getType(), is(ChartData.Config.ChartType.BALANCE));
        assertThat(chartConfig.get(0).getSchemas(), is(Set.of("60")));
        assertThat(chartConfig.get(3).getId(), is("ni"));
        assertThat(chartConfig.get(3).getName(), is(NET_INCOME));
        assertThat(chartConfig.get(3).getType(), is(ChartData.Config.ChartType.BALANCE));
        assertThat(chartConfig.get(3).getSchemas(), is(Set.of("60", "550", "551", "552", "631", "632", "633", "634")));
        assertThat(chartConfig.get(5).getId(), is("52"));
        assertThat(chartConfig.get(5).getName(), is("Sluzby"));
        assertThat(chartConfig.get(5).getType(), is(ChartData.Config.ChartType.BALANCE));
        assertThat(chartConfig.get(5).getSchemas(), is(Set.of("52")));
        assertThat(chartConfig.get(7).getId(), is("op"));
        assertThat(chartConfig.get(7).getName(), is(OPERATING_PROFIT));
        assertThat(chartConfig.get(7).getType(), is(ChartData.Config.ChartType.BALANCE));
        assertThat(chartConfig.get(7).getSchemas(), is(Set.of("60", "550", "551", "552", "631", "632", "633", "634", "51", "52", "53")));
        assertThat(chartConfig.get(11).getId(), is("62"));
        assertThat(chartConfig.get(11).getName(), is("Financne - Vynosy"));
        assertThat(chartConfig.get(11).getType(), is(ChartData.Config.ChartType.BALANCE));
        assertThat(chartConfig.get(11).getSchemas(), is(Set.of("62")));
        assertThat(chartConfig.get(14).getId(), is("55b"));
        assertThat(chartConfig.get(14).getName(), is("Institucie - Ostatne - Naklady"));
        assertThat(chartConfig.get(14).getType(), is(ChartData.Config.ChartType.BALANCE));
        assertThat(chartConfig.get(14).getSchemas(), is(Set.of("553", "554", "555")));
        assertThat(chartConfig.get(15).getId(), is("np"));
        assertThat(chartConfig.get(15).getName(), is(NET_PROFIT));
        assertThat(chartConfig.get(15).getType(), is(ChartData.Config.ChartType.BALANCE));
        assertThat(chartConfig.get(15).getSchemas(), is(Set.of("5", "6")));


    }

    @Test
    @EnabledIf(value = "isDataLocationSet", disabledReason = "set data.location in test/resources/application.properties")
    public void getChartDataTest()
    {
        given().when().get("/sync/all").then().statusCode(200);

        AccountingDto overallProfit = given().when()
                .get("/accounting/profit")
                .then()
                .statusCode(200)
                .extract().response().jsonPath().getObject("", AccountingDto.class);

        ChartDto chart60 = given().when()
                .get("/chart/data/60")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart60.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12));
        assertThat(toYearlyBalances(chart60.getValues()), is(overallProfit.getRows().get(0).getYearlyValues()));

        ChartDto chart55a = given().when()
                .get("/chart/data/55a")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart55a.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12));
        assertThat(toYearlyBalances(chart55a.getValues()), is(overallProfit.getRows().get(1).getYearlyValues()));

        ChartDto chart63a = given().when()
                .get("/chart/data/63a")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart63a.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12));
        assertThat(toYearlyBalances(chart63a.getValues()), is(overallProfit.getRows().get(2).getYearlyValues()));

        ChartDto chartNi = given().when()
                .get("/chart/data/ni")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chartNi.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12));
        assertThat(toYearlyBalances(chartNi.getValues()), is(overallProfit.getRows().get(3).getYearlyValues()));

        ChartDto chart51 = given().when()
                .get("/chart/data/51")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart51.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12));
        assertThat(toYearlyBalances(chart51.getValues()), is(overallProfit.getRows().get(4).getYearlyValues()));

        ChartDto chart52 = given().when()
                .get("/chart/data/52")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart52.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12));
        assertThat(toYearlyBalances(chart52.getValues()), is(overallProfit.getRows().get(5).getYearlyValues()));

        ChartDto chart53 = given().when()
                .get("/chart/data/53")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart53.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12));
        assertThat(toYearlyBalances(chart53.getValues()), is(overallProfit.getRows().get(6).getYearlyValues()));

        ChartDto chartOp = given().when()
                .get("/chart/data/op")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chartOp.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12));
        assertThat(toYearlyBalances(chartOp.getValues()), is(overallProfit.getRows().get(7).getYearlyValues()));

        ChartDto chart50 = given().when()
                .get("/chart/data/50")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart50.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12));
        assertThat(toYearlyBalances(chart50.getValues()), is(overallProfit.getRows().get(8).getYearlyValues()));

        ChartDto chart61 = given().when()
                .get("/chart/data/61")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart61.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12));
        assertThat(toYearlyBalances(chart61.getValues()), is(overallProfit.getRows().get(9).getYearlyValues()));

        ChartDto chart56 = given().when()
                .get("/chart/data/56")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart56.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12));
        assertThat(toYearlyBalances(chart56.getValues()), is(overallProfit.getRows().get(10).getYearlyValues()));

        ChartDto chart62 = given().when()
                .get("/chart/data/62")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart62.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12));
        assertThat(toYearlyBalances(chart62.getValues()), is(overallProfit.getRows().get(11).getYearlyValues()));

        ChartDto chart54 = given().when()
                .get("/chart/data/54")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart54.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12));
        assertThat(toYearlyBalances(chart54.getValues()), is(overallProfit.getRows().get(12).getYearlyValues()));

        ChartDto chart63b = given().when()
                .get("/chart/data/63b")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart63b.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12));
        assertThat(toYearlyBalances(chart63b.getValues()), is(overallProfit.getRows().get(13).getYearlyValues()));

        ChartDto chart55b = given().when()
                .get("/chart/data/55b")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart55b.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12));
        assertThat(toYearlyBalances(chart55b.getValues()), is(overallProfit.getRows().get(14).getYearlyValues()));

        ChartDto chartNp = given().when()
                .get("/chart/data/np")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chartNp.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12));
        assertThat(toYearlyBalances(chartNp.getValues()), is(overallProfit.getRows().get(15).getYearlyValues()));
    }

    @Test
    public void parameterValidatorTest()
    {
        assertThat(given().when()
                .get("/chart/data/xxxxx")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Chart ID Parameter"));
    }

    private Integer[] toYearlyBalances(List<ChartDto.Value> values)
    {
        Integer[] yearly = new Integer[values.size()/12];
        Arrays.fill(yearly, 0);
        for (int i=0;i<values.size();i++)
        {
            yearly[i / 12] += values.get(i).getBalance();
        }
        return yearly;
    }

    boolean isDataLocationSet()
    {
        return !ConfigProvider.getConfig().getValue("data.location", String.class).equals("X");
    }
}
