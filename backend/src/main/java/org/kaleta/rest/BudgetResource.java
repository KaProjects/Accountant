package org.kaleta.rest;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.kaleta.Utils;
import org.kaleta.dto.BudgetDto;
import org.kaleta.model.BudgetComponent;
import org.kaleta.service.BudgetingService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static org.kaleta.dto.BudgetDto.Row.Type.BALANCE;
import static org.kaleta.dto.BudgetDto.Row.Type.EXPENSE;
import static org.kaleta.dto.BudgetDto.Row.Type.EXPENSE_SUM;
import static org.kaleta.dto.BudgetDto.Row.Type.INCOME;
import static org.kaleta.dto.BudgetDto.Row.Type.INCOME_SUM;
import static org.kaleta.dto.BudgetDto.Row.Type.OF_BUDGET;
import static org.kaleta.dto.BudgetDto.Row.Type.OF_BUDGET_BALANCE;

@Path("/budget")
public class BudgetResource
{
    @Inject
    BudgetingService service;

    @GET
    @Secured
    @SecurityRequirement(name = "AccountantSecurity")
    @Produces(MediaType.APPLICATION_JSON)
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    @Path("/{year}")
    public BudgetDto getBudget(@PathParam String year)
    {
        ParamValidators.validateYear(year);

        BudgetComponent incomeComponent = service.getBudgetComponent(year, "Income", "i");
        BudgetComponent mandatoryExpensesComponent = service.getBudgetComponent(year, "Total Mandatory Expenses", "me");
        BudgetComponent expensesComponent = service.getBudgetComponent(year, "Total Expenses", "e");

        BudgetDto budgetDto = new BudgetDto(year, computeLastFilledMonth(List.of(incomeComponent, mandatoryExpensesComponent, expensesComponent)));

        constructBudgetComponentDtoRows(incomeComponent, budgetDto, INCOME);

        budgetDto.addRow(INCOME_SUM, incomeComponent.getName(), incomeComponent.getActualMonths(), incomeComponent.getPlannedMonths());

        constructBudgetComponentDtoRows(mandatoryExpensesComponent, budgetDto, EXPENSE);

        budgetDto.addRow(EXPENSE_SUM, mandatoryExpensesComponent.getName(), mandatoryExpensesComponent.getActualMonths(), mandatoryExpensesComponent.getPlannedMonths());

        Integer[] netAfterTme = Utils.subtractIntegerArrays(incomeComponent.getActualMonths(), mandatoryExpensesComponent.getActualMonths());
        Integer[] netAfterTmePlanned = Utils.subtractIntegerArrays(incomeComponent.getPlannedMonths(), mandatoryExpensesComponent.getPlannedMonths());
        budgetDto.addRow(BALANCE, "Net after TME", netAfterTme, netAfterTmePlanned);

        constructBudgetComponentDtoRows(expensesComponent, budgetDto, EXPENSE);

        Integer[] budgetCf = Utils.subtractIntegerArrays(netAfterTme, expensesComponent.getActualMonths());
        Integer[] budgetCfPlanned = Utils.subtractIntegerArrays(netAfterTmePlanned, expensesComponent.getPlannedMonths());
        budgetDto.addRow(BALANCE, "Budget CF", budgetCf, budgetCfPlanned);

        BudgetComponent ofBudgetComponent = service.getBudgetComponent(year, "Desired CF", "of");
        constructBudgetComponentDtoRows(ofBudgetComponent, budgetDto, OF_BUDGET);
        budgetDto.addRow(OF_BUDGET_BALANCE, ofBudgetComponent.getName(), ofBudgetComponent.getActualMonths(), ofBudgetComponent.getPlannedMonths());

        return budgetDto;
    }

    private Integer computeLastFilledMonth(List<BudgetComponent> components)
    {
        Boolean[] flags = new Boolean[]{false,false,false,false,false,false,false,false,false,false,false,false};
        for (BudgetComponent component: components){
            Integer[] months = component.getActualMonths();
            for(int m=0;m<months.length;m++){
                if (!flags[m] && months[m] != 0){
                    flags[m] = true;
                }
            }
        }
        int lastFilledMonth = 0;
        for (int m=0;m<12;m++){
            if (flags[m]){
                lastFilledMonth = m + 1;
            }
        }
        return lastFilledMonth;
    }

    private void constructBudgetComponentDtoRows(BudgetComponent component, BudgetDto dto, BudgetDto.Row.Type type)
    {
        for (BudgetComponent.Row row : component.getRows()){
            BudgetDto.Row rowDto = dto.addRow(type, row.getName(), row.getActualMonths(), row.getPlannedMonths());
            for (BudgetComponent.Row subRows : row.getSubRows()){
                rowDto.addSubRow(subRows.getName(), subRows.getActualMonths(), subRows.getMonthsPlanned());
            }
        }
    }
}
