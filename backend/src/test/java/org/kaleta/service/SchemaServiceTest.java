package org.kaleta.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.kaleta.Constants;
import org.kaleta.model.SchemaClass;
import org.kaleta.persistence.api.SchemaDao;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.kaleta.framework.Generator.schema;
import static org.mockito.Mockito.when;

@QuarkusTest
class SchemaServiceTest
{
    @InjectMock
    SchemaDao schemaDao;

    @Inject
    SchemaService schemaService;

    private void givenSchema()
    {
        when(schemaDao.list("2024")).thenReturn(List.of(
                schema("2024", "2", "Finance", ""),
                schema("2024", "21", "bank", ""),
                schema("2024", "210", "current account", "A"),
                schema("2024", "22", "credit", ""),
                schema("2024", "220", "credit card", "L"),
                schema("2024", "5", "Expenses", ""),
                schema("2024", "51", "consumption", ""),
                schema("2024", "510", "food", "E")));
    }

    @Test
    void getSchema_buildsEachClassWithItsGroupsAndAccounts()
    {
        givenSchema();

        Map<String, SchemaClass> schema = schemaService.getSchema("2024");

        assertThat(schema.keySet(), contains("2", "5"));
        SchemaClass finance = schema.get("2");
        assertThat(finance.getName(), is("Finance"));
        assertThat(finance.getGroup("21").getName(), is("bank"));
        assertThat(finance.getGroup("21").getAccount("210").getName(), is("current account"));
        assertThat(finance.getGroup("22").getAccount("220").getType(), is(Constants.AccountType.L));
        assertThat(schema.get("5").getGroup("51").getAccount("510").getType(), is(Constants.AccountType.E));
    }

    @Test
    void getSchemaNames_namesEveryElementById()
    {
        givenSchema();

        Map<String, String> names = schemaService.getSchemaNames("2024");

        assertThat(names.get("2"), is("Finance"));
        assertThat(names.get("21"), is("bank"));
        assertThat(names.get("510"), is("food"));
        assertThat(names.size(), is(8));
    }

    @Test
    void getClass_buildsOneClass()
    {
        when(schemaDao.list("2024", "2")).thenReturn(List.of(
                schema("2024", "2", "Finance", ""),
                schema("2024", "21", "bank", ""),
                schema("2024", "210", "current account", "A")));

        SchemaClass finance = schemaService.getClass("2024", "2");

        assertThat(finance.getId(), is("2"));
        assertThat(finance.getGroups().get("21").getAccounts().get("210").getName(), is("current account"));
    }

    @Test
    void getAccountType_readsTheTypeOfASchemaAccount()
    {
        when(schemaDao.getAccountById("2024", "220")).thenReturn(schema("2024", "220", "credit card", "L"));

        assertThat(schemaService.getAccountType("2024", "220"), is(Constants.AccountType.L));
    }

    @Test
    void anIdOfTheWrongLengthIsRefused()
    {
        assertThrows(IllegalArgumentException.class, () -> schemaService.getAccountType("2024", "22"));
        assertThrows(IllegalArgumentException.class, () -> schemaService.getClass("2024", "21"));
    }
}
