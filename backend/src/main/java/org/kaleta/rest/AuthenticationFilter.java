package org.kaleta.rest;

import org.kaleta.service.AuthService;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.Objects;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter
{
    private static final String SCHEME = "Bearer";

    @Value("${environment}")
    private String environment;
    @Value("${auth.bypass:false}")
    private boolean bypassAuth;

    @Inject
    AuthService authService;

    @Override
    public void filter(ContainerRequestContext requestContext)
    {
        if (Objects.equals(environment, "DEVEL") && bypassAuth) return;

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
