package org.kaleta.rest;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.kaleta.rest.error.Problem;

import java.time.Year;

/**
 * Answers a request for a year outside the books with 404 Not Found, for every endpoint with a
 * {@code year} in its path.
 * <p>
 * The books start in {@value #FIRST_YEAR} and cannot hold a year that has not begun. A year that
 * is not written as one at all is a different matter - the request is malformed, not asking for
 * something missing - and is left to {@link org.kaleta.rest.validation.ValidYear}, which answers
 * 400 Bad Request. Every endpoint used to make both checks itself.
 * <p>
 * Runs after authentication, so a caller without a session learns nothing about which years exist.
 */
@Provider
@Priority(Priorities.USER)
public class KnownYearFilter implements ContainerRequestFilter
{
    public static final int FIRST_YEAR = 2015;

    @Override
    public void filter(ContainerRequestContext requestContext)
    {
        String year = requestContext.getUriInfo().getPathParameters().getFirst("year");
        if (year == null || !year.matches("\\d{4}")) return;

        int value = Integer.parseInt(year);
        if (value < FIRST_YEAR || value > Year.now().getValue()) {
            requestContext.abortWith(Problem.of(Response.Status.NOT_FOUND,
                    "There are no books for the year " + year + "."));
        }
    }
}
