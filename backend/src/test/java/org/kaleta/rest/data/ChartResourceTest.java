package org.kaleta.rest.data;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kaleta.Utils;
import org.kaleta.dto.AccountingDto;
import org.kaleta.dto.ChartDto;
import org.kaleta.model.ChartData;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.kaleta.Constants.Label.ASSETS;
import static org.kaleta.Constants.Label.CASH_FLOW;
import static org.kaleta.Constants.Label.LIABILITIES;
import static org.kaleta.Constants.Label.NET_INCOME;
import static org.kaleta.Constants.Label.NET_PROFIT;
import static org.kaleta.Constants.Label.OPERATING_PROFIT;
import static org.kaleta.Constants.Label.PROFIT;

@QuarkusTest
@TestProfile(DataSourceTestProfile.class)
public class ChartResourceTest
{
    // RestAssured only learns the application's port per test method, so the datasource is
    // written and synced here rather than once for the class.
    @BeforeEach
    void syncGeneratedDataSource()
    {
        FakeDataSource.generate();
        given().when().get("/sync/all").then().statusCode(200);
    }

    @Test
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
        assertThat(chartConfig.get(0).getName(), is("group60"));
        assertThat(chartConfig.get(0).getType(), is(ChartData.Config.ChartType.BALANCE));
        assertThat(chartConfig.get(0).getSchemas(), is(Set.of("60")));
        assertThat(chartConfig.get(3).getId(), is("ni"));
        assertThat(chartConfig.get(3).getName(), is(NET_INCOME));
        assertThat(chartConfig.get(3).getType(), is(ChartData.Config.ChartType.BALANCE));
        assertThat(chartConfig.get(3).getSchemas(), is(Set.of("60", "550", "551", "552", "631", "632", "633", "634")));
        assertThat(chartConfig.get(5).getId(), is("52"));
        assertThat(chartConfig.get(5).getName(), is("group52"));
        assertThat(chartConfig.get(5).getType(), is(ChartData.Config.ChartType.BALANCE));
        assertThat(chartConfig.get(5).getSchemas(), is(Set.of("52")));
        assertThat(chartConfig.get(7).getId(), is("op"));
        assertThat(chartConfig.get(7).getName(), is(OPERATING_PROFIT));
        assertThat(chartConfig.get(7).getType(), is(ChartData.Config.ChartType.BALANCE));
        assertThat(chartConfig.get(7).getSchemas(), is(Set.of("60", "550", "551", "552", "631", "632", "633", "634", "51", "52", "53")));
        assertThat(chartConfig.get(11).getId(), is("62"));
        assertThat(chartConfig.get(11).getName(), is("group62 - Vynosy"));
        assertThat(chartConfig.get(11).getType(), is(ChartData.Config.ChartType.BALANCE));
        assertThat(chartConfig.get(11).getSchemas(), is(Set.of("62")));
        assertThat(chartConfig.get(14).getId(), is("55b"));
        assertThat(chartConfig.get(14).getName(), is("group55 - Ostatne - Naklady"));
        assertThat(chartConfig.get(14).getType(), is(ChartData.Config.ChartType.BALANCE));
        assertThat(chartConfig.get(14).getSchemas(), is(Set.of("553", "554", "555", "556")));
        assertThat(chartConfig.get(15).getId(), is("np"));
        assertThat(chartConfig.get(15).getName(), is(NET_PROFIT));
        assertThat(chartConfig.get(15).getType(), is(ChartData.Config.ChartType.BALANCE));
        assertThat(chartConfig.get(15).getSchemas(), is(Set.of("5", "6")));

        assertThat(chartConfig.get(16).getId(), is("20"));
        assertThat(chartConfig.get(16).getName(), is("group20"));
        assertThat(chartConfig.get(16).getType(), is(ChartData.Config.ChartType.CUMULATIVE));
        assertThat(chartConfig.get(16).getSchemas(), is(Set.of("20")));
        assertThat(chartConfig.get(20).getId(), is("cf"));
        assertThat(chartConfig.get(20).getName(), is(CASH_FLOW));
        assertThat(chartConfig.get(20).getType(), is(ChartData.Config.ChartType.CUMULATIVE));
        assertThat(chartConfig.get(20).getSchemas(), is(Set.of("20", "21", "22", "23")));

        assertThat(chartConfig.get(21).getId(), is("a"));
        assertThat(chartConfig.get(21).getName(), is(ASSETS));
        assertThat(chartConfig.get(21).getType(), is(ChartData.Config.ChartType.CUMULATIVE));
        assertThat(chartConfig.get(21).getSchemas(), is(Set.of("0", "1", "20", "21", "23", "30")));
        assertThat(chartConfig.get(24).getId(), is("2a"));
        assertThat(chartConfig.get(24).getName(), is("class2 - " + ASSETS));
        assertThat(chartConfig.get(24).getType(), is(ChartData.Config.ChartType.CUMULATIVE));
        assertThat(chartConfig.get(24).getSchemas(), is(Set.of("20","21","23")));

        assertThat(chartConfig.get(26).getId(), is("l"));
        assertThat(chartConfig.get(26).getName(), is(LIABILITIES));
        assertThat(chartConfig.get(26).getType(), is(ChartData.Config.ChartType.CUMULATIVE));
        assertThat(chartConfig.get(26).getSchemas(), is(Set.of("22", "31", "4")));
        assertThat(chartConfig.get(28).getId(), is("3l"));
        assertThat(chartConfig.get(28).getName(), is("class3 - " + LIABILITIES));
        assertThat(chartConfig.get(28).getType(), is(ChartData.Config.ChartType.CUMULATIVE));
        assertThat(chartConfig.get(28).getSchemas(), is(Set.of("31")));
        assertThat(chartConfig.get(30).getId(), is("p"));
        assertThat(chartConfig.get(30).getName(), is(PROFIT));
        assertThat(chartConfig.get(30).getType(), is(ChartData.Config.ChartType.CUMULATIVE));
        assertThat(chartConfig.get(30).getSchemas(), is(Set.of("6", "5")));
    }

    @Test
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

        int futureMonths = 11 - new GregorianCalendar().get(Calendar.MONTH);

        assertThat(chart60.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyBalances(chart60.getValues()), overallProfit.getRows().get(0).getYearlyValues(), futureMonths);

        ChartDto chart55a = given().when()
                .get("/chart/data/55a")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart55a.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyBalances(chart55a.getValues()), overallProfit.getRows().get(1).getYearlyValues(), futureMonths);

        ChartDto chart63a = given().when()
                .get("/chart/data/63a")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart63a.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyBalances(chart63a.getValues()), overallProfit.getRows().get(2).getYearlyValues(), futureMonths);

        ChartDto chartNi = given().when()
                .get("/chart/data/ni")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chartNi.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyBalances(chartNi.getValues()), overallProfit.getRows().get(3).getYearlyValues(), futureMonths);

        ChartDto chart51 = given().when()
                .get("/chart/data/51")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart51.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyBalances(chart51.getValues()), overallProfit.getRows().get(4).getYearlyValues(), futureMonths);

        ChartDto chart52 = given().when()
                .get("/chart/data/52")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart52.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyBalances(chart52.getValues()), overallProfit.getRows().get(5).getYearlyValues(), futureMonths);

        ChartDto chart53 = given().when()
                .get("/chart/data/53")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart53.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyBalances(chart53.getValues()), overallProfit.getRows().get(6).getYearlyValues(), futureMonths);

        ChartDto chartOp = given().when()
                .get("/chart/data/op")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chartOp.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyBalances(chartOp.getValues()), overallProfit.getRows().get(7).getYearlyValues(), futureMonths);

        ChartDto chart50 = given().when()
                .get("/chart/data/50")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart50.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyBalances(chart50.getValues()), overallProfit.getRows().get(8).getYearlyValues(), futureMonths);

        ChartDto chart61 = given().when()
                .get("/chart/data/61")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart61.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyBalances(chart61.getValues()), overallProfit.getRows().get(9).getYearlyValues(), futureMonths);

        ChartDto chart56 = given().when()
                .get("/chart/data/56")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart56.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyBalances(chart56.getValues()), overallProfit.getRows().get(10).getYearlyValues(), futureMonths);

        ChartDto chart62 = given().when()
                .get("/chart/data/62")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart62.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyBalances(chart62.getValues()), overallProfit.getRows().get(11).getYearlyValues(), futureMonths);

        ChartDto chart54 = given().when()
                .get("/chart/data/54")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart54.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyBalances(chart54.getValues()), overallProfit.getRows().get(12).getYearlyValues(), futureMonths);

        ChartDto chart63b = given().when()
                .get("/chart/data/63b")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart63b.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyBalances(chart63b.getValues()), overallProfit.getRows().get(13).getYearlyValues(), futureMonths);

        ChartDto chart55b = given().when()
                .get("/chart/data/55b")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart55b.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyBalances(chart55b.getValues()), overallProfit.getRows().get(14).getYearlyValues(), futureMonths);

        ChartDto chartNp = given().when()
                .get("/chart/data/np")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chartNp.getValues().size(), is((overallProfit.getColumns().size() - 2) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyBalances(chartNp.getValues()), overallProfit.getRows().get(15).getYearlyValues(), futureMonths);

        AccountingDto overallCashFlow = given().when()
                .get("/accounting/cashflow")
                .then()
                .statusCode(200)
                .extract().response().jsonPath().getObject("", AccountingDto.class);

        ChartDto chart20 = given().when()
                .get("/chart/data/20")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart20.getValues().size(), is((overallCashFlow.getColumns().size() - 1) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyCumulative(chart20.getValues()), overallCashFlow.getRows().get(0).getYearlyValues(), futureMonths);

        ChartDto chart21 = given().when()
                .get("/chart/data/21")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart21.getValues().size(), is((overallCashFlow.getColumns().size() - 1) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyCumulative(chart21.getValues()), overallCashFlow.getRows().get(1).getYearlyValues(), futureMonths);

        ChartDto chart23 = given().when()
                .get("/chart/data/23")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart23.getValues().size(), is((overallCashFlow.getColumns().size() - 1) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyCumulative(chart23.getValues()), overallCashFlow.getRows().get(2).getYearlyValues(), futureMonths);

        ChartDto chart22 = given().when()
                .get("/chart/data/22")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart22.getValues().size(), is((overallCashFlow.getColumns().size() - 1) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyCumulative(chart22.getValues()), Utils.invertValues(overallCashFlow.getRows().get(3).getYearlyValues()), futureMonths);

        ChartDto chartCf = given().when()
                .get("/chart/data/cf")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chartCf.getValues().size(), is((overallCashFlow.getColumns().size() - 1) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyCumulative(chartCf.getValues()), overallCashFlow.getRows().get(4).getYearlyValues(), futureMonths);

        AccountingDto overallBalance = given().when()
                .get("/accounting/balance")
                .then()
                .statusCode(200)
                .extract().response().jsonPath().getObject("", AccountingDto.class);

        ChartDto chartA = given().when()
                .get("/chart/data/a")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chartA.getValues().size(), is((overallBalance.getColumns().size() - 1) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyCumulative(chartA.getValues()), overallBalance.getRows().get(0).getYearlyValues(), futureMonths);

        ChartDto chart0 = given().when()
                .get("/chart/data/0")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart0.getValues().size(), is((overallBalance.getColumns().size() - 1) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyCumulative(chart0.getValues()), overallBalance.getRows().get(1).getYearlyValues(), futureMonths);

        ChartDto chart1 = given().when()
                .get("/chart/data/1")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart1.getValues().size(), is((overallBalance.getColumns().size() - 1) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyCumulative(chart1.getValues()), overallBalance.getRows().get(2).getYearlyValues(), futureMonths);

        ChartDto chart2a = given().when()
                .get("/chart/data/2a")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart2a.getValues().size(), is((overallBalance.getColumns().size() - 1) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyCumulative(chart2a.getValues()), overallBalance.getRows().get(3).getYearlyValues(), futureMonths);

        ChartDto chart3a = given().when()
                .get("/chart/data/3a")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart3a.getValues().size(), is((overallBalance.getColumns().size() - 1) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyCumulative(chart3a.getValues()), overallBalance.getRows().get(4).getYearlyValues(), futureMonths);

        ChartDto chartL = given().when()
                .get("/chart/data/l")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chartL.getValues().size(), is((overallBalance.getColumns().size() - 1) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyCumulative(chartL.getValues()), overallBalance.getRows().get(5).getYearlyValues(), futureMonths);

        ChartDto chart2l = given().when()
                .get("/chart/data/2l")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart2l.getValues().size(), is((overallBalance.getColumns().size() - 1) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyCumulative(chart2l.getValues()), overallBalance.getRows().get(6).getYearlyValues(), futureMonths);

        ChartDto chart3l = given().when()
                .get("/chart/data/3l")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart3l.getValues().size(), is((overallBalance.getColumns().size() - 1) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyCumulative(chart3l.getValues()), overallBalance.getRows().get(7).getYearlyValues(), futureMonths);

        ChartDto chart4 = given().when()
                .get("/chart/data/4")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chart4.getValues().size(), is((overallBalance.getColumns().size() - 1) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyCumulative(chart4.getValues()), overallBalance.getRows().get(8).getYearlyValues(), futureMonths);

        ChartDto chartP = given().when()
                .get("/chart/data/p")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", ChartDto.class);

        assertThat(chartP.getValues().size(), is((overallBalance.getColumns().size() - 1) * 12 - futureMonths));
        assertYearlyValuesMatch(toYearlyBalances(chartP.getValues()), overallBalance.getRows().get(9).getYearlyValues(), futureMonths);
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

    /**
     * A chart and the accounting view it is derived from must report the same yearly totals,
     * but they deliberately disagree about the year that is still running: a chart stops at
     * the current month, while the accounting views report the whole year, including the
     * transactions the desktop app has already recorded for months still to come. Only the
     * closed years are therefore compared while the running year is still partly charted.
     */
    private void assertYearlyValuesMatch(Integer[] charted, Integer[] accounted, int futureMonths)
    {
        if (futureMonths == 0)
        {
            assertThat(charted, is(accounted));
        }
        else
        {
            assertThat(Arrays.copyOf(charted, charted.length - 1),
                    is(Arrays.copyOf(accounted, accounted.length - 1)));
        }
    }

    private Integer[] toYearlyBalances(List<ChartDto.Value> values)
    {
        Integer[] yearly = new Integer[values.size()/12 + (values.size() % 12 == 0 ? 0 : 1)];
        Arrays.fill(yearly, 0);
        for (int i=0;i<values.size();i++)
        {
            yearly[i / 12] += values.get(i).getBalance();
        }
        return yearly;
    }

    private Integer[] toYearlyCumulative(List<ChartDto.Value> values)
    {
        int newLength = values.size()/12 + (values.size() % 12 == 0 ? 0 : 1);
        Integer[] yearly = new Integer[newLength];
        Arrays.fill(yearly, 0);
        for (int i=0;i<newLength;i++)
        {
            int cumulativeIndex = (i*12 + 11) > (values.size() - 1) ? (i*12 + (values.size() % 12) - 1) : (i*12 + 11);
            yearly[i] += values.get(cumulativeIndex).getCumulative();
        }
        return yearly;
    }

}
