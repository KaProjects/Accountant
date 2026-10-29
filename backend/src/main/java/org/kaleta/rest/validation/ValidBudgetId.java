package org.kaleta.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The ID of a budgeting row: it starts with the prefix of the part of the budget it belongs to -
 * i for income, me for mandatory expenses, e for expenses and of for off-budget.
 */
@Documented
@Constraint(validatedBy = {})
@NotNull
@Pattern(regexp = "(i|me|e|of).*")
@ReportAsSingleViolation
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBudgetId
{
    String message() default "must be the ID of a budget row";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
