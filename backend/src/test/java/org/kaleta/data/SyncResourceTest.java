package org.kaleta.data;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.http.HttpStatus;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
public class SyncResourceTest
{
    @Test
    @EnabledIf(value = "isDataLocationSet", disabledReason = "set data.location in test/resources/application.properties")
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
    @EnabledIf(value = "isDataLocationSet", disabledReason = "set data.location in test/resources/application.properties")
    public void syncAllTest()
    {
        String response = given().when()
                .get("/sync/all")
                .then()
                .statusCode(200)
                .contentType(ContentType.TEXT)
                .extract().response().body().asString();

        assertSyncData(response);
    }

    @Test
    @EnabledIf(value = "isDataLocationSet", disabledReason = "set data.location in test/resources/application.properties")
    public void syncValidateDataTest()
    {
        String response = given().when()
                .get("/sync/all/validate")
                .then()
                .statusCode(200)
                .contentType(ContentType.TEXT)
                .extract().response().body().asString();

        assertSyncData(response);

        assertThat(response, containsString("year 2015 data valid"));
        assertThat(response, containsString("year 2016 data valid"));
        assertThat(response, containsString("year 2017 data valid"));
        assertThat(response, containsString("year 2018 data valid"));
        assertThat(response, containsString("year 2019 data valid"));
        assertThat(response, containsString("year 2020 data valid"));
        assertThat(response, containsString("year 2021 data valid"));
        assertThat(response, containsString("year 2022 data valid"));
        assertThat(response, containsString("year 2023 data valid"));
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

    private void assertSyncData(String response)
    {
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
        assertThat(response, containsString("year 2019 transactions synced:"));
        assertThat(response, containsString("year 2019 schema classes synced:"));
        assertThat(response, containsString("year 2019 accounts synced:"));
        assertThat(response, containsString("year 2018 transactions synced:"));
        assertThat(response, containsString("year 2018 schema classes synced:"));
        assertThat(response, containsString("year 2018 accounts synced:"));
        assertThat(response, containsString("year 2017 transactions synced:"));
        assertThat(response, containsString("year 2017 schema classes synced:"));
        assertThat(response, containsString("year 2017 accounts synced:"));
        assertThat(response, containsString("year 2016 transactions synced:"));
        assertThat(response, containsString("year 2016 schema classes synced:"));
        assertThat(response, containsString("year 2016 accounts synced:"));
        assertThat(response, containsString("year 2015 transactions synced:"));
        assertThat(response, containsString("year 2015 schema classes synced:"));
        assertThat(response, containsString("year 2015 accounts synced:"));
    }
}
