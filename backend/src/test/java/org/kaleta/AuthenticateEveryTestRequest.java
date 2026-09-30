package org.kaleta;

import io.quarkus.test.junit.callback.QuarkusTestBeforeEachCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;
import io.restassured.RestAssured;

/**
 * Installs {@link TestAuthentication} for every {@code @QuarkusTest} in the module.
 * <p>
 * Registered through {@code META-INF/services}, so the twenty-odd test classes that predate
 * authentication need no change, and a new one is authenticated by default rather than by
 * remembering to ask.
 */
public class AuthenticateEveryTestRequest implements QuarkusTestBeforeEachCallback
{
    @Override
    public void beforeEach(QuarkusTestMethodContext context)
    {
        if (RestAssured.filters().stream().noneMatch(filter -> filter instanceof TestAuthentication))
        {
            RestAssured.filters(new TestAuthentication());
        }
    }
}
