package org.kaleta.rest.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Comparator;
import java.util.List;

/**
 * Answers a request whose parameters failed validation as 400 Bad Request, listing each invalid
 * parameter by its name and what is wrong with it.
 */
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException>
{
    @Override
    public Response toResponse(ConstraintViolationException exception)
    {
        List<Problem.Violation> violations = exception.getConstraintViolations().stream()
                .map(violation -> new Problem.Violation(fieldOf(violation), violation.getMessage()))
                .sorted(Comparator.comparing(Problem.Violation::getField))
                .toList();

        Problem problem = new Problem(Response.Status.BAD_REQUEST, "The request is not valid.");
        problem.setViolations(violations);
        return problem.toResponse();
    }

    /**
     * The name of the value that failed: the last node of its path. The path also names the
     * resource method it was passed to, which is of no use to a client.
     */
    private static String fieldOf(ConstraintViolation<?> violation)
    {
        String field = null;
        for (Path.Node node : violation.getPropertyPath()) field = node.getName();
        return field;
    }
}
