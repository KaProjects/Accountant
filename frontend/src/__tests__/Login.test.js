import {describeLoginError} from "../components/Login";

describe("describeLoginError", () => {

    it("uses the details of the container's own error object", () => {
        // What a 500 actually looks like: the backend never reached its own error handling, so
        // the container answered with a JSON object. Concatenating it showed "[object Object]".
        const error = {
            response: {
                status: 500,
                statusText: "Internal Server Error",
                data: {
                    details: "Error id 48b01e9e, java.lang.NullPointerException",
                    stack: "org.jboss.resteasy.spi.UnhandledException: ...",
                },
            },
        };

        expect(describeLoginError(error))
            .toBe("500 Internal Server Error: Error id 48b01e9e, java.lang.NullPointerException");
    });

    it("passes a plain text body through", () => {
        const error = {
            response: {status: 401, statusText: "Unauthorized", data: "User 'bob' not found!"},
        };

        expect(describeLoginError(error)).toBe("401 Unauthorized: User 'bob' not found!");
    });

    it("reports the status alone when the body says nothing", () => {
        const error = {response: {status: 502, statusText: "Bad Gateway", data: ""}};

        expect(describeLoginError(error)).toBe("502 Bad Gateway");
    });

    it("falls back to the transport error when there was no response at all", () => {
        const error = {code: "ERR_NETWORK", message: "Network Error"};

        expect(describeLoginError(error)).toBe("ERR_NETWORK Network Error");
    });
});
