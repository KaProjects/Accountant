package org.kaleta.service;

import java.security.NoSuchAlgorithmException;

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
}
