package org.kaleta.rest;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import org.kaleta.dto.CredentialsDto;
import org.kaleta.service.AccountService;
import org.kaleta.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("/authenticate")
public class AuthResource
{
    @Inject
    AuthService authService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
    @Path("/")
    public Response authenticate(CredentialsDto credentialsDto)
    {
        credentialsDto.validate();

        if (!authService.userExists(credentialsDto.getUsername())) {
            return Response.status(Response.Status.NOT_FOUND).entity("User '" + credentialsDto.getUsername() + "' not found!").build();
        } else if (!authService.authenticateUser(credentialsDto.getUsername(), credentialsDto.getPassword())) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Credentials doesn't match!").build();
        } else {
            return Response.status(Response.Status.OK).entity(UUID.randomUUID().toString()).build();
        }
    }
}
