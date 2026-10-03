package org.kaleta.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.kaleta.dto.ViewDto;
import org.kaleta.persistence.entity.Transaction;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.kaleta.framework.Generator.account;
import static org.kaleta.framework.Generator.transaction;
import static org.mockito.Mockito.when;

@QuarkusTest
class ViewServiceTest
{
    @InjectMock
    TransactionService transactionService;
    @InjectMock
    AccountService accountService;
    @InjectMock
    SchemaService schemaService;

    @Inject
    ViewService viewService;

    @Test
    void getVacationMap_groupsTransactionsByTheVacationTheyAreTaggedWith()
    {
        Transaction flight = transaction("2024", "0105", 300, "520.0", "210.1", "flight vac=rome");
        Transaction hotel = transaction("2024", "0305", 200, "520.0", "210.1", "vac=rome hotel");
        Transaction ski = transaction("2024", "1002", 150, "520.0", "210.1", "vac=alps");
        when(transactionService.getTransactionsMatchingDescription("2024", "vac=")).thenReturn(List.of(flight, hotel, ski));

        Map<String, List<Transaction>> vacations = viewService.getVacationMap("2024");

        assertThat(vacations.keySet(), containsInAnyOrder("rome", "alps"));
        assertThat(vacations.get("rome"), contains(flight, hotel));
    }

    @Test
    void getViewMap_takesAViewFromTheDescriptionOrFromAnAccountTaggedWithIt()
    {
        Transaction tagged = transaction("2024", "0105", 30, "510.0", "210.1", "view=car fuel");
        Transaction onAccount = transaction("2024", "0205", 80, "531.2", "210.1", "service");
        Transaction other = transaction("2024", "0305", 10, "510.0", "210.1", "bread");
        when(transactionService.getBalanceTransactions("2024")).thenReturn(List.of(tagged, onAccount, other));
        when(accountService.listMatchingMetadata("2024", "view=")).thenReturn(List.of(account("2024", "531.2", "repairs", "view=car")));

        Map<String, List<Transaction>> views = viewService.getViewMap("2024");

        assertThat(views.keySet(), contains("car"));
        assertThat(views.get("car"), contains(tagged, onAccount));
    }

    @Test
    void getViews_sumsTheExpensesAndMarksHowEachTransactionCounts()
    {
        when(transactionService.getBalanceTransactions("2024")).thenReturn(List.of(
                // an expense
                transaction("2024", "0105", 100, "510.0", "210.1", "view=car fuel"),
                // money back
                transaction("2024", "0205", 30, "210.1", "510.0", "view=car refund"),
                // moved from one expense to another
                transaction("2024", "0305", 20, "520.0", "510.0", "view=car reclassified")));
        when(accountService.listMatchingMetadata("2024", "view=")).thenReturn(List.of());
        when(accountService.getAccountNamesMap("2024")).thenReturn(Map.of("510.0", "fuel", "520.0", "service", "210.1", "bank"));
        when(schemaService.getSchemaNames("2024")).thenReturn(Map.of("510", "consumption", "520", "services"));

        ViewDto.View car = viewService.getViews("2024").getViews().get(0);

        assertThat(car.getName(), is("car"));
        assertThat(car.getExpenses(), is("70"));
        assertThat(car.getTransactions().stream().map(ViewDto.View.Transaction::getAmount).toList(), contains("100", "-30", "~20"));
        // the tag is taken out of the description
        assertThat(car.getTransactions().get(0).getDescription().trim(), is("fuel"));
        assertThat(car.getTransactions().get(0).getDebit(), is("fuel"));
        Map<String, Integer> chart = car.getChartData().stream()
                .collect(Collectors.toMap(ViewDto.View.ChartData::getName, ViewDto.View.ChartData::getValue));
        assertThat(chart.get("consumption"), is(100 - 30 - 20));
        assertThat(chart.get("services"), is(20));
    }
}
