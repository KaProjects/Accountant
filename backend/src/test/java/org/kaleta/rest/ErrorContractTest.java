package org.kaleta.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.kaleta.TestAuthentication;
import org.kaleta.dto.CredentialsDto;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.kaleta.framework.Problems.assertProblem;

/**
 * Every error is answered as problem details, whichever part of the application refuses: the REST
 * layer, a filter, validation or the application itself.
 */
@QuarkusTest
public class ErrorContractTest
{
    @Test
    public void aPathNothingServesIsNotFound()
    {
        assertProblem(given().when().get("/nothing/here").then(), 404, "Not Found");
    }

    @Test
    public void aMethodAPathDoesNotAllowIsRefused()
    {
        assertProblem(given().when()
                .header("Origin", TestAuthentication.ORIGIN)
                .header(CsrfFilter.CLIENT_HEADER, CsrfFilter.CLIENT_HEADER_VALUE)
                .delete("/schema/2023").then(), 405, "Method Not Allowed");
    }

    @Test
    public void aRequestFromElsewhereIsForbidden()
    {
        assertProblem(given().noFiltersOfType(TestAuthentication.class).when()
                .contentType(ContentType.JSON)
                .body(CredentialsDto.from("user1", "abcd"))
                .post("/authenticate").then(), 403, "Forbidden")
                .body("detail", containsString("did not come from the application"));
    }

    @Test
    public void aYearOutsideTheBooksIsNotFoundButAMalformedOneIsInvalid()
    {
        assertProblem(given().when().get("/schema/2014").then(), 404, "Not Found")
                .body("detail", containsString("2014"));
        assertProblem(given().when().get("/schema/20x4").then(), 400, "Bad Request");
    }
}
