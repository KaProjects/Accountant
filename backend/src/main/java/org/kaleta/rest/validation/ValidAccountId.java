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
 * An account ID: its schema account, a dot and its semantic ID, which may carry a suffix after
 * a dash.
 */
@Documented
@Constraint(validatedBy = {})
@NotNull
@Pattern(regexp = "\\d{3}\\.\\d+(-\\d+)?")
@ReportAsSingleViolation
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAccountId
{
    String message() default "must be an account ID such as 210.1";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
