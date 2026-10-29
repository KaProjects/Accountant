package org.kaleta.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.kaleta.persistence.entity.Budgeting;
import org.kaleta.persistence.entity.YearId;

/**
 * One stored row of a year's budget, as the budget editor reads and writes it. The sizes are the
 * columns' own.
 */
@Data
@RegisterForReflection
public class BudgetSchemaDto
{
    @NotBlank
    @Size(max = 10)
    private String id;
    @NotBlank
    @Size(max = 255)
    private String name;
    @Size(max = 9)
    private String debit;
    @Size(max = 9)
    private String credit;
    @Size(max = 255)
    private String description;
    @Size(max = 255)
    private String planning;
    /**
     * Whether it is one of the rows every year has - planned, but counting what it always counts.
     * Only answered; what is saved is told by what it counts.
     */
    private boolean fixed;

    public static BudgetSchemaDto from(Budgeting row, boolean fixed)
    {
        BudgetSchemaDto dto = new BudgetSchemaDto();
        dto.setId(row.getYearId().getId());
        dto.setName(row.getName());
        dto.setDebit(row.getDebit());
        dto.setCredit(row.getCredit());
        dto.setDescription(row.getDescription());
        dto.setPlanning(row.getPlanning());
        dto.setFixed(fixed);
        return dto;
    }

    /** The row to store for the given year, with every blank value left out. */
    public Budgeting toBudgeting(String year)
    {
        YearId yearId = new YearId();
        yearId.setYear(year);
        yearId.setId(id.trim());
        Budgeting row = new Budgeting();
        row.setYearId(yearId);
        row.setName(name.trim());
        row.setDebit(orNull(debit));
        row.setCredit(orNull(credit));
        row.setDescription(orNull(description));
        row.setPlanning(orNull(planning));
        return row;
    }

    private static String orNull(String value)
    {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
