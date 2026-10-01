package org.kaleta.service;

public interface AuthService
{
    /**
     * @return true if user exists, false otherwise
     */
    boolean userExists(String username);

    /**
     * @return true if user authenticated, false otherwise
     */
    boolean authenticateUser(String username, String password);

    /**
     * issues a signed session token for an authorized user
     * @return generated token
     */
    String generateToken(String username, String password);

    /**
     * @return the user the token was issued to, or null if it is not a token this application
     * signed, or it has expired
     */
    String authenticatedUser(String token);
}
