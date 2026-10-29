package org.kaleta.rest.error;

import jakarta.ws.rs.core.Response;

/**
 * The accounting data source could not be read: 500 Internal Server Error.
 * <p>
 * The cause - which file, and why - is logged, not answered: it names paths on the server.
 */
public class DataSourceException extends ApiException
{
    public DataSourceException(Throwable cause)
    {
        super(Response.Status.INTERNAL_SERVER_ERROR, "The accounting data source could not be read.", cause);
    }
}
