package org.kaleta.rest;

import org.kaleta.service.AuthService;
import org.springframework.beans.factory.annotation.Value;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.util.Objects;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter
{
    private static final String SCHEME = "Bearer";

    @Value("${environment}")
    String environment;
    @Value("${auth.bypass:false}")
    boolean bypassAuth;

    @Inject
    AuthService authService;

    @Override
    public void filter(ContainerRequestContext requestContext)
    {
        if (!Objects.equals(environment, "PRODUCTION") && bypassAuth) return;

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

    private void abortWithUnauthorized(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE, message)
                        .build());
    }
}
