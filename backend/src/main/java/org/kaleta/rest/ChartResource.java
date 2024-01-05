package org.kaleta.rest;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.Utils;
import org.kaleta.dto.ChartDto;
import org.kaleta.entity.Transaction;
import org.kaleta.model.ChartData;
import org.kaleta.service.SchemaService;
import org.kaleta.service.TransactionService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/chart")
public class ChartResource
{
    @Inject
    SchemaService schemaService;
    @Inject
    TransactionService transactionService;

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/config")
    public Response getChartConfigs()
    {
        return Endpoint.process(() -> {}, () -> {
            Map<String, String> schemaNames = schemaService.getLatestSchemaNames();
            return ChartData.getConfigs(schemaNames);
        });
    }

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/data/{id}")
    public Response getChartData(@PathParam String id)
    {
        return Endpoint.process(() -> {
            ParamValidators.validateChartId(id);
        }, () -> {
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
                dto.addValue(labels[i], balances[i], cumulative[i]);
            }
            return dto;
        });
    }

    private Integer[] getCumulativeProfit(List<String> years)
    {
        Set<String> schemas = ChartData.getConfigs().get("p").getSchemas();
        List<Transaction> transactions = transactionService.getMatching(schemas);
        ChartData data = new ChartData(transactions, years);
        return Utils.toCumulativeArray(data.getValues("p"));
    }
}
