package org.kaleta.rest.error;

import jakarta.ws.rs.core.Response;

/**
 * Data that was read but did not pass its checks, such as a synced year that does not hold
 * together: 422 Unprocessable Content. The detail says what failed.
 * <p>
 * The sync used to answer this with 406 Not Acceptable, which is about the media types a client
 * accepts and says nothing about the data.
 */
public class InvalidDataException extends ApiException
{
    /** Not among the statuses {@link Response.Status} names. */
    public static final Response.StatusType UNPROCESSABLE_CONTENT = new Response.StatusType()
    {
        @Override
        public int getStatusCode()
        {
            return 422;
        }

        @Override
        public Response.Status.Family getFamily()
        {
            return Response.Status.Family.CLIENT_ERROR;
        }

        @Override
        public String getReasonPhrase()
        {
            return "Unprocessable Content";
        }
    };

    public InvalidDataException(String detail)
    {
        super(UNPROCESSABLE_CONTENT, detail);
    }
}
