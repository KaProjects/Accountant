package org.kaleta.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChartDto
{
    List<Value> values = new ArrayList<>();

    @Data
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
