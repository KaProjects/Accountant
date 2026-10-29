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
 * A month, by its number from 1 to 12.
 */
@Documented
@Constraint(validatedBy = {})
@NotNull
@Pattern(regexp = "0*([1-9]|1[0-2])")
@ReportAsSingleViolation
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMonth
{
    String message() default "must be a month number from 1 to 12";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
