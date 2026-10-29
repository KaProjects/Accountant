package org.kaleta.rest.data;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.kaleta.framework.Problems.assertInvalid;
import static org.kaleta.framework.Problems.assertNotFound;

@QuarkusTest
@TestProfile(DataSourceTestProfile.class)
public class SyncResourceTest
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
    public void syncYearTest()
    {
        String year = FakeDataSource.activeYear();

        String response = given().when()
                .get("/sync/" + year)
                .then()
                .statusCode(200)
                .contentType(ContentType.TEXT)
                .extract().response().body().asString();

        assertThat(response, containsString("year " + year + " transactions synced:"));
        assertThat(response, containsString("year " + year + " schema classes synced:"));
        assertThat(response, containsString("year " + year + " accounts synced:"));
    }

    @Test
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
    public void syncValidateDataTest()
    {
        String response = given().when()
                .get("/sync/all/validate")
                .then()
                .statusCode(200)
                .contentType(ContentType.TEXT)
                .extract().response().body().asString();

        assertSyncData(response);

        for (String year : FakeDataSource.years())
        {
            assertThat(response, containsString("year " + year + " data valid"));
        }
    }

    @Test
    public void parameterValidatorTest()
    {
        assertInvalid("/sync/2x20", "year");

        assertNotFound("/sync/2014");

        assertNotFound("/sync/" + (new GregorianCalendar().get(Calendar.YEAR) + 1));
    }


    private void assertSyncData(String response)
    {
        for (String year : FakeDataSource.years())
        {
            assertThat(response, containsString("year " + year + " transactions synced:"));
            assertThat(response, containsString("year " + year + " schema classes synced:"));
            assertThat(response, containsString("year " + year + " accounts synced:"));
        }
    }
}
