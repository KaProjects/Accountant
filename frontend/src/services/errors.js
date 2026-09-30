/**
 * The human-readable part of a failed request, if there is one.
 *
 * The backend answers with plain text for the errors it raises itself, but an unexpected one is
 * answered by the container's own handler with a JSON object. Concatenating that into a message
 * rendered it as "[object Object]", which said nothing at all; its "details" carries the error id
 * and the exception, while its "stack" is far too long to show.
 */
export const errorDetail = (error) => {
    if (!error.response) return error.code + " " + error.message

    const body = error.response.data
    if (typeof body === "string") return body
    if (body && typeof body === "object") return body.details || body.message || ""
    return ""
}

/** A failed request as one line: the status, plus whatever the body managed to explain. */
export const describeResponseError = (error) => {
    if (!error.response) return errorDetail(error)

    const status = error.response.status + " " + error.response.statusText
    const detail = errorDetail(error)
    return detail ? status + ": " + detail : status
}
