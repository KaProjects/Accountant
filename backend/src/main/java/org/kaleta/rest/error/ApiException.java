package org.kaleta.rest.error;

import jakarta.ws.rs.core.Response;

/**
 * A request the application refuses, with the status that says why: thrown wherever the refusal
 * is found - a resource, a service - and answered as a {@link Problem} by
 * {@link ApiExceptionMapper}, so nothing between the two has to pass it along.
 */
public abstract class ApiException extends RuntimeException
{
    private final Response.StatusType status;

    protected ApiException(Response.StatusType status, String detail)
    {
        super(detail);
        this.status = status;
    }

    protected ApiException(Response.StatusType status, String detail, Throwable cause)
    {
        super(detail, cause);
        this.status = status;
    }

    public Response.StatusType getStatus()
    {
        return status;
    }
}
