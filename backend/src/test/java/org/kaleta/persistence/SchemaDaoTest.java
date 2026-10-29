package org.kaleta.persistence;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.kaleta.persistence.api.SchemaDao;
import org.kaleta.persistence.entity.Schema;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.kaleta.framework.Generator.schema;

/**
 * The schema queries, on a schema written into year 1999, where no fixture is, and rolled back
 * after each test.
 */
@QuarkusTest
class SchemaDaoTest
{
    private static final String YEAR = "1999";

    @Inject
    EntityManager entityManager;

    @Inject
    SchemaDao schemaDao;

    private void givenSchema()
    {
        for (Schema schema : List.of(
                schema(YEAR, "2", "Finance", ""), schema(YEAR, "21", "bank", ""),
                schema(YEAR, "210", "current", "A"), schema(YEAR, "22", "credit", ""),
                schema(YEAR, "220", "card", "L"))) {
            entityManager.persist(schema);
        }
        entityManager.flush();
    }

    private static List<String> idsOf(List<Schema> schemas)
    {
        return schemas.stream().map(schema -> schema.getYearId().getId()).toList();
    }

    @Test
    @TestTransaction
    void list_answersTheSchemaOfTheYear_orTheBranchOfAPrefix()
    {
        givenSchema();

        assertThat(idsOf(schemaDao.list(YEAR)), containsInAnyOrder("2", "21", "210", "22", "220"));
        assertThat(idsOf(schemaDao.list(YEAR, "21")), containsInAnyOrder("21", "210"));
    }

    @Test
    @TestTransaction
    void getAccountById_answersOneSchemaAccount()
    {
        givenSchema();

        assertThat(schemaDao.getAccountById(YEAR, "220").getType(), is("L"));
    }

    @Test
    @TestTransaction
    void listLatest_answersTheSchemaOfTheLatestYearOnly()
    {
        givenSchema();

        List<Schema> latest = schemaDao.listLatest();

        String latestYear = latest.get(0).getYearId().getYear();
        assertThat(latest.stream().map(schema -> schema.getYearId().getYear()).toList(), everyItem(is(latestYear)));
        assertThat(latestYear, not(is(YEAR)));
    }

    @Test
    @TestTransaction
    void getYears_answersEveryYearThereIsASchemaOf()
    {
        givenSchema();

        assertThat(schemaDao.getYears(), hasItem(YEAR));
    }
}
