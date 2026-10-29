package org.kaleta.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.kaleta.dto.FinancialAssetsDto;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class FinancialResourceTest
{
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

        // The view is built from the accounts themselves: an asset is every year that
        // holds its id, so its timeline starts in the year it was acquired. Names are
        // taken from the most recent year, groups from the schema.
        assertThat(dto.getGroups().size(), is(2));
        assertThat(dto.getGroups().get(0).getName(), is("ACCOUNT230"));
        assertThat(dto.getGroups().get(0).getAccounts().size(), is(2));

        FinancialAssetsDto.Group.Account heldSince2020 = dto.getGroups().get(0).getAccounts().get(0);
        assertThat(heldSince2020.getId(), is("230.0"));
        assertThat(heldSince2020.getName(), is("fin0-0"));
        // the fixtures' latest year, 2023, holds no financial assets: every one of them was sold
        assertThat(heldSince2020.getActive(), is(false));
        assertThat(heldSince2020.getLabels().length, is(36));
        assertThat(heldSince2020.getLabels()[0], is("1/20"));
        assertThat(heldSince2020.getLabels()[12], is("1/21"));
        assertThat(heldSince2020.getLabels()[24], is("1/22"));
        assertThat(heldSince2020.getInitialValue(), is(4000));
        assertThat(heldSince2020.getDepositsSum(), is(0));
        assertThat(heldSince2020.getWithdrawalsSum(), is(0));
        assertThat(heldSince2020.getCurrentValue(), is(1000));
        assertThat(heldSince2020.getCurrentReturn(), is(BigDecimal.valueOf(-75.0)));
        assertThat(heldSince2020.getBalances()[11], is(4000));
        assertThat(heldSince2020.getBalances()[12], is(1000));

        FinancialAssetsDto.Group.Account acquiredIn2021 = dto.getGroups().get(0).getAccounts().get(1);
        assertThat(acquiredIn2021.getId(), is("230.1"));
        assertThat(acquiredIn2021.getName(), is("fin0-1"));
        // no longer held, so its timeline ends with its last transaction, in November 2021
        assertThat(acquiredIn2021.getLabels().length, is(11));
        assertThat(acquiredIn2021.getLabels()[10], is("11/21"));
        assertThat(acquiredIn2021.getLabels()[0], is("1/21"));
        assertThat(acquiredIn2021.getInitialValue(), is(2000));
        assertThat(acquiredIn2021.getDepositsSum(), is(3000));
        assertThat(acquiredIn2021.getWithdrawalsSum(), is(1000));
        assertThat(acquiredIn2021.getCurrentValue(), is(1000));
        assertThat(acquiredIn2021.getCurrentReturn(), is(BigDecimal.valueOf(-60.0)));
        assertThat(acquiredIn2021.getDeposits(), is(new Integer[]{0, 1000, 1000, 0, 0, 0, 0, 0, 0, 1000, 0}));
        assertThat(acquiredIn2021.getWithdrawals(), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 1000, 0, 0, 0}));
        assertThat(acquiredIn2021.getRevaluations(), is(new Integer[]{0, 0, 0, -1000, 0, 1000, 0, 0, 1000, 0, -1000}));
        assertThat(acquiredIn2021.getCumulativeDeposits(), is(new Integer[]{0, 1000, 2000, 2000, 2000, 2000, 2000, 2000, 2000, 3000, 3000}));

        assertThat(dto.getGroups().get(1).getName(), is("ACCOUNT231"));
        assertThat(dto.getGroups().get(1).getAccounts().size(), is(1));

        FinancialAssetsDto.Group.Account other = dto.getGroups().get(1).getAccounts().get(0);
        assertThat(other.getName(), is("fin1-0"));
        assertThat(other.getLabels().length, is(23));
        assertThat(other.getInitialValue(), is(3000));
        assertThat(other.getDepositsSum(), is(3000));
        assertThat(other.getWithdrawalsSum(), is(1000));
        assertThat(other.getCurrentValue(), is(3000));
        assertThat(other.getCurrentReturn(), is(BigDecimal.valueOf(-33.33)));
    }
}
