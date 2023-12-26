package org.kaleta;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.kaleta.dto.CredentialsDto;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
public class AuthResourceTest
{
    @Test
    public void authenticateTest()
    {
        given().when()
                .contentType(ContentType.JSON)
                .body(CredentialsDto.from("user1", "abcd"))
                .post("/authenticate")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.TEXT);
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
