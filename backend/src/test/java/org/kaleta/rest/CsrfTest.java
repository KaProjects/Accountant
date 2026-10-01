package org.kaleta.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import org.kaleta.TestAuthentication;
import org.kaleta.dto.CredentialsDto;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.kaleta.TestAuthentication.ORIGIN;
import static org.kaleta.rest.CsrfFilter.CLIENT_HEADER;
import static org.kaleta.rest.CsrfFilter.CLIENT_HEADER_VALUE;

/**
 * A state-changing request has to look like it came from this application's own page: the right
 * origin, and a header that a form on another site cannot set and a script cannot add without
 * first being granted a preflight this application never grants.
 */
@QuarkusTest
public class CsrfTest
{
    /** Sends nothing of its own, so each case controls exactly what the request carries. */
    private RequestSpecification bare()
    {
        return given().noFiltersOfType(TestAuthentication.class)
                .contentType(ContentType.JSON)
                .body(CredentialsDto.from("user1", "abcd"));
    }

    @Test
    public void anUnsafeRequestIsRefusedUnlessItCarriesBothTheOriginAndTheClientHeader()
    {
        Map<String, Map<String, String>> refused = Map.of(
                "nothing at all", Map.of(),
                "the origin alone", Map.of("Origin", ORIGIN),
                "the client header alone", Map.of(CLIENT_HEADER, CLIENT_HEADER_VALUE),
                "another site's origin", Map.of("Origin", "http://untrusted.test", CLIENT_HEADER, CLIENT_HEADER_VALUE),
                "the wrong client header", Map.of("Origin", ORIGIN, CLIENT_HEADER, "curl"));

        refused.forEach((description, headers) -> {
            RequestSpecification request = bare();
            headers.forEach(request::header);

            assertThat("expected to be refused with " + description,
                    request.when().post("/authenticate")
                            .then().statusCode(HttpStatus.FORBIDDEN.value())
                            .extract().body().asString(),
                    containsString("did not come from the application"));
        });
    }

    @Test
    public void anUnsafeRequestThatCarriesBothIsLetThrough()
    {
        bare().header("Origin", ORIGIN)
                .header(CLIENT_HEADER, CLIENT_HEADER_VALUE)
                .when().post("/authenticate")
                .then().statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void aSafeRequestIsNotSubjectToTheCheckAtAll()
    {
        // Reading changes nothing, so it is the session cookie's job alone to decide. The suite
        // supplies that, and the request carries neither origin nor client header.
        given().when().get("/schema/2023").then().statusCode(HttpStatus.OK.value());
    }
}
