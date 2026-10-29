// properties.js reads window.location and the build-time backend port when the
// module is first evaluated, so each case re-imports it with a fresh environment.

const loadProperties = () => {
    let loaded;
    jest.isolateModules(() => {
        loaded = require("../properties");
    });
    return loaded;
};

describe("properties", () => {
    const originalEnv = process.env.REACT_APP_BACKEND_PORT;

    afterEach(() => {
        if (originalEnv === undefined) {
            delete process.env.REACT_APP_BACKEND_PORT;
        } else {
            process.env.REACT_APP_BACKEND_PORT = originalEnv;
        }
    });

    it("reaches the API through a same-origin /api prefix", () => {
        // nothing host-specific may be baked into the bundle - nginx and
        // setupProxy.js are what route /api to the backend
        expect(loadProperties().properties.backend).toBe("/api");
        expect(loadProperties().backend).toBe("/api");
    });

    it("builds the swagger link from the current origin and the configured port", () => {
        process.env.REACT_APP_BACKEND_PORT = "7701";

        expect(loadProperties().properties.apiDocsUrl)
            .toBe(`${window.location.protocol}//${window.location.hostname}:7701/api/docs`);
    });

    it("falls back to the development backend port when none is configured", () => {
        delete process.env.REACT_APP_BACKEND_PORT;

        expect(loadProperties().properties.apiDocsUrl)
            .toBe(`${window.location.protocol}//${window.location.hostname}:9091/api/docs`);
    });

    it("points the swagger link at the backend directly, not through the proxy", () => {
        process.env.REACT_APP_BACKEND_PORT = "7701";
        const {properties} = loadProperties();

        // the backend serves swagger at its own /api/docs, so routing it through
        // the /api proxy would resolve to /api/api/docs
        expect(properties.apiDocsUrl.startsWith(properties.backend)).toBe(false);
        expect(properties.apiDocsUrl).toContain(":7701/api/docs");
    });
});
