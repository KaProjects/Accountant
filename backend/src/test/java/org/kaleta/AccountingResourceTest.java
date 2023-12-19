package org.kaleta;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.kaleta.dto.AccountingDto;
import org.kaleta.dto.YearTransactionDto;

import javax.ws.rs.core.MediaType;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

@QuarkusTest
public class AccountingResourceTest
{
    @Test
    public void getTransactionsEndpointTest()
    {
        String year = "2023";
        String accountId = "000";
        String month = "5";

        List<YearTransactionDto> transactions =
                given().when()
                        .get("/accounting/" + year + "/transaction/" + accountId + "/month/" + month)
                        .then()
                        .statusCode(200)
                        .header("Content-Type", containsString(MediaType.APPLICATION_JSON))
                        .body("size()", is(4))
                        .body("[0].date", is("0505"))
                        .body("[1].date", is("1005"))
                        .body("[2].date", is("1505"))
                        .body("[3].date", is("2005"))
                        .extract().response().jsonPath().getList("", YearTransactionDto.class);

        assertThat(transactions, hasItem(yearTransactionDto("2005", "500", "account000", "account003", "month5 000")));
        assertThat(transactions, hasItem(yearTransactionDto("0505", "600", "account000", "account001", "month5 000")));
        assertThat(transactions, hasItem(yearTransactionDto("1505", "-700", "account001", "account000", "month5 000 -amount")));
        assertThat(transactions, hasItem(yearTransactionDto("1005", "800", "account000.2", "account001.2", "month5 000")));
    }

    @Test
    public void getCashFlowTest()
    {
        String year = "2020";

        Response responseInefficient = given().when()
                .get("/accounting/inefficient/" + year + "/cashflow")
                .then()
                .statusCode(200)
                .extract().response();

        System.out.println("inefficient response time: " + responseInefficient.time() + "ms");

        Response responseLatest = given().when()
                .get("/accounting/" + year + "/cashflow")
                .then()
                .statusCode(200)
                .extract().response();

        System.out.println("latest response time: " + responseLatest.time() + "ms");

        assertThat(responseLatest.time(), is(lessThan(responseInefficient.time() - 900)));

        AccountingDto dto = responseLatest.jsonPath().getObject("", AccountingDto.class);

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

    private YearTransactionDto yearTransactionDto(String date, String amount, String debit, String credit, String description)
    {
        YearTransactionDto dto = new YearTransactionDto();
        dto.setDate(date);
        dto.setAmount(amount);
        dto.setDebit(debit);
        dto.setCredit(credit);
        dto.setDescription(description);
        return dto;
    }
}