export const backend = "/api"

const backendPort = process.env.REACT_APP_BACKEND_PORT || "9091"

export const properties = {
    backend: backend,
    apiDocsUrl: `${window.location.protocol}//${window.location.hostname}:${backendPort}/api/docs`,
}
