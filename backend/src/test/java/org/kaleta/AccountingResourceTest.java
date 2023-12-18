package org.kaleta;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.kaleta.dto.YearTransactionDto;

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
        String year = "2023";

        given().when()
                .get("/accounting/" + year + "/cashflow")
                .then()
                .statusCode(500);
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
