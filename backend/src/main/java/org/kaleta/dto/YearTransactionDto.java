package org.kaleta.dto;

import lombok.Data;

@Data
public class YearTransactionDto implements Comparable<YearTransactionDto>
{
    private String date;
    private String description;
    private String amount;
    private String debit;
    private String credit;

    @Override
    public int compareTo(YearTransactionDto other)
    {
        Integer thisMonth = Integer.parseInt(this.getDate().substring(2));
        Integer otherMonth = Integer.parseInt(other.getDate().substring(2));
        if (!thisMonth.equals(otherMonth)) {
            return thisMonth - otherMonth;
        } else {
            return Integer.parseInt(this.getDate().substring(0, 2)) - Integer.parseInt(other.getDate().substring(0, 2));
        }
    }
}
