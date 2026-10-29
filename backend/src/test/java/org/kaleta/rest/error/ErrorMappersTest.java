package org.kaleta.rest.error;

import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class ErrorMappersTest
{
    private static Problem problemOf(Response response)
    {
        assertThat(response.getMediaType().toString(), is(Problem.MEDIA_TYPE));
        Problem problem = (Problem) response.getEntity();
        assertThat(problem.getStatus(), is(response.getStatus()));
        return problem;
    }

    @Test
    public void anUnexpectedFailureIsAnsweredWithAnIdAndNothingOfItsInternals()
    {
        Response response = new UnexpectedExceptionMapper()
                .toResponse(new IllegalStateException("/srv/data/2024/transactions.xml is broken"));

        Problem problem = problemOf(response);
        assertThat(response.getStatus(), is(500));
        assertThat(problem.getTitle(), is("Internal Server Error"));
        assertThat(problem.getErrorId(), is(notNullValue()));
        assertThat(problem.getDetail(), not(containsString("/srv/data")));
    }

    @Test
    public void anUnreadableDataSourceIsLoggedUnderAnIdAndItsPathIsNotAnswered()
    {
        Response response = new ApiExceptionMapper()
                .toResponse(new DataSourceException(new IOException("/srv/data/config.xml not found")));

        Problem problem = problemOf(response);
        assertThat(response.getStatus(), is(500));
        assertThat(problem.getErrorId(), is(notNullValue()));
        assertThat(problem.getDetail(), is("The accounting data source could not be read."));
    }

    @Test
    public void dataThatFailedItsChecksIsUnprocessableAndSaysWhatFailed()
    {
        Response response = new ApiExceptionMapper().toResponse(new InvalidDataException("year 2024 account 210.1: unknown"));

        Problem problem = problemOf(response);
        assertThat(response.getStatus(), is(422));
        assertThat(problem.getTitle(), is("Unprocessable Content"));
        assertThat(problem.getDetail(), is("year 2024 account 210.1: unknown"));
        // a refusal of the request, not a failure of the server: nothing to look up in the log
        assertThat(problem.getErrorId(), is(nullValue()));
    }

    @Test
    public void somethingMissingIsNotFound()
    {
        Response response = new ApiExceptionMapper().toResponse(new ResourceNotFoundException("There is no such year."));

        assertThat(response.getStatus(), is(404));
        assertThat(problemOf(response).getTitle(), is("Not Found"));
    }

    @Test
    public void failedCredentialsAreUnauthorized()
    {
        Response response = new ApiExceptionMapper().toResponse(new AuthenticationFailedException());

        assertThat(response.getStatus(), is(401));
        assertThat(problemOf(response).getDetail(), is("Invalid username or password."));
    }

    @Test
    public void aRefusalOfTheRestLayerKeepsItsStatus()
    {
        Response response = new WebApplicationExceptionMapper().toResponse(new NotAllowedException("GET"));

        assertThat(response.getStatus(), is(405));
        assertThat(problemOf(response).getTitle(), is("Method Not Allowed"));
    }
}
