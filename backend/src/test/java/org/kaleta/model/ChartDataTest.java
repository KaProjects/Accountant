package org.kaleta.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

class ChartDataTest
{
    private static Map<String, String> namesById(Map<String, String> schemaNames)
    {
        return ChartData.getConfigs(schemaNames).stream()
                .collect(Collectors.toMap(ChartData.Config::getId, ChartData.Config::getName));
    }

    @Test
    void aChartIsNamedAfterTheSchemaOfTheLatestYear()
    {
        Map<String, String> names = namesById(Map.of("55", "institucie", "60", "pracovne", "51", "consumption"));

        assertThat(names.get("55a"), is("institucie - pracovne - Naklady"));
        assertThat(names.get("51"), is("consumption"));
    }

    @Test
    void aSchemaTheLatestYearHasNoNameForIsNamedByItsId()
    {
        Map<String, String> names = namesById(Map.of("60", "pracovne"));

        assertThat(names.get("55a"), is("55 - pracovne - Naklady"));
        assertThat(names.get("51"), is("51"));
    }

    @Test
    void aChartListsItsSchemasInTheOrderTheyAreWritten()
    {
        // the same on every run, rather than in an order the JVM shuffles each time it starts
        ChartData.Config netIncome = ChartData.getConfigs().get("ni");

        assertThat(List.copyOf(netIncome.getSchemas()), contains("60", "550", "551", "552", "631", "632", "633", "634"));
    }
}
