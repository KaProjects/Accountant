import axios from "axios";
import {properties} from "../properties";

/**
 * Every request carries the session, which the browser holds as an HttpOnly cookie and attaches
 * by itself. Nothing in the page can read it, which is the point: a token kept in session storage
 * is readable by any script that gets injected into the page, and this one is not.
 */
export const credentialed = {withCredentials: true}

/**
 * A request that changes something has to prove it came from this application's own page.
 *
 * The browser attaches the session cookie whether or not the page that caused the request belongs
 * here, so the backend also asks for a header: another site's form cannot set one, and another
 * site's script has to ask permission first, through a preflight this application never grants.
 */
export const CLIENT_HEADER = "X-Accountant-Client"

export const mutatingJson = {
    ...credentialed,
    headers: {"Content-Type": "application/json", [CLIENT_HEADER]: "web"},
}

/**
 * Asks the backend whether the caller still has a session.
 *
 * The page cannot tell by looking, because it cannot see the cookie. It therefore asks, once, on
 * startup - which also covers the cases a stored token used to get wrong: a session the backend
 * has forgotten because it restarted, and one that has simply expired.
 */
export const hasSession = async () => {
    try {
        await axios.get(properties.backend + "/authenticate", credentialed)
        return true
    } catch (error) {
        return false
    }
}
