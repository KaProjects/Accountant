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
     * sets the security token for the authenticated user
     * @param token
     */
    void setToken(String token);

    /**
     * checks the validity of provided token
     * @param token
     * @return true if token is valid, false otherwise
     */
    boolean validateToken(String token);
}
