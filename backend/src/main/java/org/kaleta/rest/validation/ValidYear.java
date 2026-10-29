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
 * A year, written with four digits. Whether the books have such a year is a separate question,
 * answered by {@link org.kaleta.rest.KnownYearFilter} with 404 Not Found.
 */
@Documented
@Constraint(validatedBy = {})
@NotNull
@Pattern(regexp = "\\d{4}")
@ReportAsSingleViolation
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidYear
{
    String message() default "must be a four-digit year";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
