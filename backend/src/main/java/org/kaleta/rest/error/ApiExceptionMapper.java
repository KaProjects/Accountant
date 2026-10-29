package org.kaleta.rest.error;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

/** Answers a refusal the application raised itself as the {@link Problem} it describes. */
@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException>
{
    private static final Logger LOG = Logger.getLogger(ApiExceptionMapper.class);

    @Override
    public Response toResponse(ApiException exception)
    {
        Problem problem = new Problem(exception.getStatus(), exception.getMessage());
        if (exception.getCause() != null) {
            // a failure of the server rather than of the request: what the client is told is not
            // enough to find it again, so it is logged under an id the client is given
            problem.setErrorId(ErrorIds.next());
            LOG.errorf(exception, "Error %s: %s", problem.getErrorId(), exception.getMessage());
        }
        return problem.toResponse();
    }
}
