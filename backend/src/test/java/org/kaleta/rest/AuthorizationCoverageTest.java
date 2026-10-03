package org.kaleta.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;
import org.kaleta.TestAuthentication;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Asserts that a caller with no credentials is turned away from every endpoint except the ones
 * this test names as public.
 * <p>
 * Authentication used to be opt-in: a resource method was protected only if somebody remembered
 * to annotate it, nothing reported the ones that were not, and three of them were not - the sync
 * endpoints. This test is what keeps the set of public endpoints a decision rather than an
 * accident, and it fails the moment a new endpoint becomes reachable without a token.
 * <p>
 * It deliberately asserts the response rather than the presence of an annotation, so that it
 * keeps its meaning when the mechanism behind the annotation is replaced.
 * <p>
 * The suite as a whole authenticates itself through {@link org.kaleta.TestAuthentication}, which
 * this test opts out of: its entire subject is what happens to a caller who has no credentials.
 */
@QuarkusTest
public class AuthorizationCoverageTest
{
    /**
     * The only request an unauthenticated caller may make, because it is how a caller stops being
     * unauthenticated. Everything else needs a session - including the temporary sync endpoints,
     * and including the GET to this same path, which is how the page asks whether its session is
     * still good.
     */
    private static final List<String> PUBLIC_ENDPOINTS = List.of("POST /authenticate");

    /** Any value will do: authentication is decided before a parameter is ever looked at. */
    private static final Map<String, String> PARAMETER_VALUES = Map.of("year", "2023", "month", "1");

    @Test
    public void everyEndpointRefusesACallerWithoutCredentials() throws Exception
    {
        List<String> reachable = new ArrayList<>();

        for (Endpoint endpoint : endpoints())
        {
            if (isPublic(endpoint)) continue;

            Response response = given().noFiltersOfType(TestAuthentication.class)
                    .when().request(endpoint.httpMethod, endpoint.path);
            if (response.statusCode() != 401) reachable.add(endpoint + " -> " + response.statusCode());
        }

        assertThat("reachable without credentials: " + reachable, reachable, is(empty()));
    }

    /**
     * The counterpart, so that the exception list cannot quietly grow to cover everything: each
     * path named public must actually answer an unauthenticated caller. The status itself is
     * beside the point - a missing payload or an unparsable parameter is a fine answer - as long
     * as the caller was not turned away.
     */
    @Test
    public void everyEndpointDeclaredPublicAnswersACallerWithoutCredentials() throws Exception
    {
        List<String> refused = new ArrayList<>();

        for (Endpoint endpoint : endpoints())
        {
            if (!isPublic(endpoint)) continue;

            Response response = given().noFiltersOfType(TestAuthentication.class)
                    .when().request(endpoint.httpMethod, endpoint.path);
            if (response.statusCode() == 401) refused.add(endpoint.toString());
        }

        assertThat(endpoints(), is(not(empty())));
        assertThat("declared public but refused: " + refused, refused, is(empty()));
    }

    private boolean isPublic(Endpoint endpoint)
    {
        return PUBLIC_ENDPOINTS.contains(endpoint.toString());
    }

    private record Endpoint(String httpMethod, String path)
    {
        @Override
        public String toString()
        {
            return httpMethod + " " + path;
        }
    }

    /**
     * Every resource method the application publishes, found by reading the compiled classes of
     * the package rather than by keeping a list in step with them by hand.
     */
    private List<Endpoint> endpoints() throws Exception
    {
        List<Endpoint> endpoints = new ArrayList<>();

        // read from the build output: the class loader a @QuarkusTest runs in can serve the classes
        // from memory - it does once quarkus-jacoco instruments them - where there is no directory
        java.nio.file.Path directory = java.nio.file.Path.of("target", "classes")
                .resolve(AuthResource.class.getPackageName().replace('.', '/'));

        try (Stream<java.nio.file.Path> files = Files.list(directory))
        {
            for (java.nio.file.Path file : files.sorted().toList())
            {
                String name = file.getFileName().toString();
                if (!name.endsWith(".class") || name.contains("$")) continue;

                Class<?> type = Class.forName(AuthResource.class.getPackageName() + "."
                        + name.substring(0, name.length() - ".class".length()));

                Path resourcePath = type.getAnnotation(Path.class);
                if (resourcePath == null) continue;

                for (Method method : type.getDeclaredMethods())
                {
                    String httpMethod = httpMethodOf(method);
                    if (httpMethod == null) continue;

                    endpoints.add(new Endpoint(httpMethod, uri(resourcePath, method)));
                }
            }
        }
        return endpoints;
    }

    private String httpMethodOf(Method method)
    {
        for (Class<? extends Annotation> annotation : List.of(GET.class, POST.class, PUT.class, DELETE.class))
        {
            if (method.isAnnotationPresent(annotation)) return annotation.getSimpleName();
        }
        return null;
    }

    private String uri(Path resourcePath, Method method)
    {
        Path methodPath = method.getAnnotation(Path.class);
        String uri = resourcePath.value() + (methodPath == null ? "" : "/" + methodPath.value());

        for (Map.Entry<String, String> parameter : PARAMETER_VALUES.entrySet())
        {
            uri = uri.replace("{" + parameter.getKey() + "}", parameter.getValue());
        }
        uri = uri.replaceAll("\\{[^}]+}", "x").replaceAll("/{2,}", "/");

        return uri.length() > 1 && uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri;
    }
}
