package org.kaleta.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.kaleta.dto.BudgetDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

/**
 * The budget as the service assembles it, without HTTP between: what its balance rows are made
 * of, which holds for any budget, so it is asserted on the fixtures without restating their
 * figures.
 */
@QuarkusTest
class BudgetingServiceTest
{
    @Inject
    BudgetingService budgetingService;

    private static BudgetDto.Row row(BudgetDto budget, String id)
    {
        return budget.getRows().stream().filter(row -> id.equals(row.getId())).findFirst().orElseThrow();
    }

    /**
     * The expenses beyond the mandatory ones, which have no total row of their own: their rows,
     * the ones whose id starts with "e", added up.
     */
    private static List<Integer> otherExpenses(BudgetDto budget, Function<BudgetDto.Row, List<Integer>> values)
    {
        List<Integer> total = new ArrayList<>(Collections.nCopies(12, 0));
        budget.getRows().stream()
                .filter(row -> row.getType() == BudgetDto.Row.Type.EXPENSE && row.getId().startsWith("e"))
                .forEach(row -> {
                    for (int m = 0; m < 12; m++) total.set(m, total.get(m) + values.apply(row).get(m));
                });
        return total;
    }

    private static List<Integer> minus(List<Integer> from, List<Integer> taken)
    {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < from.size(); i++) result.add(from.get(i) - taken.get(i));
        return result;
    }

    @Test
    void theBalancesAreWhatIsLeftOfTheIncomeAsEachPartOfTheExpensesIsTaken()
    {
        BudgetDto budget = budgetingService.getBudget("2019");

        BudgetDto.Row income = row(budget, "i");
        BudgetDto.Row mandatory = row(budget, "me");
        BudgetDto.Row netAfterMandatory = row(budget, "ntme");
        BudgetDto.Row budgetCashFlow = row(budget, "bcf");

        assertThat(netAfterMandatory.getActual(), is(minus(income.getActual(), mandatory.getActual())));
        assertThat(netAfterMandatory.getPlanned(), is(minus(income.getPlanned(), mandatory.getPlanned())));
        assertThat(budgetCashFlow.getActual(), is(minus(netAfterMandatory.getActual(), otherExpenses(budget, BudgetDto.Row::getActual))));
        assertThat(budgetCashFlow.getPlanned(), is(minus(netAfterMandatory.getPlanned(), otherExpenses(budget, BudgetDto.Row::getPlanned))));
    }

    @Test
    void theLastFilledMonthIsTheLastWithAnyActualIncomeOrExpense()
    {
        BudgetDto budget = budgetingService.getBudget("2019");

        int last = budget.getLastFilledMonth();
        assertThat(last, greaterThan(0));
        assertThat(last, lessThanOrEqualTo(12));
        boolean anyInLast = false;
        boolean anyAfter = false;
        for (List<Integer> actual : List.of(row(budget, "i").getActual(), row(budget, "me").getActual(),
                otherExpenses(budget, BudgetDto.Row::getActual))) {
            anyInLast |= actual.get(last - 1) != 0;
            for (int m = last; m < 12; m++) anyAfter |= actual.get(m) != 0;
        }
        assertThat(anyInLast, is(true));
        assertThat(anyAfter, is(false));
    }
}
