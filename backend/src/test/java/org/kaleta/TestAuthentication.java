package org.kaleta;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import static io.restassured.RestAssured.given;
import static org.kaleta.rest.AuthenticationFilter.SESSION_COOKIE;
import static org.kaleta.rest.CsrfFilter.CLIENT_HEADER;
import static org.kaleta.rest.CsrfFilter.CLIENT_HEADER_VALUE;

/**
 * Logs the test suite in, so that every request carries a real session cookie.
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

    /** Matches auth.frontend-origin in the test configuration. */
    public static final String ORIGIN = "http://accountant.test";

    private static final java.util.Set<String> UNSAFE_METHODS =
            java.util.Set.of("POST", "PUT", "PATCH", "DELETE");

    private static String session;

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext context)
    {
        sameOrigin(requestSpec);

        if (issuesSession(requestSpec)) return context.next(requestSpec, responseSpec);

        Response response = context.next(authenticated(requestSpec), responseSpec);
        if (response.statusCode() != 401) return response;

        // Each test profile runs its own application instance, and the session lives in the memory
        // of the service that issued it, so one cached from an earlier instance is a stranger.
        session = null;
        return context.next(authenticated(requestSpec), responseSpec);
    }

    /** The cross-site request guard turns away an unsafe request that does not look like the app. */
    private void sameOrigin(FilterableRequestSpecification requestSpec)
    {
        if (!UNSAFE_METHODS.contains(requestSpec.getMethod())) return;

        requestSpec.removeHeader("Origin");
        requestSpec.removeHeader(CLIENT_HEADER);
        requestSpec.header("Origin", ORIGIN);
        requestSpec.header(CLIENT_HEADER, CLIENT_HEADER_VALUE);
    }

    private FilterableRequestSpecification authenticated(FilterableRequestSpecification requestSpec)
    {
        requestSpec.removeCookie(SESSION_COOKIE);
        requestSpec.cookie(SESSION_COOKIE, session());
        return requestSpec;
    }

    /** Posting credentials is the one request that is made without a session. */
    private boolean issuesSession(FilterableRequestSpecification requestSpec)
    {
        return "POST".equals(requestSpec.getMethod()) && requestSpec.getURI().contains("/authenticate");
    }

    private static synchronized String session()
    {
        if (session == null)
        {
            // noFilters() also skips this one, so the login has to carry them itself.
            session = given().noFilters()
                    .header("Origin", ORIGIN)
                    .header(CLIENT_HEADER, CLIENT_HEADER_VALUE)
                    .contentType(ContentType.JSON)
                    .body("{\"username\":\"" + USERNAME + "\",\"password\":\"" + PASSWORD + "\"}")
                    .when().post("/authenticate")
                    .then().statusCode(204)
                    .extract().cookie(SESSION_COOKIE);
        }
        return session;
    }
}
