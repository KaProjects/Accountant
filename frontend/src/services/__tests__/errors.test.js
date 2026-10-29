import {describeResponseError} from "../../services/errors";

describe("describeResponseError", () => {

    it("says what went wrong, from the problem the backend answered with", () => {
        const error = {
            response: {
                status: 401,
                statusText: "Unauthorized",
                data: {title: "Unauthorized", status: 401, detail: "Invalid username or password."},
            },
        };

        expect(describeResponseError(error)).toBe("401 Unauthorized: Invalid username or password.");
    });

    it("names each invalid parameter of a request that failed validation", () => {
        const error = {
            response: {
                status: 400,
                statusText: "Bad Request",
                data: {
                    title: "Bad Request", status: 400, detail: "The request is not valid.",
                    violations: [
                        {field: "month", message: "must be a month number from 1 to 12"},
                        {field: "year", message: "must be a four-digit year"},
                    ],
                },
            },
        };

        expect(describeResponseError(error))
            .toBe("400 Bad Request: month must be a month number from 1 to 12; year must be a four-digit year");
    });

    it("gives the id an unexpected failure was logged under, to quote when reporting it", () => {
        const error = {
            response: {
                status: 500,
                statusText: "Internal Server Error",
                data: {title: "Internal Server Error", status: 500, detail: "An unexpected error occurred.", errorId: "48b01e9e"},
            },
        };

        expect(describeResponseError(error))
            .toBe("500 Internal Server Error: An unexpected error occurred. (error id 48b01e9e)");
    });

    it("passes a plain text body through", () => {
        const error = {
            response: {status: 502, statusText: "Bad Gateway", data: "upstream unavailable"},
        };

        expect(describeResponseError(error)).toBe("502 Bad Gateway: upstream unavailable");
    });

    it("reports the status alone when the body says nothing", () => {
        const error = {response: {status: 502, statusText: "Bad Gateway", data: ""}};

        expect(describeResponseError(error)).toBe("502 Bad Gateway");
    });

    it("falls back to the transport error when there was no response at all", () => {
        const error = {code: "ERR_NETWORK", message: "Network Error"};

        expect(describeResponseError(error)).toBe("ERR_NETWORK Network Error");
    });
});
