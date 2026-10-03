package org.kaleta.rest.error;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Answers the refusals of the REST layer itself - a path nothing serves, a method a path does not
 * allow, a body of a type it does not read - with the status it chose, as a {@link Problem}.
 * Without this they would fall to {@link UnexpectedExceptionMapper} and all become 500.
 */
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException>
{
    @Override
    public Response toResponse(WebApplicationException exception)
    {
        Response.StatusType status = exception.getResponse().getStatusInfo();
        return Problem.of(status, status.getReasonPhrase() + ".");
    }
}
