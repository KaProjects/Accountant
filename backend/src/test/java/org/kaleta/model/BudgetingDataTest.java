package org.kaleta.model;

import org.junit.jupiter.api.Test;
import org.kaleta.entity.Budgeting;
import org.kaleta.entity.Transaction;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.kaleta.framework.Generator.budgetingPlan;
import static org.kaleta.framework.Generator.budgetingRow;
import static org.kaleta.framework.Generator.transaction;

/**
 * Covers how a budget row turns its planning string and its matching transactions
 * into the twelve monthly figures the budgeting view renders.
 */
class BudgetingDataTest
{
    private static final String YEAR = "2024";

    private static BudgetComponent componentOf(List<Budgeting> schema, List<Transaction> transactions)
    {
        return new BudgetingData(schema, transactions).getBudgetComponent("e", "Expenses");
    }

    @Test
    void planningWithAllAppliesTheSameAmountToEveryMonth()
    {
        BudgetComponent component = componentOf(
                List.of(budgetingRow(YEAR, "e1", "510.0", "210.0", null, "all=1500")),
                List.of());

        assertThat(component.getPlannedMonths(),
                is(arrayContaining(1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500)));
    }

    @Test
    void planningWithTwelvePipedValuesMapsPositionally()
    {
        BudgetComponent component = componentOf(
                List.of(budgetingRow(YEAR, "e1", "510.0", "210.0", null, "1|2|3|4|5|6|7|8|9|10|11|12")),
                List.of());

        assertThat(component.getPlannedMonths(), is(arrayContaining(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
    }

    @Test
    void absentPlanningIsTreatedAsZero()
    {
        BudgetComponent component = componentOf(
                List.of(budgetingRow(YEAR, "e1", "510.0", "210.0", null, null)),
                List.of());

        assertThat(component.getPlannedMonths(),
                is(arrayContaining(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
    }

    @Test
    void planningThatIsNeitherFormIsRejected()
    {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> componentOf(
                        List.of(budgetingRow(YEAR, "e1", "510.0", "210.0", null, "1|2|3")),
                        List.of()));

        assertThat(e.getMessage(), containsString("unrecognized planning value"));
    }

    @Test
    void actualsAreBucketedByMonthFromTheDdmmDate()
    {
        BudgetComponent component = componentOf(
                List.of(budgetingRow(YEAR, "e1", "510.0", "210.0", null, "all=0")),
                List.of(transaction(YEAR, "0101", 100, "510.0", "210.0"),
                        transaction(YEAR, "1501", 50, "510.0", "210.0"),
                        transaction(YEAR, "0203", 300, "510.0", "210.0")));

        assertThat(component.getActualMonths(),
                is(arrayContaining(150, 0, 300, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
    }

    @Test
    void anAccountIdWithoutADotMatchesByPrefix()
    {
        BudgetComponent component = componentOf(
                List.of(budgetingRow(YEAR, "e1", "510%", "210%", null, "all=0")),
                List.of(transaction(YEAR, "0101", 100, "510.0", "210.0"),
                        transaction(YEAR, "0101", 200, "510.7", "210.3"),
                        transaction(YEAR, "0101", 400, "520.0", "210.0")));

        assertThat(component.getActualMonths()[0], is(300));
    }

    @Test
    void anAccountIdWithADotMatchesExactly()
    {
        BudgetComponent component = componentOf(
                List.of(budgetingRow(YEAR, "e1", "510.0", "210.0", null, "all=0")),
                List.of(transaction(YEAR, "0101", 100, "510.0", "210.0"),
                        transaction(YEAR, "0101", 200, "510.7", "210.0")));

        assertThat(component.getActualMonths()[0], is(100));
    }

    @Test
    void aDescriptionFiltersTheMatchingTransactions()
    {
        BudgetComponent component = componentOf(
                List.of(budgetingRow(YEAR, "e1", "510%", "210%", "vac", "all=0")),
                List.of(transaction(YEAR, "0101", 100, "510.0", "210.0", "summer vacation"),
                        transaction(YEAR, "0101", 200, "510.0", "210.0", "groceries")));

        assertThat(component.getActualMonths()[0], is(100));
    }

    @Test
    void aDescriptionPrefixedWithBangExcludesTheMatchingTransactions()
    {
        BudgetComponent component = componentOf(
                List.of(budgetingRow(YEAR, "e1", "510%", "210%", "!vac", "all=0")),
                List.of(transaction(YEAR, "0101", 100, "510.0", "210.0", "summer vacation"),
                        transaction(YEAR, "0101", 200, "510.0", "210.0", "groceries")));

        assertThat(component.getActualMonths()[0], is(200));
    }

    @Test
    void whenDebitEqualsCreditTheRowNetsMovementsBothWays()
    {
        // a row pointed at the same account on both sides reports debits minus credits
        BudgetComponent component = componentOf(
                List.of(budgetingRow(YEAR, "e1", "520.0", "520.0", null, "all=0")),
                List.of(transaction(YEAR, "0101", 500, "520.0", "210.0"),
                        transaction(YEAR, "0101", 120, "210.0", "520.0")));

        assertThat(component.getActualMonths()[0], is(380));
    }

    @Test
    void aGroupRowAggregatesItsSubRows()
    {
        BudgetComponent component = componentOf(
                List.of(budgetingPlan(YEAR, "e1", "all=1000"),
                        budgetingRow(YEAR, "e1.1", "510.0", "210.0", null, null),
                        budgetingRow(YEAR, "e1.2", "511.0", "210.0", null, null)),
                List.of(transaction(YEAR, "0101", 100, "510.0", "210.0"),
                        transaction(YEAR, "0101", 250, "511.0", "210.0")));

        assertThat(component.getActualMonths()[0], is(350));
        // the group's own planning wins over the sub-rows, which have none
        assertThat(component.getPlannedMonths()[0], is(1000));
    }

    @Test
    void severalGroupRowsAreSummedIntoTheComponentTotal()
    {
        BudgetComponent component = componentOf(
                List.of(budgetingRow(YEAR, "e1", "510.0", "210.0", null, "all=100"),
                        budgetingRow(YEAR, "e2", "511.0", "210.0", null, "all=200")),
                List.of(transaction(YEAR, "0101", 10, "510.0", "210.0"),
                        transaction(YEAR, "0101", 20, "511.0", "210.0")));

        assertThat(component.getPlannedMonths()[0], is(300));
        assertThat(component.getActualMonths()[0], is(30));
    }

    @Test
    void anEmptyComponentReportsTwelveZeroes()
    {
        BudgetComponent component = componentOf(List.of(), List.of());

        assertThat(component.getActualMonths(), is(arrayContaining(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
        assertThat(component.getPlannedMonths(), is(arrayContaining(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
    }
}
