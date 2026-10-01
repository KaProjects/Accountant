package org.kaleta.rest;

import org.kaleta.service.AuthService;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import java.security.Principal;

/**
 * Requires a valid session on every request except the one that issues a session.
 * <p>
 * This filter used to be bound by name to a {@code @Secured} annotation, which made protection
 * opt-in: an endpoint was only guarded if somebody remembered to annotate it, and nothing
 * reported the ones that were not. Three of them were not, and the sync endpoints - which rebuild
 * the database from the desktop application's data - were reachable by anyone who could reach the
 * port. Being global inverts that: a new endpoint is protected by the fact of existing, and making
 * one public is a deliberate edit here.
 * <p>
 * There is deliberately no way to switch this off. It previously honoured an {@code auth.bypass}
 * property guarded by an {@code environment} check, and both were injected into fields, which
 * Quarkus resolves while building a native image; the production binary then refused to start
 * whenever the deployed values differed from the ones present at build time. Development and test
 * authenticate for real instead of asking to be excused.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter
{
    /**
     * The session travels in a cookie the browser marks HttpOnly, so no script on the page can
     * read it. It used to be handed to the page as text and kept in session storage, where any
     * injected script could have taken it.
     */
    public static final String SESSION_COOKIE = "accountant_session";

    static final String AUTHENTICATE_PATH = "authenticate";

    @Inject
    AuthService authService;

    @Override
    public void filter(ContainerRequestContext requestContext)
    {
        if (issuesSession(requestContext)) return;

        Cookie session = requestContext.getCookies().get(SESSION_COOKIE);

        if (session == null) {
            abortWithUnauthorized(requestContext, "missing session cookie");
            return;
        }

        String user = authService.authenticatedUser(session.getValue());

        if (user == null) {
            abortWithUnauthorized(requestContext, "invalid session");
        } else {
            requestContext.setSecurityContext(securityContextFor(user, requestContext));
        }
    }

    /**
     * Makes the caller's identity available to whatever handles the request. The session used to
     * be an opaque identifier that belonged to nobody, so there was no answer to the question of
     * who was asking; it is now a signed statement of exactly that.
     */
    private SecurityContext securityContextFor(String user, ContainerRequestContext requestContext)
    {
        Principal principal = () -> user;
        boolean secure = "https".equals(requestContext.getUriInfo().getRequestUri().getScheme());

        return new SecurityContext()
        {
            @Override
            public Principal getUserPrincipal() { return principal; }

            @Override
            public boolean isUserInRole(String role) { return false; }

            @Override
            public boolean isSecure() { return secure; }

            @Override
            public String getAuthenticationScheme() { return "COOKIE"; }
        };
    }

    /**
     * Only posting credentials may be done without a session. Asking whether the session is still
     * good is a GET to the same path, and that one goes through the check like everything else,
     * which is exactly how it answers the question.
     */
    private boolean issuesSession(ContainerRequestContext requestContext)
    {
        String path = requestContext.getUriInfo().getPath();
        while (path.startsWith("/")) path = path.substring(1);
        while (path.endsWith("/")) path = path.substring(0, path.length() - 1);

        return requestContext.getMethod().equals("POST") && path.equals(AUTHENTICATE_PATH);
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE, message)
                        .entity(message)
                        .build());
    }
}
