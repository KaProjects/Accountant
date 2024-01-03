package org.kaleta.dto;

import lombok.Data;

@Data
public class YearAccountOverviewDto
{
    String id;
    String name;
    Integer initial = 0;
    Integer turnover = 0;
    Integer balance = 0;

    public static YearAccountOverviewDto from(String id, String name, Integer initial, Integer turnover, Integer balance)
    {
        YearAccountOverviewDto dto = new YearAccountOverviewDto();
        dto.setId(id);
        dto.setName(name);
        dto.setInitial(initial);
        dto.setTurnover(turnover);
        dto.setBalance(balance);
        return dto;
    }
}
