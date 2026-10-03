package org.kaleta.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.kaleta.persistence.api.TransactionDao;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.kaleta.framework.Generator.transaction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class TransactionServiceTest
{
    @InjectMock
    TransactionDao transactionDao;

    @Inject
    TransactionService transactionService;

    @Test
    void getMonthlyProfit_addsRevenuesAndTakesExpensesInTheirMonths()
    {
        when(transactionDao.listProfitTransactions("2024")).thenReturn(List.of(
                // revenue in January, credited to 600
                transaction("2024", "1501", 1000, "210.1", "600.0"),
                // expense in January, debited to 510
                transaction("2024", "2001", 300, "510.0", "210.1"),
                // expense in March
                transaction("2024", "0103", 50, "520.0", "210.1"),
                // moved from one expense to another: no profit either way
                transaction("2024", "1003", 20, "510.0", "520.0")));

        Integer[] profit = transactionService.getMonthlyProfit("2024");

        assertThat(profit, is(new Integer[]{700, 0, -50, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
    }

    @Test
    void getMonthlyProfit_isNoughtEveryMonthOfAYearWithoutProfit()
    {
        when(transactionDao.listProfitTransactions("2024")).thenReturn(List.of());

        assertThat(transactionService.getMonthlyProfit("2024"), is(new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
    }

    @Test
    void theQueriesPassTheirConditionsOnToTheDao()
    {
        transactionService.getTransactionsMatching("2024", "21");
        verify(transactionDao).list("2024", "21");

        transactionService.getBalanceTransactions("2024");
        verify(transactionDao).listByDescriptionMatching("2024", "");

        transactionService.getSchemaTransactions("2024", "210", "3");
        verify(transactionDao).listBySchema("2024", "210", "3");
    }
}
