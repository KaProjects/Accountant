package org.kaleta.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.kaleta.model.ChartData;

public class ChartIdValidator implements ConstraintValidator<ValidChartId, String>
{
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context)
    {
        return value == null || ChartData.getConfigs().containsKey(value);
    }
}
