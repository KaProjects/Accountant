package org.kaleta;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.kaleta.dto.FinancialAssetsDto;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class FinancialResourceTest
{
    @Test
    public void getFinancialAssetsProgressTest()
    {
        Response response = given().when()
                .get("/financial/assets/2021")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response();

        System.out.println("response time: " + response.time() + "ms");

        FinancialAssetsDto dto = response.jsonPath().getObject("", FinancialAssetsDto.class);

        assertThat(dto.getGroups().size(), is(2));
        assertThat(dto.getGroups().get(0).getName(), is("ACCOUNT230"));
        assertThat(dto.getGroups().get(0).getAccounts().size(), is(2));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getName(), is("fin0-0"));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getInitialValue(), is(1000));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getDepositsSum(), is(0));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getWithdrawalsSum(), is(0));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getCurrentValue(), is(1000));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getCurrentReturn(), is(new BigDecimal("0.0")));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getDeposits(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getWithdrawals(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getRevaluations(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getLabels().length, is(12));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getLabels()[2], is("3/21"));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getBalances(), is(new Integer[]{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000}));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getFunding(), is(new Integer[]{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000}));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getCumulativeDeposits(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getCumulativeWithdrawals(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));

        assertThat(dto.getGroups().get(0).getAccounts().get(1).getName(), is("fin0-1"));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getInitialValue(), is(2000));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getDepositsSum(), is(3000));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getWithdrawalsSum(), is(1000));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getCurrentValue(), is(1000));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getCurrentReturn(), is(BigDecimal.valueOf(-60.0)));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getDeposits(), is(new Integer[]{0, 1000, 1000, 0, 0, 0, 0, 0, 0, 1000, 0, 0}));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getWithdrawals(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 1000, 0, 0, 0, 0}));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getRevaluations(), is(new Integer[]{0, 0, 0, -1000, 0, 1000, 0, 0, 1000, 0, -1000, 0}));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getLabels().length, is(12));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getLabels()[3], is("4/21"));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getBalances(), is(new Integer[]{2000, 2000, 2000, 1000, 1000, 2000, 2000, 1000, 2000, 2000, 1000, 1000}));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getFunding(), is(new Integer[]{2000, 3000, 4000, 4000, 4000, 4000, 4000, 3000, 3000, 4000, 4000, 4000}));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getCumulativeDeposits(), is(new Integer[]{0, 1000, 2000, 2000, 2000, 2000, 2000, 2000, 2000, 3000, 3000, 3000}));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getCumulativeWithdrawals(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 1000, 1000, 1000, 1000, 1000}));

        assertThat(dto.getGroups().get(1).getName(), is("ACCOUNT231"));
        assertThat(dto.getGroups().get(1).getAccounts().size(), is(1));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getName(), is("fin1-0"));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getInitialValue(), is(3000));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getDepositsSum(), is(0));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getWithdrawalsSum(), is(0));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getCurrentValue(), is(3000));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getCurrentReturn(), is(new BigDecimal("0.0")));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getDeposits(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getWithdrawals(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getRevaluations(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getLabels().length, is(12));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getLabels()[4], is("5/21"));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getBalances(), is(new Integer[]{3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000}));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getFunding(), is(new Integer[]{3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000}));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getCumulativeDeposits(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getCumulativeWithdrawals(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
    }

    @Test
    public void getFinancialAssetsOverallProgressTest()
    {
        Response response = given().when()
                .get("/financial/assets")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response();

        System.out.println("response time: " + response.time() + "ms");

        FinancialAssetsDto dto = response.jsonPath().getObject("", FinancialAssetsDto.class);

        assertThat(dto.getGroups().size(), is(2));
        assertThat(dto.getGroups().get(0).getName(), is("GROUP0"));
        assertThat(dto.getGroups().get(0).getAccounts().size(), is(2));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getName(), is("aaaa0-0"));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getInitialValue(), is(1000));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getDepositsSum(), is(0));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getWithdrawalsSum(), is(0));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getCurrentValue(), is(1000));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getCurrentReturn(), is(new BigDecimal("0.0")));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getDeposits(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getWithdrawals(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getRevaluations(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getLabels().length, is(24));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getLabels()[2], is("3/21"));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getLabels()[14], is("3/22"));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getBalances(), is(new Integer[]{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000}));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getFunding(), is(new Integer[]{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000}));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getCumulativeDeposits(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getGroups().get(0).getAccounts().get(0).getCumulativeWithdrawals(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));

        assertThat(dto.getGroups().get(0).getAccounts().get(1).getName(), is("bbbb0-1"));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getInitialValue(), is(2000));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getDepositsSum(), is(3000));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getWithdrawalsSum(), is(1000));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getCurrentValue(), is(1000));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getCurrentReturn(), is(BigDecimal.valueOf(-60.0)));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getDeposits(), is(new Integer[]{0, 1000, 1000, 0, 0, 0, 0, 0, 0, 1000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getWithdrawals(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 1000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getRevaluations(), is(new Integer[]{0, 0, 0, -1000, 0, 1000, 0, 0, 1000, 0, -1000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getLabels().length, is(24));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getLabels()[3], is("4/21"));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getLabels()[15], is("4/22"));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getBalances(), is(new Integer[]{2000, 2000, 2000, 1000, 1000, 2000, 2000, 1000, 2000, 2000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000}));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getFunding(), is(new Integer[]{2000, 3000, 4000, 4000, 4000, 4000, 4000, 3000, 3000, 4000, 4000, 4000, 4000, 4000, 4000, 4000, 4000, 4000, 4000, 4000, 4000, 4000, 4000, 4000}));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getCumulativeDeposits(), is(new Integer[]{0, 1000, 2000, 2000, 2000, 2000, 2000, 2000, 2000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000}));
        assertThat(dto.getGroups().get(0).getAccounts().get(1).getCumulativeWithdrawals(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000}));

        assertThat(dto.getGroups().get(1).getName(), is("GROUP1"));
        assertThat(dto.getGroups().get(1).getAccounts().size(), is(1));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getName(), is("cccc1-0"));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getInitialValue(), is(3000));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getDepositsSum(), is(3000));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getWithdrawalsSum(), is(1000));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getCurrentValue(), is(3000));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getCurrentReturn(), is(new BigDecimal("-33.33")));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getDeposits(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1000, 1000, 0, 0, 1000, 0, 0, 0, 0, 0, 0}));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getWithdrawals(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1000, 0, 0, 0, 0}));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getRevaluations(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1500, -500, 0, 1000, 0, 0, 0, 0, -1000, 0}));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getLabels().length, is(24));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getLabels()[4], is("5/21"));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getLabels()[16], is("5/22"));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getBalances(), is(new Integer[]{3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 4500, 4000, 4000, 5000, 5000, 4000, 4000, 4000, 3000, 3000}));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getFunding(), is(new Integer[]{3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 4000, 5000, 5000, 5000, 6000, 6000, 5000, 5000, 5000, 5000, 5000}));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getCumulativeDeposits(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1000, 2000, 2000, 2000, 3000, 3000, 3000, 3000, 3000, 3000, 3000}));
        assertThat(dto.getGroups().get(1).getAccounts().get(0).getCumulativeWithdrawals(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1000, 1000, 1000, 1000, 1000}));
    }

    @Test
    public void parameterValidatorTest()
    {
        assertThat(given().when()
                .get("/financial/assets/2x20")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));

        assertThat(given().when()
                .get("/financial/assets/2014")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString(), containsString(" not found"));

        assertThat(given().when()
                .get("/financial/assets/" + (new GregorianCalendar().get(Calendar.YEAR) + 1))
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString(), containsString(" not found"));
    }
}
