package org.kaleta.service;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.security.UnauthorizedException;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.kaleta.model.UsersConfig;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.kaleta.Utils.inputStreamToString;

@Service
public class AuthServiceImpl implements AuthService
{
    public static final String ISSUER = "accountant";
    public static final Duration SESSION_LIFETIME = Duration.ofHours(1);

    /** HMAC-SHA256 is only as strong as its key, and a short one would weaken the signature. */
    private static final int MINIMUM_SECRET_LENGTH = 32;

    /** Overridden in development so a throwaway file stands in for the encrypted one. */
    @ConfigProperty(name = "auth.users-resource", defaultValue = "users.json")
    String usersResource;

    /**
     * Optional so that a missing secret reaches the check below and is reported as the thing to
     * set. Declared as a plain String it never got that far: an unset or empty value is converted
     * to null, and the deployment failed with the converter's own message instead.
     */
    @ConfigProperty(name = "auth.token-secret")
    Optional<String> tokenSecret;

    @Inject
    JWTParser parser;

    /**
     * Refuses to start without a signing secret, rather than starting and issuing tokens anybody
     * could forge. There is deliberately no default: a secret compiled into a public repository
     * would be no secret, so an unconfigured deployment has to fail loudly instead of quietly
     * accepting whatever it is handed.
     */
    void verifyConfiguration(@Observes StartupEvent event)
    {
        String secret = tokenSecret.orElse("");

        if (secret.isBlank())
        {
            throw new IllegalStateException("auth.token-secret is not set: the backend signs "
                    + "session tokens with it, and cannot run without one. Set AUTH_TOKEN_SECRET.");
        }
        if (secret.length() < MINIMUM_SECRET_LENGTH)
        {
            throw new IllegalStateException("auth.token-secret is too short: it must be at least "
                    + MINIMUM_SECRET_LENGTH + " characters, but is " + secret.length() + ".");
        }
    }

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
        InputStream stream = getClass().getClassLoader().getResourceAsStream(usersResource);
        if (stream == null) {
            throw new IllegalStateException("'" + usersResource + "' is not on the classpath");
        }
        try {
            return new JsonMapper().readValue(inputStreamToString(stream), UsersConfig.class).getUsers();
        } catch (IOException e) {
            throw new RuntimeException("could not read '" + usersResource + "'", e);
        }
    }

    /**
     * Issues a token that says who it belongs to and when it stops being valid, signed so that
     * neither claim can be altered.
     * <p>
     * It used to be a random identifier remembered in this object's memory, which meant one token
     * for the whole application: whoever logged in first minted it, everybody else was handed the
     * same one, it carried no identity at all, and a restart silently invalidated it. A signed
     * token has none of those problems, because the token is the record.
     */
    @Override
    public String generateToken(String username, String password)
    {
        if (!authenticateUser(username, password)) {
            throw new UnauthorizedException("User '" + username + "' has to be authorized to generate the token!");
        }
        return Jwt.issuer(ISSUER)
                .upn(username)
                .expiresIn(SESSION_LIFETIME)
                .signWithSecret(tokenSecret.orElseThrow());
    }

    @Override
    public String authenticatedUser(String token)
    {
        if (token == null || token.isBlank()) return null;

        try {
            JsonWebToken jwt = parser.verify(token, tokenSecret.orElseThrow());
            return jwt.getName();
        } catch (ParseException e) {
            return null;
        }
    }
}
