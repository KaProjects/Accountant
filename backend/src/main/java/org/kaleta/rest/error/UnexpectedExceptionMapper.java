package org.kaleta.rest.error;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

/**
 * Answers any failure nothing else answers as 500 Internal Server Error, logged in full under an
 * id the client is given.
 * <p>
 * Such a failure used to reach the container, which answered with its own JSON carrying the
 * exception and its stack trace - the inside of the server, sent to whoever asked.
 */
@Provider
public class UnexpectedExceptionMapper implements ExceptionMapper<Exception>
{
    private static final Logger LOG = Logger.getLogger(UnexpectedExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception)
    {
        Problem problem = new Problem(Response.Status.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
        problem.setErrorId(ErrorIds.next());
        LOG.errorf(exception, "Error %s: unexpected failure", problem.getErrorId());
        return problem.toResponse();
    }
}
