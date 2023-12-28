package org.kaleta;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.kaleta.dto.BudgetDto;
import org.kaleta.dto.YearTransactionDto;
import org.springframework.http.HttpStatus;

import java.util.Calendar;
import java.util.GregorianCalendar;
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
    public void getBudgetTest(){
        Response response = given().when()
                .get("/budget/2019")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response();

        System.out.println("response time: " + response.time() + "ms");

        BudgetDto dto = response.jsonPath().getObject("", BudgetDto.class);

        assertThat(dto.getColumns().size(), is(17));
        assertThat(dto.getColumns().get(0), is("Budget 2019"));
        assertThat(dto.getLastFilledMonth(), is(9));
        assertThat(dto.getRows().size(), is(12));

        assertThat(dto.getRows().get(0).getId(), is("i1"));
        assertThat(dto.getRows().get(0).getType(), is(BudgetDto.Row.Type.INCOME));
        assertThat(dto.getRows().get(0).getActual(), is(List.of(0, 0, 0, 0, 7000, 0, 300, 300, 0, 0, 0, 0)));
        assertThat(dto.getRows().get(0).getActualSum(), is(7600));
        assertThat(dto.getRows().get(0).getActualAvg(), is(844));
        assertThat(dto.getRows().get(0).getDeltaSum(), is(-1400));
        assertThat(dto.getRows().get(0).getDeltaAvg(), is(-156));
        assertThat(dto.getRows().get(0).getPlanned(), is(List.of(1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000)));
        assertThat(dto.getRows().get(0).getPlannedSum(), is(12000));
        assertThat(dto.getRows().get(0).getPlannedSumToFilledMonth(), is(9000));
        assertThat(dto.getRows().get(0).getPlannedAvg(), is(1000));
        assertThat(dto.getRows().get(0).getPlannedAvgToFilledMonth(), is(1000));
        assertThat(dto.getRows().get(0).getSubRows().size(), is(2));
        assertThat(dto.getRows().get(0).getSubRows().get(0).getId(), is("i1.1"));
        assertThat(dto.getRows().get(0).getSubRows().get(0).getType(), is(BudgetDto.Row.Type.SUB_ROW));
        assertThat(dto.getRows().get(0).getSubRows().get(0).getActual(), is(List.of(0, 0, 0, 0, 0, 0, 300, 300, 0, 0, 0, 0)));
        assertThat(dto.getRows().get(0).getSubRows().get(0).getActualSum(), is(600));
        assertThat(dto.getRows().get(0).getSubRows().get(0).getActualAvg(), is(66));
        assertThat(dto.getRows().get(0).getSubRows().get(0).getDeltaSum(), is(600));
        assertThat(dto.getRows().get(0).getSubRows().get(0).getDeltaAvg(), is(66));
        assertThat(dto.getRows().get(0).getSubRows().get(0).getPlanned(), is(List.of(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
        assertThat(dto.getRows().get(0).getSubRows().get(0).getSubRows().size(), is(0));
        assertThat(dto.getRows().get(0).getSubRows().get(1).getId(), is("i1.2"));
        assertThat(dto.getRows().get(0).getSubRows().get(1).getType(), is(BudgetDto.Row.Type.SUB_ROW));
        assertThat(dto.getRows().get(0).getSubRows().get(1).getActual(), is(List.of(0, 0, 0, 0, 7000, 0, 0, 0, 0, 0, 0, 0)));
        assertThat(dto.getRows().get(0).getSubRows().get(1).getActualSum(), is(7000));
        assertThat(dto.getRows().get(0).getSubRows().get(1).getActualAvg(), is(777));
        assertThat(dto.getRows().get(0).getSubRows().get(1).getDeltaSum(), is(7000));
        assertThat(dto.getRows().get(0).getSubRows().get(1).getDeltaAvg(), is(777));
        assertThat(dto.getRows().get(0).getSubRows().get(1).getPlanned(), is(List.of(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
        assertThat(dto.getRows().get(0).getSubRows().get(1).getSubRows().size(), is(0));

        assertThat(dto.getRows().get(1).getId(), is("i2"));
        assertThat(dto.getRows().get(1).getType(), is(BudgetDto.Row.Type.INCOME));
        assertThat(dto.getRows().get(1).getActual(), is(List.of(0, 0, 0, 0, 0, 0, 0, 0, 50, 0, 0, 0)));
        assertThat(dto.getRows().get(1).getActualSum(), is(50));
        assertThat(dto.getRows().get(1).getActualAvg(), is(5));
        assertThat(dto.getRows().get(1).getDeltaSum(), is(-550));
        assertThat(dto.getRows().get(1).getDeltaAvg(), is(-61));
        assertThat(dto.getRows().get(1).getPlanned(), is(List.of(50, 50, 50, 50, 50, 50, 100, 100, 100, 100, 100, 100)));
        assertThat(dto.getRows().get(1).getPlannedSum(), is(900));
        assertThat(dto.getRows().get(1).getPlannedSumToFilledMonth(), is(600));
        assertThat(dto.getRows().get(1).getPlannedAvg(), is(75));
        assertThat(dto.getRows().get(1).getPlannedAvgToFilledMonth(), is(66));
        assertThat(dto.getRows().get(1).getSubRows().size(), is(0));

        assertThat(dto.getRows().get(2).getId(), is("i"));
        assertThat(dto.getRows().get(2).getType(), is(BudgetDto.Row.Type.INCOME_SUM));
        assertThat(dto.getRows().get(2).getActual(), is(List.of(0, 0, 0, 0, 7000, 0, 300, 300, 50, 0, 0, 0)));
        assertThat(dto.getRows().get(2).getActualSum(), is(7650));
        assertThat(dto.getRows().get(2).getActualAvg(), is(850));
        assertThat(dto.getRows().get(2).getDeltaSum(), is(-1950));
        assertThat(dto.getRows().get(2).getDeltaAvg(), is(-216));
        assertThat(dto.getRows().get(2).getPlanned(), is(List.of(1050, 1050, 1050, 1050, 1050, 1050, 1100, 1100, 1100, 1100, 1100, 1100)));
        assertThat(dto.getRows().get(2).getPlannedSum(), is(12900));
        assertThat(dto.getRows().get(2).getPlannedSumToFilledMonth(), is(9600));
        assertThat(dto.getRows().get(2).getPlannedAvg(), is(1075));
        assertThat(dto.getRows().get(2).getPlannedAvgToFilledMonth(), is(1066));
        assertThat(dto.getRows().get(2).getSubRows().size(), is(0));

        assertThat(dto.getRows().get(3).getId(), is("me1"));
        assertThat(dto.getRows().get(3).getType(), is(BudgetDto.Row.Type.EXPENSE));
        assertThat(dto.getRows().get(3).getActual(), is(List.of(0, 800, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
        assertThat(dto.getRows().get(3).getActualSum(), is(800));
        assertThat(dto.getRows().get(3).getActualAvg(), is(88));
        assertThat(dto.getRows().get(3).getDeltaSum(), is(-100));
        assertThat(dto.getRows().get(3).getDeltaAvg(), is(-12));
        assertThat(dto.getRows().get(3).getPlanned(), is(List.of(100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100)));
        assertThat(dto.getRows().get(3).getPlannedSum(), is(1200));
        assertThat(dto.getRows().get(3).getPlannedSumToFilledMonth(), is(900));
        assertThat(dto.getRows().get(3).getPlannedAvg(), is(100));
        assertThat(dto.getRows().get(3).getPlannedAvgToFilledMonth(), is(100));
        assertThat(dto.getRows().get(3).getSubRows().size(), is(0));

        assertThat(dto.getRows().get(4).getId(), is("me2"));
        assertThat(dto.getRows().get(4).getType(), is(BudgetDto.Row.Type.EXPENSE));
        assertThat(dto.getRows().get(4).getActual(), is(List.of(0, 0, 900, 0, 7000, 0, 0, 0, 0, 0, 0, 0)));
        assertThat(dto.getRows().get(4).getActualSum(), is(7900));
        assertThat(dto.getRows().get(4).getActualAvg(), is(877));
        assertThat(dto.getRows().get(4).getDeltaSum(), is(-10100));
        assertThat(dto.getRows().get(4).getDeltaAvg(), is(-1123));
        assertThat(dto.getRows().get(4).getPlanned(), is(List.of(2000, 2000, 2000, 2000, 2000, 2000, 2000, 2000, 2000, 2000, 2000, 2000)));
        assertThat(dto.getRows().get(4).getPlannedSum(), is(24000));
        assertThat(dto.getRows().get(4).getPlannedSumToFilledMonth(), is(18000));
        assertThat(dto.getRows().get(4).getPlannedAvg(), is(2000));
        assertThat(dto.getRows().get(4).getPlannedAvgToFilledMonth(), is(2000));
        assertThat(dto.getRows().get(4).getSubRows().size(), is(2));
        assertThat(dto.getRows().get(4).getSubRows().get(0).getId(), is("me2.1"));
        assertThat(dto.getRows().get(4).getSubRows().get(0).getType(), is(BudgetDto.Row.Type.SUB_ROW));
        assertThat(dto.getRows().get(4).getSubRows().get(0).getActual(), is(List.of(0, 0, 900, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
        assertThat(dto.getRows().get(4).getSubRows().get(0).getActualSum(), is(900));
        assertThat(dto.getRows().get(4).getSubRows().get(0).getActualAvg(), is(100));
        assertThat(dto.getRows().get(4).getSubRows().get(0).getDeltaSum(), is(900));
        assertThat(dto.getRows().get(4).getSubRows().get(0).getDeltaAvg(), is(100));
        assertThat(dto.getRows().get(4).getSubRows().get(0).getPlanned(), is(List.of(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
        assertThat(dto.getRows().get(4).getSubRows().get(0).getSubRows().size(), is(0));
        assertThat(dto.getRows().get(4).getSubRows().get(1).getId(), is("me2.2"));
        assertThat(dto.getRows().get(4).getSubRows().get(1).getType(), is(BudgetDto.Row.Type.SUB_ROW));
        assertThat(dto.getRows().get(4).getSubRows().get(1).getActual(), is(List.of(0, 0, 0, 0, 7000, 0, 0, 0, 0, 0, 0, 0)));
        assertThat(dto.getRows().get(4).getSubRows().get(1).getActualSum(), is(7000));
        assertThat(dto.getRows().get(4).getSubRows().get(1).getActualAvg(), is(777));
        assertThat(dto.getRows().get(4).getSubRows().get(1).getDeltaSum(), is(7000));
        assertThat(dto.getRows().get(4).getSubRows().get(1).getDeltaAvg(), is(777));
        assertThat(dto.getRows().get(4).getSubRows().get(1).getPlanned(), is(List.of(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
        assertThat(dto.getRows().get(4).getSubRows().get(1).getSubRows().size(), is(0));

        assertThat(dto.getRows().get(5).getId(), is("me"));
        assertThat(dto.getRows().get(5).getType(), is(BudgetDto.Row.Type.EXPENSE_SUM));
        assertThat(dto.getRows().get(5).getActual(), is(List.of(0, 800, 900, 0, 7000, 0, 0, 0, 0, 0, 0, 0)));
        assertThat(dto.getRows().get(5).getActualSum(), is(8700));
        assertThat(dto.getRows().get(5).getActualAvg(), is(966));
        assertThat(dto.getRows().get(5).getDeltaSum(), is(-10200));
        assertThat(dto.getRows().get(5).getDeltaAvg(), is(-1134));
        assertThat(dto.getRows().get(5).getPlanned(), is(List.of(2100, 2100, 2100, 2100, 2100, 2100, 2100, 2100, 2100, 2100, 2100, 2100)));
        assertThat(dto.getRows().get(5).getPlannedSum(), is(25200));
        assertThat(dto.getRows().get(5).getPlannedSumToFilledMonth(), is(18900));
        assertThat(dto.getRows().get(5).getPlannedAvg(), is(2100));
        assertThat(dto.getRows().get(5).getPlannedAvgToFilledMonth(), is(2100));
        assertThat(dto.getRows().get(5).getSubRows().size(), is(0));

        assertThat(dto.getRows().get(6).getId(), is("ntme"));
        assertThat(dto.getRows().get(6).getType(), is(BudgetDto.Row.Type.BALANCE));
        assertThat(dto.getRows().get(6).getActual(), is(List.of(0, -800, -900, 0, 0, 0, 300, 300, 50, 0, 0, 0)));
        assertThat(dto.getRows().get(6).getActualSum(), is(-1050));
        assertThat(dto.getRows().get(6).getActualAvg(), is(-116));
        assertThat(dto.getRows().get(6).getDeltaSum(), is(8250));
        assertThat(dto.getRows().get(6).getDeltaAvg(), is(917));
        assertThat(dto.getRows().get(6).getPlanned(), is(List.of(-1050, -1050, -1050, -1050, -1050, -1050, -1000, -1000, -1000, -1000, -1000, -1000)));
        assertThat(dto.getRows().get(6).getPlannedSum(), is(-12300));
        assertThat(dto.getRows().get(6).getPlannedSumToFilledMonth(), is(-9300));
        assertThat(dto.getRows().get(6).getPlannedAvg(), is(-1025));
        assertThat(dto.getRows().get(6).getPlannedAvgToFilledMonth(), is(-1033));
        assertThat(dto.getRows().get(6).getSubRows().size(), is(0));

        assertThat(dto.getRows().get(7).getId(), is("e1"));
        assertThat(dto.getRows().get(7).getType(), is(BudgetDto.Row.Type.EXPENSE));
        assertThat(dto.getRows().get(7).getActual(), is(List.of(0, 800, 900, 0, 0, 0, 200, 200, 0, 0, 0, 0)));
        assertThat(dto.getRows().get(7).getActualSum(), is(2100));
        assertThat(dto.getRows().get(7).getActualAvg(), is(233));
        assertThat(dto.getRows().get(7).getDeltaSum(), is(-2400));
        assertThat(dto.getRows().get(7).getDeltaAvg(), is(-267));
        assertThat(dto.getRows().get(7).getPlanned(), is(List.of(500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500)));
        assertThat(dto.getRows().get(7).getPlannedSum(), is(6000));
        assertThat(dto.getRows().get(7).getPlannedSumToFilledMonth(), is(4500));
        assertThat(dto.getRows().get(7).getPlannedAvg(), is(500));
        assertThat(dto.getRows().get(7).getPlannedAvgToFilledMonth(), is(500));
        assertThat(dto.getRows().get(7).getSubRows().size(), is(2));
        assertThat(dto.getRows().get(7).getSubRows().get(0).getId(), is("e1.1"));
        assertThat(dto.getRows().get(7).getSubRows().get(0).getType(), is(BudgetDto.Row.Type.SUB_ROW));
        assertThat(dto.getRows().get(7).getSubRows().get(0).getActual(), is(List.of(0, 0, 0, 0, 0, 0, 200, 200, 0, 0, 0, 0)));
        assertThat(dto.getRows().get(7).getSubRows().get(0).getActualSum(), is(400));
        assertThat(dto.getRows().get(7).getSubRows().get(0).getActualAvg(), is(44));
        assertThat(dto.getRows().get(7).getSubRows().get(0).getDeltaSum(), is(400));
        assertThat(dto.getRows().get(7).getSubRows().get(0).getDeltaAvg(), is(44));
        assertThat(dto.getRows().get(7).getSubRows().get(0).getPlanned(), is(List.of(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
        assertThat(dto.getRows().get(7).getSubRows().get(0).getSubRows().size(), is(0));
        assertThat(dto.getRows().get(7).getSubRows().get(1).getId(), is("e1.2"));
        assertThat(dto.getRows().get(7).getSubRows().get(1).getType(), is(BudgetDto.Row.Type.SUB_ROW));
        assertThat(dto.getRows().get(7).getSubRows().get(1).getActual(), is(List.of(0, 800, 900, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
        assertThat(dto.getRows().get(7).getSubRows().get(1).getActualSum(), is(1700));
        assertThat(dto.getRows().get(7).getSubRows().get(1).getActualAvg(), is(188));
        assertThat(dto.getRows().get(7).getSubRows().get(1).getDeltaSum(), is(1700));
        assertThat(dto.getRows().get(7).getSubRows().get(1).getDeltaAvg(), is(188));
        assertThat(dto.getRows().get(7).getSubRows().get(1).getPlanned(), is(List.of(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
        assertThat(dto.getRows().get(7).getSubRows().get(1).getSubRows().size(), is(0));

        assertThat(dto.getRows().get(8).getId(), is("bcf"));
        assertThat(dto.getRows().get(8).getType(), is(BudgetDto.Row.Type.BALANCE));
        assertThat(dto.getRows().get(8).getActual(), is(List.of(0, -1600, -1800, 0, 0, 0, 100, 100, 50, 0, 0, 0)));
        assertThat(dto.getRows().get(8).getActualSum(), is(-3150));
        assertThat(dto.getRows().get(8).getActualAvg(), is(-350));
        assertThat(dto.getRows().get(8).getDeltaSum(), is(10650));
        assertThat(dto.getRows().get(8).getDeltaAvg(), is(1183));
        assertThat(dto.getRows().get(8).getPlanned(), is(List.of(-1550, -1550, -1550, -1550, -1550, -1550, -1500, -1500, -1500, -1500, -1500, -1500)));
        assertThat(dto.getRows().get(8).getPlannedSum(), is(-18300));
        assertThat(dto.getRows().get(8).getPlannedSumToFilledMonth(), is(-13800));
        assertThat(dto.getRows().get(8).getPlannedAvg(), is(-1525));
        assertThat(dto.getRows().get(8).getPlannedAvgToFilledMonth(), is(-1533));
        assertThat(dto.getRows().get(8).getSubRows().size(), is(0));

        assertThat(dto.getRows().get(9).getId(), is("of1"));
        assertThat(dto.getRows().get(9).getType(), is(BudgetDto.Row.Type.OF_BUDGET));
        assertThat(dto.getRows().get(9).getActual(), is(List.of(0, 0, 0, 0, 0, 0, 0, 0, 10000, 0, 0, 0)));
        assertThat(dto.getRows().get(9).getActualSum(), is(10000));
        assertThat(dto.getRows().get(9).getActualAvg(), is(1111));
        assertThat(dto.getRows().get(9).getDeltaSum(), is(1000));
        assertThat(dto.getRows().get(9).getDeltaAvg(), is(111));
        assertThat(dto.getRows().get(9).getPlanned(), is(List.of(1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000)));
        assertThat(dto.getRows().get(9).getPlannedSum(), is(12000));
        assertThat(dto.getRows().get(9).getPlannedSumToFilledMonth(), is(9000));
        assertThat(dto.getRows().get(9).getPlannedAvg(), is(1000));
        assertThat(dto.getRows().get(9).getPlannedAvgToFilledMonth(), is(1000));
        assertThat(dto.getRows().get(9).getSubRows().size(), is(0));

        assertThat(dto.getRows().get(10).getId(), is("of2"));
        assertThat(dto.getRows().get(10).getType(), is(BudgetDto.Row.Type.OF_BUDGET));
        assertThat(dto.getRows().get(10).getActual(), is(List.of(0, 0, 0, 1000, 0, 0, 200, 200, 10000, 0, 0, 0)));
        assertThat(dto.getRows().get(10).getActualSum(), is(11400));
        assertThat(dto.getRows().get(10).getActualAvg(), is(1266));
        assertThat(dto.getRows().get(10).getDeltaSum(), is(3400));
        assertThat(dto.getRows().get(10).getDeltaAvg(), is(378));
        assertThat(dto.getRows().get(10).getPlanned(), is(List.of(1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 0, 0, 0, 0)));
        assertThat(dto.getRows().get(10).getPlannedSum(), is(8000));
        assertThat(dto.getRows().get(10).getPlannedSumToFilledMonth(), is(8000));
        assertThat(dto.getRows().get(10).getPlannedAvg(), is(666));
        assertThat(dto.getRows().get(10).getPlannedAvgToFilledMonth(), is(888));
        assertThat(dto.getRows().get(10).getSubRows().size(), is(2));
        assertThat(dto.getRows().get(10).getSubRows().get(0).getId(), is("of2.1"));
        assertThat(dto.getRows().get(10).getSubRows().get(0).getType(), is(BudgetDto.Row.Type.SUB_ROW));
        assertThat(dto.getRows().get(10).getSubRows().get(0).getActual(), is(List.of(0, 0, 0, 1000, 0, 0, 0, 0, 0, 0, 0, 0)));
        assertThat(dto.getRows().get(10).getSubRows().get(0).getActualSum(), is(1000));
        assertThat(dto.getRows().get(10).getSubRows().get(0).getActualAvg(), is(111));
        assertThat(dto.getRows().get(10).getSubRows().get(0).getDeltaSum(), is(1000));
        assertThat(dto.getRows().get(10).getSubRows().get(0).getDeltaAvg(), is(111));
        assertThat(dto.getRows().get(10).getSubRows().get(0).getPlanned(), is(List.of(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
        assertThat(dto.getRows().get(10).getSubRows().get(0).getSubRows().size(), is(0));
        assertThat(dto.getRows().get(10).getSubRows().get(1).getId(), is("of2.2"));
        assertThat(dto.getRows().get(10).getSubRows().get(1).getType(), is(BudgetDto.Row.Type.SUB_ROW));
        assertThat(dto.getRows().get(10).getSubRows().get(1).getActual(), is(List.of(0, 0, 0, 0, 0, 0, 200, 200, 10000, 0, 0, 0)));
        assertThat(dto.getRows().get(10).getSubRows().get(1).getActualSum(), is(10400));
        assertThat(dto.getRows().get(10).getSubRows().get(1).getActualAvg(), is(1155));
        assertThat(dto.getRows().get(10).getSubRows().get(1).getDeltaSum(), is(10400));
        assertThat(dto.getRows().get(10).getSubRows().get(1).getDeltaAvg(), is(1155));
        assertThat(dto.getRows().get(10).getSubRows().get(1).getPlanned(), is(List.of(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
        assertThat(dto.getRows().get(10).getSubRows().get(1).getSubRows().size(), is(0));

        assertThat(dto.getRows().get(11).getId(), is("dcf"));
        assertThat(dto.getRows().get(11).getType(), is(BudgetDto.Row.Type.OF_BUDGET_BALANCE));
        assertThat(dto.getRows().get(11).getActual(), is(List.of(0, 0, 0, 1000, 0, 0, 200, 200, 20000, 0, 0, 0)));
        assertThat(dto.getRows().get(11).getActualSum(), is(21400));
        assertThat(dto.getRows().get(11).getActualAvg(), is(2377));
        assertThat(dto.getRows().get(11).getDeltaSum(), is(4400));
        assertThat(dto.getRows().get(11).getDeltaAvg(), is(489));
        assertThat(dto.getRows().get(11).getPlanned(), is(List.of(2000, 2000, 2000, 2000, 2000, 2000, 2000, 2000, 1000, 1000, 1000, 1000)));
        assertThat(dto.getRows().get(11).getPlannedSum(), is(20000));
        assertThat(dto.getRows().get(11).getPlannedSumToFilledMonth(), is(17000));
        assertThat(dto.getRows().get(11).getPlannedAvg(), is(1666));
        assertThat(dto.getRows().get(11).getPlannedAvgToFilledMonth(), is(1888));
        assertThat(dto.getRows().get(11).getSubRows().size(), is(0));
    }

    @Test
    public void getTransactionsTest()
    {
       List<YearTransactionDto> transactions = given().when()
               .get("/budget/2019/transaction/i2/month/9")
               .then()
               .statusCode(200)
               .contentType(ContentType.JSON)
               .body("size()", is(5))
               .body("[0].date", is("0909"))
               .body("[1].date", is("1009"))
               .body("[2].date", is("1509"))
               .body("[3].date", is("2009"))
               .body("[4].date", is("2109"))
               .extract().response().jsonPath().getList("", YearTransactionDto.class);

        assertThat(transactions, hasItem(YearTransactionDto.from("1509", "10", "account553", "sda ad", "same group")));
        assertThat(transactions, hasItem(YearTransactionDto.from("1009", "10", "account553", "sda ad", "same group")));
        assertThat(transactions, hasItem(YearTransactionDto.from("0909", "10", "account553", "sda ad", "same group")));
        assertThat(transactions, hasItem(YearTransactionDto.from("2009", "10", "account553", "sda ad", "same group")));
        assertThat(transactions, hasItem(YearTransactionDto.from("2109", "10", "account553", "sda ad", "same group")));
    }

    @Test
    public void parameterValidatorTest()
    {
        String validYear = "2019";
        String validBudgetId = "i2";
        String validMonth = "9";

        assertThat(given().when()
                .get("/budget/" + "2x20" + "/transaction/" + validBudgetId + "/month/" + validMonth)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));

        assertThat(given().when()
                .get("/budget/" + "2014" + "/transaction/" + validBudgetId + "/month/" + validMonth)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString(), containsString(" not found"));

        assertThat(given().when()
                .get("/budget/" + (new GregorianCalendar().get(Calendar.YEAR) + 1) + "/transaction/" + validBudgetId + "/month/" + validMonth)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString(), containsString(" not found"));

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
                .get("/budget/" + "2x20")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));

        assertThat(given().when()
                .get("/budget/" + "2014")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString(), containsString(" not found"));

        assertThat(given().when()
                .get("/budget/" + (new GregorianCalendar().get(Calendar.YEAR) + 1))
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString(), containsString(" not found"));
    }
}
