package org.kaleta.rest;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

/**
 * An endpoint reports a refusal by throwing {@link ResponseStatusException}, and it must arrive at
 * the caller as the status it names.
 * <p>
 * That only used to work for the parameter validators. A refusal raised while producing the answer
 * escaped instead, and the container turned it into a 500 whose body was a Java stack trace - so
 * {@code /sync/all/validate}, whose whole purpose is to answer 406 with a report of the years that
 * failed their checks, could never deliver that report. The existing tests all used valid data, so
 * nothing noticed.
 */
public class EndpointTest
{
    @Test
    public void aRefusalFromTheValidatorsKeepsItsStatusAndMessage()
    {
        Response response = Endpoint.process(
                () -> {throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "bad parameter");},
                () -> "never reached");

        assertThat(response.getStatus(), is(400));
        assertThat(response.getEntity().toString(), containsString("bad parameter"));
    }

    @Test
    public void aRefusalFromTheBodyKeepsItsStatusAndMessage()
    {
        Response response = Endpoint.process(
                () -> {},
                () -> {throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "year 2019 data invalid");});

        assertThat(response.getStatus(), is(406));
        assertThat(response.getEntity().toString(), containsString("year 2019 data invalid"));
    }

    @Test
    public void anAnswerIsPassedThroughUntouched()
    {
        Response response = Endpoint.process(() -> {}, () -> "the answer");

        assertThat(response.getStatus(), is(200));
        assertThat(response.getEntity(), is("the answer"));
    }
}
