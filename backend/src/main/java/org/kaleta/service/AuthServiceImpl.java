package org.kaleta.service;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.security.UnauthorizedException;
import org.apache.commons.codec.digest.DigestUtils;
import org.kaleta.model.UsersConfig;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static org.kaleta.Utils.inputStreamToString;

@Service
public class AuthServiceImpl implements AuthService
{
    private String token = null;
    private long expiration;

    @Override
    public boolean userExists(String username)
    {
        try {
            JsonMapper mapper = new JsonMapper();
            String json = inputStreamToString(getClass().getClassLoader().getResourceAsStream("users.json"));
            UsersConfig config = mapper.readValue(json, UsersConfig.class);
            for (UsersConfig.User user : config.getUsers()){
                if (user.getUsername().equals(username)) {
                    return true;
                }
            }
            return false;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean authenticateUser(String username, String password)
    {
        try {
            JsonMapper mapper = new JsonMapper();
            String json = inputStreamToString(getClass().getClassLoader().getResourceAsStream("users.json"));
            UsersConfig config = mapper.readValue(json, UsersConfig.class);
            for (UsersConfig.User user : config.getUsers()){
                if (user.getUsername().equals(username)) {
                    String sha256hex = DigestUtils.sha256Hex(password);
                    return user.getHash().equals(sha256hex);
                }
            }
            throw new IllegalArgumentException("User '" + username + "' not found!");

        } catch (IOException e) {
            throw new RuntimeException(e);
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
