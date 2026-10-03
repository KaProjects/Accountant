package org.kaleta.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.kaleta.TestAuthentication;
import org.kaleta.dto.CredentialsDto;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.kaleta.framework.Problems.assertProblem;
import static org.kaleta.rest.AuthenticationFilter.SESSION_COOKIE;

@QuarkusTest
public class AuthResourceTest
{
    @Test
    public void authenticateSetsAnHttpOnlySessionCookieAndReturnsNoBody()
    {
        Response response = given().when()
                .contentType(ContentType.JSON)
                .body(CredentialsDto.from("user1", "abcd"))
                .post("/authenticate")
                .then()
                .statusCode(204)
                .extract().response();

        // Nothing the page can read: the session is the browser's to hold and attach, which is
        // what stops an injected script from ever getting hold of it.
        assertThat(response.body().asString(), is(""));

        io.restassured.http.Cookie cookie = response.getDetailedCookie(SESSION_COOKIE);
        assertThat(cookie, is(not(nullValue())));
        assertThat(cookie.isHttpOnly(), is(true));
        assertThat(cookie.getSameSite(), is("Strict"));
        assertThat(cookie.getPath(), is("/"));
        assertThat(cookie.isSecured(), is(false));
        assertThat(cookie.getValue(), is(not(nullValue())));
    }

    @Test
    public void aRejectedLoginSetsNoCookie()
    {
        Response response = given().when()
                .contentType(ContentType.JSON)
                .body(CredentialsDto.from("user1", "xxxx"))
                .post("/authenticate")
                .then()
                .statusCode(401)
                .extract().response();

        assertThat(response.getCookie(SESSION_COOKIE), is(nullValue()));
    }

    @Test
    public void theSessionCheckAnswersWhetherTheCookieIsStillGood()
    {
        given().when()
                .get("/authenticate")
                .then()
                .statusCode(204);

        assertProblem(given().noFiltersOfType(TestAuthentication.class).when()
                .get("/authenticate")
                .then(), 401, "Unauthorized")
                .header("WWW-Authenticate", containsString("Cookie"))
                .body("detail", containsString("no session"));

        assertProblem(given().noFiltersOfType(TestAuthentication.class).when()
                .cookie(SESSION_COOKIE, "not-a-session")
                .get("/authenticate")
                .then(), 401, "Unauthorized")
                .body("detail", containsString("not valid"));
    }

    @Test
    public void parameterValidatorTest()
    {
        assertProblem(given().when()
                .contentType(ContentType.JSON)
                .post("/authenticate")
                .then(), 400, "Bad Request")
                .body("violations.field", contains("credentialsDto"));

        assertProblem(given().when()
                .contentType(ContentType.JSON)
                .body(CredentialsDto.from(null, "password"))
                .post("/authenticate")
                .then(), 400, "Bad Request")
                .body("violations.field", contains("username"));

        assertProblem(given().when()
                .contentType(ContentType.JSON)
                .body(CredentialsDto.from("username", null))
                .post("/authenticate")
                .then(), 400, "Bad Request")
                .body("violations.field", contains("password"));
    }

    @Test
    public void aRejectedLoginDoesNotSayWhetherTheUserExists()
    {
        // naming which of the two was wrong told anybody trying names which ones exist
        String unknownUser = given().when()
                .contentType(ContentType.JSON)
                .body(CredentialsDto.from("nonuser", "password"))
                .post("/authenticate")
                .then()
                .statusCode(401)
                .extract().path("detail");

        String wrongPassword = given().when()
                .contentType(ContentType.JSON)
                .body(CredentialsDto.from("user1", "xxxx"))
                .post("/authenticate")
                .then()
                .statusCode(401)
                .extract().path("detail");

        assertThat(unknownUser, is("Invalid username or password."));
        assertThat(wrongPassword, is(unknownUser));
    }
}
