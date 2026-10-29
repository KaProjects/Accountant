package org.kaleta.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.kaleta.Utils;
import org.kaleta.dto.ChartDto;
import org.kaleta.model.ChartData;
import org.kaleta.persistence.entity.Transaction;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The accounting charts: which there are, and the month by month values of one of them over all
 * the years.
 */
@ApplicationScoped
public class ChartService
{
    @Inject
    SchemaService schemaService;

    @Inject
    TransactionService transactionService;

    /** Every chart there is, named after the latest year's schema. */
    public List<ChartData.Config> getChartConfigs()
    {
        Map<String, String> schemaNames = schemaService.getLatestSchemaNames();
        return ChartData.getConfigs(schemaNames);
    }

    /**
     * The chart of the specified id: each month's value and its running total, up to the current
     * month.
     */
    public ChartDto getChartData(String id)
    {
        Set<String> schemas = ChartData.getConfigs().get(id).getSchemas();
        List<Transaction> transactions = transactionService.getMatching(schemas);
        List<String> years = schemaService.getYears();
        ChartData data = new ChartData(transactions, years);

        ChartDto dto = new ChartDto();
        String[] labels = data.getLabels();
        Integer[] balances = data.getValues(id);
        Integer[] cumulative = Utils.toCumulativeArray(balances);
        if (id.equals("l"))
        {
            cumulative = Utils.mergeIntegerArrays(cumulative, getCumulativeProfit(years));
        }
        for (int i=0; i<labels.length; i++)
        {
            if (Integer.parseInt(labels[i].split("/")[1]) + 2000 == new GregorianCalendar().get(Calendar.YEAR)
                    && Integer.parseInt(labels[i].split("/")[0]) > new GregorianCalendar().get(Calendar.MONTH) + 1) continue;

            dto.addValue(labels[i], balances[i], cumulative[i]);
        }
        return dto;
    }

    private Integer[] getCumulativeProfit(List<String> years)
    {
        Set<String> schemas = ChartData.getConfigs().get("p").getSchemas();
        List<Transaction> transactions = transactionService.getMatching(schemas);
        ChartData data = new ChartData(transactions, years);
        return Utils.toCumulativeArray(data.getValues("p"));
    }
}
