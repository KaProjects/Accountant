package org.kaleta.rest.error;

import java.util.UUID;

/** The ids failures are logged under and answered with, so a report of one can be found in the log. */
final class ErrorIds
{
    private ErrorIds() {}

    static String next()
    {
        return UUID.randomUUID().toString();
    }
}
