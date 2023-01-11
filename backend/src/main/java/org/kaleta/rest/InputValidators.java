package org.kaleta.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InputValidators {

    public static void validateYear(String year){
        if (!year.matches("20\\d\\d"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Year Parameter");
    }
}
