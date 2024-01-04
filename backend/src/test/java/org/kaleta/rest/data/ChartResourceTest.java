package org.kaleta.rest.data;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.kaleta.Utils;
import org.kaleta.dto.AccountingDto;
import org.kaleta.dto.ChartDto;
import org.kaleta.model.ChartData;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class ChartResourceTest
{
    @Test
    @EnabledIf(value = "isDataLocationSet", disabledReason = "set data.location in test/resources/application.properties")
    public void getChartConfigs()
    {
        given().when().get("/sync/all").then().statusCode(200);

        Map<Object, LinkedHashMap> chartConfig = given().when()
                .get("/chart/config")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getMap("");

        for (Object key : chartConfig.keySet()) {
            assertThat(key, is(in(ChartData.getConfigs(new HashMap<>()).keySet().toArray())));
        }
        assertThat(chartConfig.get("60").get("name"), is("Pracovne"));
        assertThat(chartConfig.get("60").get("type"), is(ChartData.Config.ChartType.BALANCE.toString()));
        assertThat(chartConfig.get("60").get("schemas"), is(List.of("60")));
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

        assertThat(chart60.getLabels().length, is((overallProfit.getColumns().size() - 2) * 12));
        assertThat(toYearly(chart60.getValues()), is(overallProfit.getRows().get(0).getYearlyValues()));

        ChartDto chart55a = given().when()
                .get("/chart/data/55a")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart55a.getLabels().length, is((overallProfit.getColumns().size() - 2) * 12));
        assertThat(toYearly(chart55a.getValues()), is(overallProfit.getRows().get(1).getYearlyValues()));

        ChartDto chart63a = given().when()
                .get("/chart/data/63a")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart63a.getLabels().length, is((overallProfit.getColumns().size() - 2) * 12));
        assertThat(toYearly(chart63a.getValues()), is(overallProfit.getRows().get(2).getYearlyValues()));

        ChartDto chartNp = given().when()
                .get("/chart/data/np")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chartNp.getLabels().length, is((overallProfit.getColumns().size() - 2) * 12));
        assertThat(toYearly(chartNp.getValues()), is(overallProfit.getRows().get(3).getYearlyValues()));
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

    private Integer[] toYearly(Integer[] monthly)
    {
        Integer[] yearly = new Integer[monthly.length/12];
        for (int i=0;i<monthly.length/12;i++)
        {
            yearly[i] = Utils.sumArray(Arrays.copyOfRange(monthly, i*12, i*12 + 12));
        }
        return yearly;
    }

    boolean isDataLocationSet()
    {
        return !ConfigProvider.getConfig().getValue("data.location", String.class).equals("X");
    }
}
