package org.kaleta;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import jakarta.ws.rs.core.HttpHeaders;

import static io.restassured.RestAssured.given;

/**
 * Logs the test suite in, so that every request carries a real token.
 * <p>
 * The suite used to set an {@code auth.bypass} property instead, which meant the tests never
 * exercised the code that decides whether a caller may proceed - and could therefore not tell a
 * protected endpoint from an open one. It also put the switch itself into production code, where
 * a native image resolved it at build time and refused to start when the deployed value differed.
 * Authenticating for real costs one request per application instance and removes both problems.
 * <p>
 * The credentials are the fixture ones in {@code src/test/resources/users.json}, which shadows the
 * real file on the test classpath.
 */
public class TestAuthentication implements Filter
{
    private static final String USERNAME = "user1";
    private static final String PASSWORD = "abcd";

    private static String token;

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext context)
    {
        if (issuesTokens(requestSpec)) return context.next(requestSpec, responseSpec);

        Response response = context.next(authenticated(requestSpec), responseSpec);
        if (response.statusCode() != 401) return response;

        // Each test profile runs its own application instance, and the token lives in the memory
        // of the service that issued it, so one cached from an earlier instance is a stranger.
        token = null;
        return context.next(authenticated(requestSpec), responseSpec);
    }

    private FilterableRequestSpecification authenticated(FilterableRequestSpecification requestSpec)
    {
        requestSpec.removeHeader(HttpHeaders.AUTHORIZATION);
        requestSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + token());
        return requestSpec;
    }

    private boolean issuesTokens(FilterableRequestSpecification requestSpec)
    {
        return requestSpec.getURI().contains("/authenticate");
    }

    private static synchronized String token()
    {
        if (token == null)
        {
            token = given().noFilters()
                    .contentType(ContentType.JSON)
                    .body("{\"username\":\"" + USERNAME + "\",\"password\":\"" + PASSWORD + "\"}")
                    .when().post("/authenticate")
                    .then().statusCode(200)
                    .extract().body().asString();
        }
        return token;
    }
}
