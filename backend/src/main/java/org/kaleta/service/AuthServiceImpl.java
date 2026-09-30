package org.kaleta.service;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.security.UnauthorizedException;
import org.apache.commons.codec.digest.DigestUtils;
import org.kaleta.model.UsersConfig;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.kaleta.Utils.inputStreamToString;

@Service
public class AuthServiceImpl implements AuthService
{
    private static final String USERS_RESOURCE = "users.json";

    private String token = null;
    private long expiration;

    @Override
    public boolean userExists(String username)
    {
        for (UsersConfig.User user : readUsers()){
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean authenticateUser(String username, String password)
    {
        for (UsersConfig.User user : readUsers()){
            if (user.getUsername().equals(username)) {
                String sha256hex = DigestUtils.sha256Hex(password);
                return user.getHash().equals(sha256hex);
            }
        }
        throw new IllegalArgumentException("User '" + username + "' not found!");
    }

    /**
     * Reads the users from the classpath.
     * <p>
     * The missing resource is reported explicitly because a native image embeds only the
     * resources it is configured to: without that configuration the stream is simply null, and
     * the reader it was handed to then failed with a bare NullPointerException that said
     * nothing about the cause.
     */
    private List<UsersConfig.User> readUsers()
    {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(USERS_RESOURCE);
        if (stream == null) {
            throw new IllegalStateException("'" + USERS_RESOURCE + "' is not on the classpath");
        }
        try {
            return new JsonMapper().readValue(inputStreamToString(stream), UsersConfig.class).getUsers();
        } catch (IOException e) {
            throw new RuntimeException("could not read '" + USERS_RESOURCE + "'", e);
        }
    }

    @Override
    public String generateToken(String username, String password)
    {
        if (!authenticateUser(username, password)) {
            throw new UnauthorizedException("User '" + username + "' has to be authorized to generate the token!");
        }
        if (token == null || expiration < new Date().getTime()){
            token = UUID.randomUUID().toString();
            expiration = new Date().getTime() + 3600000;
        }
        return this.token;
    }

    @Override
    public boolean validateToken(String token)
    {
        if (this.token == null) return false;
        if (this.expiration < new Date().getTime()) return false;
        return Objects.equals(this.token, token);
    }
}
