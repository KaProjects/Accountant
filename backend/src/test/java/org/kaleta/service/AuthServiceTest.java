package org.kaleta.service;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The session token used to be a random identifier remembered in the service's own memory. These
 * assert the properties that replaced it, none of which the old one had: the token says who it
 * belongs to, it is different for every login, it stops working when it expires, and it is worth
 * nothing to anybody who cannot sign with this application's secret.
 */
@QuarkusTest
public class AuthServiceTest
{
    private static final String SECRET = "test-token-secret-not-used-anywhere-real";

    @Inject
    AuthService authService;

    @Test
    public void theTokenSaysWhoItBelongsTo()
    {
        String token = authService.generateToken("user1", "abcd");

        assertThat(authService.authenticatedUser(token), is("user1"));
    }

    @Test
    public void everyLoginGetsItsOwnToken()
    {
        // The old implementation minted one token for the whole application and handed the same
        // one to everybody who logged in afterwards.
        String first = authService.generateToken("user1", "abcd");
        String second = authService.generateToken("user1", "abcd");

        assertThat(first, is(not(second)));
        assertThat(authService.authenticatedUser(first), is("user1"));
        assertThat(authService.authenticatedUser(second), is("user1"));
    }

    @Test
    public void nothingIsAcceptedThatThisApplicationDidNotSign()
    {
        String forged = Jwt.issuer(AuthService.ISSUER).upn("user1")
                .expiresIn(Duration.ofHours(1))
                .signWithSecret("a-different-secret-that-is-long-enough!!");

        assertThat(authService.authenticatedUser(forged), is(nullValue()));
    }

    @Test
    public void aTokenFromAnotherIssuerIsRejected()
    {
        String elsewhere = Jwt.issuer("somewhere-else").upn("user1")
                .expiresIn(Duration.ofHours(1))
                .signWithSecret(SECRET);

        assertThat(authService.authenticatedUser(elsewhere), is(nullValue()));
    }

    @Test
    public void anExpiredTokenIsRejected()
    {
        String expired = Jwt.issuer(AuthService.ISSUER).upn("user1")
                .expiresAt(Instant.now().minusSeconds(3600))
                .signWithSecret(SECRET);

        assertThat(authService.authenticatedUser(expired), is(nullValue()));
    }

    @Test
    public void aTamperedTokenIsRejected()
    {
        String token = authService.generateToken("user1", "abcd");
        String[] parts = token.split("\\.");
        String tampered = parts[0] + "." + parts[1].substring(0, parts[1].length() - 2) + "AA." + parts[2];

        assertThat(authService.authenticatedUser(tampered), is(nullValue()));
    }

    @Test
    public void rubbishIsRejectedRatherThanThrown()
    {
        assertThat(authService.authenticatedUser("not-a-token"), is(nullValue()));
        assertThat(authService.authenticatedUser(""), is(nullValue()));
        assertThat(authService.authenticatedUser(null), is(nullValue()));
    }

    @Test
    public void theBackendRefusesToStartWithoutAUsableSecret()
    {
        AuthService service = new AuthService();

        service.tokenSecret = java.util.Optional.empty();
        assertThat(assertThrows(IllegalStateException.class, () -> service.verifyConfiguration(null))
                .getMessage(), containsString("is not set"));

        service.tokenSecret = java.util.Optional.of("   ");
        assertThrows(IllegalStateException.class, () -> service.verifyConfiguration(null));

        service.tokenSecret = java.util.Optional.of("too-short");
        assertThat(assertThrows(IllegalStateException.class, () -> service.verifyConfiguration(null))
                .getMessage(), containsString("too short"));

        service.tokenSecret = java.util.Optional.of(SECRET);
        service.verifyConfiguration(null);
    }
}
