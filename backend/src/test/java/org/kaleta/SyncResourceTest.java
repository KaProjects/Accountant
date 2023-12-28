package org.kaleta;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.http.HttpStatus;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
@Disabled("must be executed separately, because rewrites test data")
public class SyncResourceTest
{
    @Test
    @EnabledIf("isDataLocationSet")
    public void syncYearTest()
    {
        String response = given().when()
                .get("/sync/2023")
                .then()
                .statusCode(200)
                .contentType(ContentType.TEXT)
                .extract().response().body().asString();

        assertThat(response, containsString("year 2023 transactions synced:"));
        assertThat(response, containsString("year 2023 schema classes synced:"));
        assertThat(response, containsString("year 2023 accounts synced:"));
    }

    @Test
    @EnabledIf("isDataLocationSet")
    public void syncAllTest()
    {
        String response = given().when()
                .get("/sync/all")
                .then()
                .statusCode(200)
                .contentType(ContentType.TEXT)
                .extract().response().body().asString();

        assertThat(response, containsString("year 2023 transactions synced:"));
        assertThat(response, containsString("year 2023 schema classes synced:"));
        assertThat(response, containsString("year 2023 accounts synced:"));
        assertThat(response, containsString("year 2022 transactions synced:"));
        assertThat(response, containsString("year 2022 schema classes synced:"));
        assertThat(response, containsString("year 2022 accounts synced:"));
        assertThat(response, containsString("year 2021 transactions synced:"));
        assertThat(response, containsString("year 2021 schema classes synced:"));
        assertThat(response, containsString("year 2021 accounts synced:"));
        assertThat(response, containsString("year 2020 transactions synced:"));
        assertThat(response, containsString("year 2020 schema classes synced:"));
        assertThat(response, containsString("year 2020 accounts synced:"));
        assertThat(response, containsString("year 2015 transactions synced:"));
        assertThat(response, containsString("year 2015 schema classes synced:"));
        assertThat(response, containsString("year 2015 accounts synced:"));
    }

    @Test
    public void parameterValidatorTest()
    {
        assertThat(given().when()
                .get("/sync/2x20")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));

        assertThat(given().when()
                .get("/sync/2014")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString(), containsString(" not found"));

        assertThat(given().when()
                .get("/sync/" + (new GregorianCalendar().get(Calendar.YEAR) + 1))
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString(), containsString(" not found"));
    }

    boolean isDataLocationSet()
    {
        return !ConfigProvider.getConfig().getValue("data.location", String.class).equals("X");
    }
}
