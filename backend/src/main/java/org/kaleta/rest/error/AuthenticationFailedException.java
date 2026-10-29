package org.kaleta.rest.error;

import jakarta.ws.rs.core.Response;

/**
 * Credentials that do not sign anybody in: 401 Unauthorized.
 * <p>
 * It says the same whether the user does not exist or the password is wrong. Telling the two
 * apart told anybody trying names which ones they had got right.
 */
public class AuthenticationFailedException extends ApiException
{
    public AuthenticationFailedException()
    {
        super(Response.Status.UNAUTHORIZED, "Invalid username or password.");
    }
}
