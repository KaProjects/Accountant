package org.kaleta.dto;

import lombok.Data;
import org.kaleta.Utils;

@Data
public class YearAccountTransactionDto implements Comparable<YearAccountTransactionDto>
{
    private String date;
    private String debit;
    private String credit;
    private String pair;
    private String description;

    public static YearAccountTransactionDto from(String date, String debit, String credit, String pair, String description)
    {
        YearAccountTransactionDto dto = new YearAccountTransactionDto();
        dto.setDate(date);
        dto.setDebit(debit);
        dto.setCredit(credit);
        dto.setPair(pair);
        dto.setDescription(description);
        return dto;
    }

    @Override
    public int compareTo(YearAccountTransactionDto other)
    {
        // from latest
        return -Utils.compareDates(this.getDate(), other.getDate());
    }
}
