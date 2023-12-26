package org.kaleta.rest;

import org.kaleta.dto.CredentialsDto;
import org.kaleta.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/authenticate")
public class AuthResource
{
    @Inject
    AuthService authService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/")
    public Response authenticate(CredentialsDto credentialsDto)
    {
        return Endpoint.process(() -> {
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
        }, () -> authService.generateToken(credentialsDto.getUsername(), credentialsDto.getPassword()));
    }
}
