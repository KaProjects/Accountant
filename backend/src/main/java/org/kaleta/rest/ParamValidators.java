package org.kaleta.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ParamValidators
{
    public static void validateYear(String year)
    {
        if (year == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Year Parameter is NULL");
        }
        if (!year.matches("20\\d\\d"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Year Parameter: '" + year + "'");
    }

    public static void validateDebitPrefix(String debitPrefix)
    {
        if (debitPrefix == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debit Prefix Parameter is NULL");
        }
        if (!debitPrefix.matches("\\d?\\d?\\d"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Debit Prefix Parameter: '" + debitPrefix + "'");
    }

    public static void validateCreditPrefix(String creditPrefix)
    {
        if (creditPrefix == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credit Prefix Parameter is NULL");
        }
        if (!creditPrefix.matches("\\d?\\d?\\d"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Credit Prefix Parameter: '" + creditPrefix + "'");
    }

    public static void validateMonth(String month)
    {
        if (month == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Month Parameter is NULL");
        }
        if (!month.matches("^-?\\d+$") || Integer.parseInt(month) < 1 || Integer.parseInt(month) > 12)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Month Parameter: '" + month + "'");
    }

    public static void validateBudgetId(String budgetId)
    {
        if (budgetId == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Budget ID Parameter is NULL");
        }
        if (!budgetId.startsWith("i") && !budgetId.startsWith("me") && !budgetId.startsWith("e") && !budgetId.startsWith("of"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Budget ID Parameter: '" + budgetId + "'");
    }
}
