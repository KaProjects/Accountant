package org.kaleta.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** The ID of one of the accounting charts. */
@Documented
@Constraint(validatedBy = ChartIdValidator.class)
@NotNull
@ReportAsSingleViolation
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidChartId
{
    String message() default "must be the ID of a chart";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
