package org.kaleta.rest;

import org.springframework.web.server.ResponseStatusException;

import jakarta.ws.rs.core.Response;
import java.util.function.Supplier;

public class Endpoint
{
    /**
     * Runs an endpoint, turning a {@link ResponseStatusException} raised by either part of it into
     * the response it names.
     * <p>
     * The body used to be outside the try, so a refusal raised while producing the answer escaped
     * to the container and came back as a 500 carrying a Java stack trace. That silently disabled
     * the one endpoint whose failure is its answer: {@code /sync/all/validate} reports the years
     * that did not pass their checks as a 406, and nobody could ever see it.
     */
    public static Response process(Runnable validators, Supplier<Object> logic) {
        try {
            validators.run();
            return Response.ok().entity(logic.get()).build();
        } catch (ResponseStatusException e) {
            return Response.status(e.getStatusCode().value()).entity(e.getMessage()).build();
        }
    }
}
