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
        YearTransactionDto dto = new YearTransactionDto();
        dto.setDate(transaction.getDate());
        dto.setAmount(String.valueOf(transaction.getAmount()));
        dto.setDebit(transaction.getDebit());
        dto.setCredit(transaction.getCredit());
        dto.setDescription(transaction.getDescription());
        return dto;
    }

    public static YearTransactionDto from(String date, String amount, String debit, String credit, String description)
    {
        YearTransactionDto dto = new YearTransactionDto();
        dto.setDate(date);
        dto.setAmount(amount);
        dto.setDebit(debit);
        dto.setCredit(credit);
        dto.setDescription(description);
        return dto;
    }

    @Override
    public int compareTo(YearTransactionDto other)
    {
        return Utils.compareDates(this.getDate(), other.getDate());
    }
}
