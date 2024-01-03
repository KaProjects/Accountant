package org.kaleta.dto;

import lombok.Data;

@Data
public class YearAccountOverviewDto
{
    private String id;
    private String name;
    private Integer initial = 0;
    private Integer turnover = 0;
    private Integer balance = 0;

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
