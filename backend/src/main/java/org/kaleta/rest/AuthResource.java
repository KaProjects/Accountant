package org.kaleta.rest;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.kaleta.dto.CredentialsDto;
import org.kaleta.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

@Path("/authenticate")
public class AuthResource
{
    /** Matches the lifetime the service gives the token it issues. */
    private static final int SESSION_SECONDS = 3600;

    @Inject
    AuthService authService;

    /**
     * Exchanges credentials for a session.
     * <p>
     * The session comes back as a cookie and never as a body, so the page is never handed anything
     * worth stealing: the browser holds it, marks it HttpOnly, and attaches it by itself.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response authenticate(CredentialsDto credentialsDto)
    {
        return Endpoint.respond(() -> {
            if (credentialsDto == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "payload is null");
            } else {
                credentialsDto.validate();
            }
            if (!authService.userExists(credentialsDto.getUsername())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User '" + credentialsDto.getUsername() + "' not found!");
            }
            if (!authService.authenticateUser(credentialsDto.getUsername(), credentialsDto.getPassword())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credentials doesn't match!");
            }
        }, () -> Response.noContent()
                .cookie(sessionCookie(authService.generateToken(
                        credentialsDto.getUsername(), credentialsDto.getPassword())))
                .build());
    }

    /**
     * Answers whether the caller still has a session, so the page can find out before it asks for
     * anything else. The authentication filter decides it: this method is only reached when the
     * session is good, and the filter answers 401 when it is not.
     */
    @GET
    @SecurityRequirement(name = "AccountantSecurity")
    @Path("/")
    public Response session()
    {
        return Response.noContent().build();
    }

    /**
     * Secure is deliberately not set: the application is served over plain HTTP on a local
     * network, and a Secure cookie would simply never be sent. That makes the transport, not the
     * cookie, the thing worth fixing.
     */
    private NewCookie sessionCookie(String token)
    {
        return new NewCookie.Builder(AuthenticationFilter.SESSION_COOKIE)
                .value(token)
                .path("/")
                .maxAge(SESSION_SECONDS)
                .httpOnly(true)
                .secure(false)
                .sameSite(NewCookie.SameSite.STRICT)
                .build();
    }
}
