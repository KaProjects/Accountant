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
 * A schema account ID: three digits.
 */
@Documented
@Constraint(validatedBy = {})
@NotNull
@Pattern(regexp = "\\d{3}")
@ReportAsSingleViolation
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSchemaAccountId
{
    String message() default "must be a three-digit schema account ID";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
