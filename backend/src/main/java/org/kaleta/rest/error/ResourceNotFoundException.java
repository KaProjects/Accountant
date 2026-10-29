package org.kaleta.rest.error;

import jakarta.ws.rs.core.Response;

/** A request for something there is none of, such as a year outside the books: 404 Not Found. */
public class ResourceNotFoundException extends ApiException
{
    public ResourceNotFoundException(String detail)
    {
        super(Response.Status.NOT_FOUND, detail);
    }
}
