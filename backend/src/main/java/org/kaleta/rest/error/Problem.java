package org.kaleta.rest.error;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Data;

import java.util.List;

/**
 * The body of every error the API answers with, in the shape of RFC 9457 problem details: a short
 * {@code title} that is the same for every problem of its kind, the HTTP {@code status}, and a
 * {@code detail} saying what went wrong with this request. A request that failed validation also
 * lists its {@code violations}, one per invalid value; an unexpected failure carries the
 * {@code errorId} it was logged under, and nothing of its internals.
 * <p>
 * The errors used to be plain text in several shapes - a sentence from the resource, the message
 * of whatever exception got out, or the container's own JSON with a stack trace in it - so a
 * client could only show them, not read them.
 */
@Data
@RegisterForReflection
public class Problem
{
    public static final String MEDIA_TYPE = "application/problem+json";

    private String title;
    private int status;
    private String detail;
    private List<Violation> violations;
    private String errorId;

    /** One value of a request that is not valid, and why. */
    @Data
    @RegisterForReflection
    public static class Violation
    {
        private String field;
        private String message;

        public Violation() {}

        public Violation(String field, String message)
        {
            this.field = field;
            this.message = message;
        }
    }

    public Problem() {}

    public Problem(Response.StatusType status, String detail)
    {
        this.title = status.getReasonPhrase();
        this.status = status.getStatusCode();
        this.detail = detail;
    }

    /** This problem as the response to send. */
    public Response toResponse()
    {
        return Response.status(status).type(MediaType.valueOf(MEDIA_TYPE)).entity(this).build();
    }

    /** The response for a problem of the given status, saying the given detail. */
    public static Response of(Response.StatusType status, String detail)
    {
        return new Problem(status, detail).toResponse();
    }
}
