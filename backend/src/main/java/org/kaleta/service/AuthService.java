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
     * generate and store token for authorized user
     * @return generated token
     */
    String generateToken(String username, String password);

    /**
     * checks the validity of provided token
     * @param token
     * @return true if token is valid, false otherwise
     */
    boolean validateToken(String token);
}
