package org.kaleta;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.kaleta.dto.YearSchemaDto;
import org.springframework.http.HttpStatus;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class SchemaResourceTest
{
    @Test
    public void getSchemaTest()
    {
        YearSchemaDto schema = given().when()
                .get("/schema/2020")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response().jsonPath().getObject("", YearSchemaDto.class);

        assertThat(schema.getClasses().size(), is(8));

        assertThat(schema.getClasses().get(0).getId(), is("0"));
        assertThat(schema.getClasses().get(0).getName(), is("class0"));
        assertThat(schema.getClasses().get(0).getGroups().size(), is(0));

        assertThat(schema.getClasses().get(1).getId(), is("1"));
        assertThat(schema.getClasses().get(1).getName(), is("class1"));
        assertThat(schema.getClasses().get(1).getGroups().size(), is(0));

        assertThat(schema.getClasses().get(2).getId(), is("2"));
        assertThat(schema.getClasses().get(2).getName(), is("class2"));
        assertThat(schema.getClasses().get(2).getGroups().size(), is(4));
        assertThat(schema.getClasses().get(2).getGroups().get(0).getId(), is("20"));
        assertThat(schema.getClasses().get(2).getGroups().get(0).getName(), is("group20"));
        assertThat(schema.getClasses().get(2).getGroups().get(0).getAccounts().size(), is(2));
        assertThat(schema.getClasses().get(2).getGroups().get(0).getAccounts().get(0).getId(), is("200"));
        assertThat(schema.getClasses().get(2).getGroups().get(0).getAccounts().get(0).getName(), is("account200"));
        assertThat(schema.getClasses().get(2).getGroups().get(0).getAccounts().get(0).getType(), is(Constants.AccountType.A));
        assertThat(schema.getClasses().get(2).getGroups().get(0).getAccounts().get(1).getId(), is("201"));
        assertThat(schema.getClasses().get(2).getGroups().get(0).getAccounts().get(1).getName(), is("account201"));
        assertThat(schema.getClasses().get(2).getGroups().get(0).getAccounts().get(1).getType(), is(Constants.AccountType.A));
        assertThat(schema.getClasses().get(2).getGroups().get(1).getId(), is("21"));
        assertThat(schema.getClasses().get(2).getGroups().get(1).getName(), is("group21"));
        assertThat(schema.getClasses().get(2).getGroups().get(1).getAccounts().size(), is(1));
        assertThat(schema.getClasses().get(2).getGroups().get(1).getAccounts().get(0).getId(), is("210"));
        assertThat(schema.getClasses().get(2).getGroups().get(1).getAccounts().get(0).getName(), is("account210"));
        assertThat(schema.getClasses().get(2).getGroups().get(1).getAccounts().get(0).getType(), is(Constants.AccountType.A));
        assertThat(schema.getClasses().get(2).getGroups().get(2).getId(), is("22"));
        assertThat(schema.getClasses().get(2).getGroups().get(2).getName(), is("group22"));
        assertThat(schema.getClasses().get(2).getGroups().get(2).getAccounts().size(), is(1));
        assertThat(schema.getClasses().get(2).getGroups().get(2).getAccounts().get(0).getId(), is("220"));
        assertThat(schema.getClasses().get(2).getGroups().get(2).getAccounts().get(0).getName(), is("account220"));
        assertThat(schema.getClasses().get(2).getGroups().get(2).getAccounts().get(0).getType(), is(Constants.AccountType.L));
        assertThat(schema.getClasses().get(2).getGroups().get(3).getId(), is("23"));
        assertThat(schema.getClasses().get(2).getGroups().get(3).getName(), is("group23"));
        assertThat(schema.getClasses().get(2).getGroups().get(3).getAccounts().size(), is(1));
        assertThat(schema.getClasses().get(2).getGroups().get(3).getAccounts().get(0).getId(), is("230"));
        assertThat(schema.getClasses().get(2).getGroups().get(3).getAccounts().get(0).getName(), is("account230"));
        assertThat(schema.getClasses().get(2).getGroups().get(3).getAccounts().get(0).getType(), is(Constants.AccountType.A));

        assertThat(schema.getClasses().get(3).getId(), is("3"));
        assertThat(schema.getClasses().get(3).getName(), is("class3"));
        assertThat(schema.getClasses().get(3).getGroups().size(), is(2));
        assertThat(schema.getClasses().get(3).getGroups().get(0).getId(), is("30"));
        assertThat(schema.getClasses().get(3).getGroups().get(0).getName(), is("group30"));
        assertThat(schema.getClasses().get(3).getGroups().get(0).getAccounts().size(), is(0));
        assertThat(schema.getClasses().get(3).getGroups().get(1).getId(), is("31"));
        assertThat(schema.getClasses().get(3).getGroups().get(1).getName(), is("group31"));
        assertThat(schema.getClasses().get(3).getGroups().get(1).getAccounts().size(), is(0));

        assertThat(schema.getClasses().get(4).getId(), is("4"));
        assertThat(schema.getClasses().get(4).getName(), is("class4"));
        assertThat(schema.getClasses().get(4).getGroups().size(), is(1));
        assertThat(schema.getClasses().get(4).getGroups().get(0).getId(), is("40"));
        assertThat(schema.getClasses().get(4).getGroups().get(0).getName(), is("group40"));
        assertThat(schema.getClasses().get(4).getGroups().get(0).getAccounts().size(), is(1));
        assertThat(schema.getClasses().get(4).getGroups().get(0).getAccounts().get(0).getId(), is("400"));
        assertThat(schema.getClasses().get(4).getGroups().get(0).getAccounts().get(0).getName(), is("account400"));
        assertThat(schema.getClasses().get(4).getGroups().get(0).getAccounts().get(0).getType(), is(Constants.AccountType.L));

        assertThat(schema.getClasses().get(5).getId(), is("5"));
        assertThat(schema.getClasses().get(5).getName(), is("class5"));
        assertThat(schema.getClasses().get(5).getGroups().size(), is(7));
        assertThat(schema.getClasses().get(5).getGroups().get(0).getId(), is("50"));
        assertThat(schema.getClasses().get(5).getGroups().get(0).getName(), is("group50"));
        assertThat(schema.getClasses().get(5).getGroups().get(0).getAccounts().size(), is(0));
        assertThat(schema.getClasses().get(5).getGroups().get(1).getId(), is("51"));
        assertThat(schema.getClasses().get(5).getGroups().get(1).getName(), is("group51"));
        assertThat(schema.getClasses().get(5).getGroups().get(1).getAccounts().size(), is(1));
        assertThat(schema.getClasses().get(5).getGroups().get(1).getAccounts().get(0).getId(), is("510"));
        assertThat(schema.getClasses().get(5).getGroups().get(1).getAccounts().get(0).getName(), is("account510"));
        assertThat(schema.getClasses().get(5).getGroups().get(1).getAccounts().get(0).getType(), is(Constants.AccountType.E));
        assertThat(schema.getClasses().get(5).getGroups().get(2).getId(), is("52"));
        assertThat(schema.getClasses().get(5).getGroups().get(2).getName(), is("group52"));
        assertThat(schema.getClasses().get(5).getGroups().get(2).getAccounts().size(), is(1));
        assertThat(schema.getClasses().get(5).getGroups().get(2).getAccounts().get(0).getId(), is("520"));
        assertThat(schema.getClasses().get(5).getGroups().get(2).getAccounts().get(0).getName(), is("account520"));
        assertThat(schema.getClasses().get(5).getGroups().get(2).getAccounts().get(0).getType(), is(Constants.AccountType.E));
        assertThat(schema.getClasses().get(5).getGroups().get(3).getId(), is("53"));
        assertThat(schema.getClasses().get(5).getGroups().get(3).getName(), is("group53"));
        assertThat(schema.getClasses().get(5).getGroups().get(3).getAccounts().size(), is(0));
        assertThat(schema.getClasses().get(5).getGroups().get(4).getId(), is("54"));
        assertThat(schema.getClasses().get(5).getGroups().get(4).getName(), is("group54"));
        assertThat(schema.getClasses().get(5).getGroups().get(4).getAccounts().size(), is(0));
        assertThat(schema.getClasses().get(5).getGroups().get(5).getId(), is("55"));
        assertThat(schema.getClasses().get(5).getGroups().get(5).getName(), is("group55"));
        assertThat(schema.getClasses().get(5).getGroups().get(5).getAccounts().size(), is(6));
        assertThat(schema.getClasses().get(5).getGroups().get(5).getAccounts().get(0).getId(), is("550"));
        assertThat(schema.getClasses().get(5).getGroups().get(5).getAccounts().get(0).getName(), is("account550"));
        assertThat(schema.getClasses().get(5).getGroups().get(5).getAccounts().get(0).getType(), is(Constants.AccountType.E));
        assertThat(schema.getClasses().get(5).getGroups().get(5).getAccounts().get(1).getId(), is("551"));
        assertThat(schema.getClasses().get(5).getGroups().get(5).getAccounts().get(1).getName(), is("account551"));
        assertThat(schema.getClasses().get(5).getGroups().get(5).getAccounts().get(1).getType(), is(Constants.AccountType.E));
        assertThat(schema.getClasses().get(5).getGroups().get(5).getAccounts().get(2).getId(), is("552"));
        assertThat(schema.getClasses().get(5).getGroups().get(5).getAccounts().get(2).getName(), is("account552"));
        assertThat(schema.getClasses().get(5).getGroups().get(5).getAccounts().get(2).getType(), is(Constants.AccountType.E));
        assertThat(schema.getClasses().get(5).getGroups().get(5).getAccounts().get(3).getId(), is("553"));
        assertThat(schema.getClasses().get(5).getGroups().get(5).getAccounts().get(3).getName(), is("account553"));
        assertThat(schema.getClasses().get(5).getGroups().get(5).getAccounts().get(3).getType(), is(Constants.AccountType.E));
        assertThat(schema.getClasses().get(5).getGroups().get(5).getAccounts().get(4).getId(), is("554"));
        assertThat(schema.getClasses().get(5).getGroups().get(5).getAccounts().get(4).getName(), is("account554"));
        assertThat(schema.getClasses().get(5).getGroups().get(5).getAccounts().get(4).getType(), is(Constants.AccountType.E));
        assertThat(schema.getClasses().get(5).getGroups().get(5).getAccounts().get(5).getId(), is("555"));
        assertThat(schema.getClasses().get(5).getGroups().get(5).getAccounts().get(5).getName(), is("account555"));
        assertThat(schema.getClasses().get(5).getGroups().get(5).getAccounts().get(5).getType(), is(Constants.AccountType.E));
        assertThat(schema.getClasses().get(5).getGroups().get(6).getId(), is("56"));
        assertThat(schema.getClasses().get(5).getGroups().get(6).getName(), is("group56"));
        assertThat(schema.getClasses().get(5).getGroups().get(6).getAccounts().size(), is(0));

        assertThat(schema.getClasses().get(6).getId(), is("6"));
        assertThat(schema.getClasses().get(6).getName(), is("class6"));
        assertThat(schema.getClasses().get(6).getGroups().size(), is(4));
        assertThat(schema.getClasses().get(6).getGroups().get(0).getId(), is("60"));
        assertThat(schema.getClasses().get(6).getGroups().get(0).getName(), is("group60"));
        assertThat(schema.getClasses().get(6).getGroups().get(0).getAccounts().size(), is(1));
        assertThat(schema.getClasses().get(6).getGroups().get(0).getAccounts().get(0).getId(), is("600"));
        assertThat(schema.getClasses().get(6).getGroups().get(0).getAccounts().get(0).getName(), is("account600"));
        assertThat(schema.getClasses().get(6).getGroups().get(0).getAccounts().get(0).getType(), is(Constants.AccountType.R));
        assertThat(schema.getClasses().get(6).getGroups().get(1).getId(), is("61"));
        assertThat(schema.getClasses().get(6).getGroups().get(1).getName(), is("group61"));
        assertThat(schema.getClasses().get(6).getGroups().get(1).getAccounts().size(), is(0));
        assertThat(schema.getClasses().get(6).getGroups().get(2).getId(), is("62"));
        assertThat(schema.getClasses().get(6).getGroups().get(2).getName(), is("group62"));
        assertThat(schema.getClasses().get(6).getGroups().get(2).getAccounts().size(), is(0));
        assertThat(schema.getClasses().get(6).getGroups().get(3).getId(), is("63"));
        assertThat(schema.getClasses().get(6).getGroups().get(3).getName(), is("group63"));
        assertThat(schema.getClasses().get(6).getGroups().get(3).getAccounts().size(), is(4));
        assertThat(schema.getClasses().get(6).getGroups().get(3).getAccounts().get(0).getId(), is("630"));
        assertThat(schema.getClasses().get(6).getGroups().get(3).getAccounts().get(0).getName(), is("account630"));
        assertThat(schema.getClasses().get(6).getGroups().get(3).getAccounts().get(0).getType(), is(Constants.AccountType.R));
        assertThat(schema.getClasses().get(6).getGroups().get(3).getAccounts().get(1).getId(), is("631"));
        assertThat(schema.getClasses().get(6).getGroups().get(3).getAccounts().get(1).getName(), is("account631"));
        assertThat(schema.getClasses().get(6).getGroups().get(3).getAccounts().get(1).getType(), is(Constants.AccountType.R));
        assertThat(schema.getClasses().get(6).getGroups().get(3).getAccounts().get(2).getId(), is("632"));
        assertThat(schema.getClasses().get(6).getGroups().get(3).getAccounts().get(2).getName(), is("account632"));
        assertThat(schema.getClasses().get(6).getGroups().get(3).getAccounts().get(2).getType(), is(Constants.AccountType.R));
        assertThat(schema.getClasses().get(6).getGroups().get(3).getAccounts().get(3).getId(), is("633"));
        assertThat(schema.getClasses().get(6).getGroups().get(3).getAccounts().get(3).getName(), is("account633"));
        assertThat(schema.getClasses().get(6).getGroups().get(3).getAccounts().get(3).getType(), is(Constants.AccountType.R));

        assertThat(schema.getClasses().get(7).getId(), is("7"));
        assertThat(schema.getClasses().get(7).getName(), is("class7"));
        assertThat(schema.getClasses().get(7).getGroups().size(), is(1));
        assertThat(schema.getClasses().get(7).getGroups().get(0).getId(), is("70"));
        assertThat(schema.getClasses().get(7).getGroups().get(0).getName(), is("group70"));
        assertThat(schema.getClasses().get(7).getGroups().get(0).getAccounts().size(), is(1));
        assertThat(schema.getClasses().get(7).getGroups().get(0).getAccounts().get(0).getId(), is("700"));
        assertThat(schema.getClasses().get(7).getGroups().get(0).getAccounts().get(0).getName(), is("account700"));
        assertThat(schema.getClasses().get(7).getGroups().get(0).getAccounts().get(0).getType(), is(Constants.AccountType.X));
    }

    @Test
    public void parameterValidatorTest()
    {
        assertThat(given().when()
                .get("/schema/2x20")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().body().asString(), containsString("Invalid Year Parameter"));

        assertThat(given().when()
                .get("/schema/2014")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString(), containsString(" not found"));

        assertThat(given().when()
                .get("/schema/" + (new GregorianCalendar().get(Calendar.YEAR) + 1))
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString(), containsString(" not found"));
    }
}
