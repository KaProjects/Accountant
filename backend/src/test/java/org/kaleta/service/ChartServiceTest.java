package org.kaleta.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.kaleta.dto.ChartDto;

import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.kaleta.framework.Generator.transaction;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class ChartServiceTest
{
    @InjectMock
    SchemaService schemaService;
    @InjectMock
    TransactionService transactionService;

    @Inject
    ChartService chartService;

    @Test
    void aChartRunsMonthByMonthToTheCurrentMonth_withItsRunningTotal()
    {
        YearMonth now = YearMonth.now();
        String lastYear = String.valueOf(now.getYear() - 1);
        String thisYear = String.valueOf(now.getYear());
        when(schemaService.getYears()).thenReturn(List.of(lastYear, thisYear));
        when(transactionService.getMatching(any(Set.class))).thenReturn(List.of(
                // consumption: 100 in March last year, 10 in January this year
                transaction(lastYear, "1003", 100, "510.0", "210.0"),
                transaction(thisYear, "0501", 10, "510.0", "210.0")));

        ChartDto chart = chartService.getChartData("51");

        // every month of last year, and this year's up to the current one - not the months to come
        assertThat(chart.getValues().size(), is(12 + now.getMonthValue()));
        ChartDto.Value march = chart.getValues().get(2);
        assertThat(march.getLabel(), is("03/" + lastYear.substring(2)));
        assertThat(march.getBalance(), is(100));
        assertThat(march.getCumulative(), is(100));
        ChartDto.Value january = chart.getValues().get(12);
        assertThat(january.getBalance(), is(10));
        assertThat(january.getCumulative(), is(110));
    }

    @Test
    void theChartsAreNamedAfterTheLatestSchema()
    {
        when(schemaService.getLatestSchemaNames()).thenReturn(java.util.Map.of("51", "consumption"));

        assertThat(chartService.getChartConfigs().stream().map(config -> config.getId()).toList(), hasItem("51"));
    }
}
