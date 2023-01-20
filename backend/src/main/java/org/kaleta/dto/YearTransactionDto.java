package org.kaleta.dto;

import lombok.Data;

@Data
public class YearTransactionDto
{
    private String date;
    private String description;
    private String amount;
    private String debit;
    private String credit;
}
