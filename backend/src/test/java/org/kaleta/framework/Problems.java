package org.kaleta.framework;

import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Assertions on the problem details every error of the API is answered with: the status, both in
 * the response and in its body, the title that goes with it, and the problem media type.
 */
public final class Problems
{
    private Problems() {}

    /** That a GET of the path is refused as invalid, for the value of the one parameter named. */
    public static void assertInvalid(String path, String field)
    {
        assertProblem(given().when().get(path).then(), 400, "Bad Request")
                .body("violations.field", contains(field))
                .body("violations[0].message", notNullValue());
    }

    /** That a GET of the path is answered 404 Not Found, as there is nothing at it. */
    public static void assertNotFound(String path)
    {
        assertProblem(given().when().get(path).then(), 404, "Not Found")
                .body("detail", notNullValue());
    }

    /** That the response is a problem of the given status and title. */
    public static ValidatableResponse assertProblem(ValidatableResponse response, int status, String title)
    {
        return response
                .statusCode(status)
                .contentType(containsString("application/problem+json"))
                .body("status", is(status))
                .body("title", is(title));
    }
}
