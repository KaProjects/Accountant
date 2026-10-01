package org.kaleta.rest;

import org.springframework.web.server.ResponseStatusException;

import jakarta.ws.rs.core.Response;
import java.util.function.Supplier;

public class Endpoint
{
    /**
     * Runs an endpoint that answers 200 with a body.
     */
    public static Response process(Runnable validators, Supplier<Object> logic) {
        return respond(validators, () -> Response.ok().entity(logic.get()).build());
    }

    /**
     * Runs an endpoint that builds its own response, because it needs another status or a header.
     * <p>
     * Either part may refuse by throwing {@link ResponseStatusException}, and it arrives at the
     * caller as the status it names. The body used to be outside the try, so a refusal raised
     * while producing the answer escaped to the container and came back as a 500 carrying a Java
     * stack trace. That silently disabled the one endpoint whose failure is its answer:
     * {@code /sync/all/validate} reports the years that did not pass their checks as a 406, and
     * nobody could ever see it.
     */
    public static Response respond(Runnable validators, Supplier<Response> logic) {
        try {
            validators.run();
            return logic.get();
        } catch (ResponseStatusException e) {
            return Response.status(e.getStatusCode().value()).entity(e.getMessage()).build();
        }
    }
}
