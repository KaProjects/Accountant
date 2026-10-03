package org.kaleta.persistence;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.kaleta.persistence.api.BudgetingDao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.kaleta.framework.Generator.budgetingPlan;
import static org.kaleta.framework.Generator.budgetingRow;

/**
 * The budgeting queries, on rows written into year 1999, where no fixture is, and rolled back
 * after each test.
 */
@QuarkusTest
class BudgetingDaoTest
{
    private static final String YEAR = "1999";

    @Inject
    EntityManager entityManager;

    @Inject
    BudgetingDao budgetingDao;

    @Test
    @TestTransaction
    void getSchema_answersTheRowsOfTheYear_andGetSchemaByIdOneOfThem()
    {
        entityManager.persist(budgetingPlan(YEAR, "i1", "all=1000"));
        entityManager.persist(budgetingRow(YEAR, "i1.1", "210.0", "600.0", "", "all=1000"));
        entityManager.flush();

        assertThat(budgetingDao.getSchema(YEAR).stream().map(row -> row.getYearId().getId()).toList(),
                containsInAnyOrder("i1", "i1.1"));
        assertThat(budgetingDao.getSchemaById(YEAR, "i1.1").getDebit(), is("210.0"));
    }
}
