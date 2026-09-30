package org.kaleta.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@RegisterForReflection
public class ChartDto
{
    List<Value> values = new ArrayList<>();

    @Data
    @RegisterForReflection
    public static class Value
    {
        String label;
        Integer balance;
        Integer cumulative;
    }

    public void addValue(String label, Integer balance, Integer cumulative)
    {
        Value value = new Value();
        value.setLabel(label);
        value.setBalance(balance);
        value.setCumulative(cumulative);
        values.add(value);
    }
}
