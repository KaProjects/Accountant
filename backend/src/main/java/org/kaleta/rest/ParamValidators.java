package org.kaleta.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ParamValidators
{
    public static void validateYear(String year)
    {
        if (!year.matches("20\\d\\d"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Year Parameter");
    }

    public static void validateDebitPrefix(String debitPrefix)
    {
        if (!debitPrefix.matches("\\d?\\d?\\d"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Debit Prefix Parameter");
    }

    public static void validateCreditPrefix(String creditPrefix)
    {
        if (!creditPrefix.matches("\\d?\\d?\\d"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Credit Prefix Parameter");
    }
}
