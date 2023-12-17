package org.kaleta.rest;

import org.kaleta.dto.CredentialsDto;
import org.kaleta.service.AuthService;

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
        credentialsDto.validate();

        if (!authService.userExists(credentialsDto.getUsername())) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("User '" + credentialsDto.getUsername() + "' not found!").build();
        } else if (!authService.authenticateUser(credentialsDto.getUsername(), credentialsDto.getPassword())) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Credentials doesn't match!").build();
        } else {
            String token = authService.generateToken(credentialsDto.getUsername(), credentialsDto.getPassword());
            return Response.status(Response.Status.OK).entity(token).build();
        }
    }
}
