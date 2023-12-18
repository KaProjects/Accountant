package org.kaleta.dto;

import lombok.Data;
import org.kaleta.Utils;
import org.kaleta.entity.Transaction;

import java.util.ArrayList;
import java.util.List;

@Data
public class YearTransactionDto implements Comparable<YearTransactionDto>
{
    private String date;
    private String amount;
    private String debit;
    private String credit;
    private String description;

    public static List<YearTransactionDto> from(List<Transaction> transactions)
    {
        List<YearTransactionDto> dtoTransactions = new ArrayList<>();
        transactions.forEach(transaction -> dtoTransactions.add(YearTransactionDto.from(transaction)));
        return dtoTransactions;
    }

    public static YearTransactionDto from(Transaction transaction)
    {
        YearTransactionDto dtoTransaction = new YearTransactionDto();
        dtoTransaction.setDate(transaction.getDate());
        dtoTransaction.setAmount(String.valueOf(transaction.getAmount()));
        dtoTransaction.setDebit(transaction.getDebit());
        dtoTransaction.setCredit(transaction.getCredit());
        dtoTransaction.setDescription(transaction.getDescription());
        return dtoTransaction;
    }

    @Override
    public int compareTo(YearTransactionDto other)
    {
        return Utils.compareDates(this.getDate(), other.getDate());
    }
}
