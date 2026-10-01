package org.kaleta.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.kaleta.TestAuthentication;
import org.kaleta.dto.CredentialsDto;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
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
                .statusCode(HttpStatus.NO_CONTENT.value())
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
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .extract().response();

        assertThat(response.getCookie(SESSION_COOKIE), is(nullValue()));
    }

    @Test
    public void theSessionCheckAnswersWhetherTheCookieIsStillGood()
    {
        given().when()
                .get("/authenticate")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(given().noFiltersOfType(TestAuthentication.class).when()
                .get("/authenticate")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .extract().body().asString(), containsString("missing session cookie"));

        assertThat(given().noFiltersOfType(TestAuthentication.class).when()
                .cookie(SESSION_COOKIE, "not-a-session")
                .get("/authenticate")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .extract().body().asString(), containsString("invalid session"));
    }

    @Test
    public void parameterValidatorTest()
    {
        assertThat(given().when()
                .contentType(ContentType.JSON)
                .post("/authenticate")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("payload is null"));

        assertThat(given().when()
                .contentType(ContentType.JSON)
                .body(CredentialsDto.from(null, "password"))
                .post("/authenticate")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("username can't be null"));

        assertThat(given().when()
                .contentType(ContentType.JSON)
                .body(CredentialsDto.from("username", null))
                .post("/authenticate")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("password can't be null"));

        assertThat(given().when()
                .contentType(ContentType.JSON)
                .body(CredentialsDto.from("nonuser", "password"))
                .post("/authenticate")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .extract().body().asString(), containsString("User 'nonuser' not found!"));

        assertThat(given().when()
                .contentType(ContentType.JSON)
                .body(CredentialsDto.from("user1", "xxxx"))
                .post("/authenticate")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .extract().body().asString(), containsString("Credentials doesn't match!"));
    }
}
