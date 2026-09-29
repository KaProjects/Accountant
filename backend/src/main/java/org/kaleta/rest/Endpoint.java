package org.kaleta.rest;

import org.springframework.web.server.ResponseStatusException;

import javax.ws.rs.core.Response;
import java.util.function.Supplier;

public class Endpoint
{
    public static Response process(Runnable validators, Supplier<Object> logic) {
        try {
            validators.run();
        } catch (ResponseStatusException e) {
            return Response.status(e.getStatus().value()).entity(e.getMessage()).build();
        }
        return Response.ok().entity(logic.get()).build();
    }
}
