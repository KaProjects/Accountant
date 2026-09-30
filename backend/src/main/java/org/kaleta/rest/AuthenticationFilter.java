package org.kaleta.rest;

import org.kaleta.service.AuthService;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.util.List;

/**
 * Requires a valid token on every request except the ones that issue tokens.
 * <p>
 * This filter used to be bound by name to a {@code @Secured} annotation, which made protection
 * opt-in: an endpoint was only guarded if somebody remembered to annotate it, and nothing
 * reported the ones that were not. Three of them were not, and the sync endpoints - which
 * rebuild the database from the desktop application's data - were reachable by anyone who could
 * reach the port. Being global inverts that: a new endpoint is protected by the fact of existing,
 * and making one public is a deliberate edit to the list below.
 * <p>
 * There is deliberately no way to switch this off. It previously honoured an {@code auth.bypass}
 * property guarded by an {@code environment} check, and both were injected into fields, which
 * Quarkus resolves while building a native image; the production binary then refused to start
 * whenever the deployed values differed from the ones present at build time. Development and test
 * now authenticate for real instead of asking to be excused.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter
{
    private static final String SCHEME = "Bearer";

    /** Reachable without a token, because it is how a caller obtains one. */
    private static final List<String> PUBLIC_PATHS = List.of("/authenticate");

    @Inject
    AuthService authService;

    @Override
    public void filter(ContainerRequestContext requestContext)
    {
        if (isPublic(requestContext)) return;

        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.toLowerCase().startsWith(SCHEME.toLowerCase() + " "))
        {
            abortWithUnauthorized(requestContext, SCHEME + " realm=\"accountant\"");
        } else {
            String token = authorizationHeader.substring(SCHEME.length()).trim();

            if (!authService.validateToken(token)) {
                abortWithUnauthorized(requestContext, "invalid token");
            }
        }
    }

    private boolean isPublic(ContainerRequestContext requestContext)
    {
        String path = requestContext.getUriInfo().getPath();
        if (!path.startsWith("/")) path = "/" + path;

        for (String publicPath : PUBLIC_PATHS)
        {
            if (path.equals(publicPath) || path.startsWith(publicPath + "/")) return true;
        }
        return false;
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE, message)
                        .build());
    }
}
