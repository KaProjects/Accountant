package org.kaleta.accountant.backend.model;

import lombok.Data;

@Data
public class PdfTransactionModel {

    private String date;
    private String description;
    private String amount;
    private String debit;
    private String credit;

}
