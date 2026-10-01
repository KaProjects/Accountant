package org.kaleta.rest;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.util.Set;

/**
 * Refuses a state-changing request that another site could have caused the browser to make.
 * <p>
 * The session is a cookie, and a browser attaches a cookie to a request whether or not the page
 * that caused it belongs to this application. {@code SameSite=Strict} already stops the obvious
 * cases, and this is the layer that does not depend on the browser getting that right: a form on
 * another site cannot set a header of its own, and a script that tries has to ask permission
 * first, through a preflight this application never grants.
 * <p>
 * Today the only unsafe request is the login itself, because the web application cannot write
 * accounting data yet. It is here so that the first endpoint which can write is covered by the
 * fact of existing, rather than by somebody remembering - which is the same reason the
 * authentication filter is global.
 */
@Provider
// Runs before authentication, so a forged request is turned away before it is even read.
@Priority(Priorities.AUTHENTICATION - 100)
public class CsrfFilter implements ContainerRequestFilter
{
    public static final String CLIENT_HEADER = "X-Accountant-Client";
    public static final String CLIENT_HEADER_VALUE = "web";

    private static final Set<String> UNSAFE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    @ConfigProperty(name = "auth.frontend-origin")
    String frontendOrigin;

    @Override
    public void filter(ContainerRequestContext requestContext)
    {
        if (!UNSAFE_METHODS.contains(requestContext.getMethod())) return;

        String origin = requestContext.getHeaderString("Origin");
        String client = requestContext.getHeaderString(CLIENT_HEADER);

        if (!frontendOrigin.equals(origin) || !CLIENT_HEADER_VALUE.equals(client))
        {
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                    .entity("request did not come from the application")
                    .build());
        }
    }
}
