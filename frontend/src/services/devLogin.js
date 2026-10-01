import axios from "axios";
import {properties} from "../properties";
import {mutatingJson} from "./session";

/**
 * The throwaway pair in backend/src/dev/resources/dev-users.json, which exists only on the
 * development classpath. Overridable so that changing that file does not mean editing this one.
 */
const USERNAME = process.env.REACT_APP_DEV_USER || "dev"
const PASSWORD = process.env.REACT_APP_DEV_PASSWORD || "dev"

export const isDevelopment = () => process.env.NODE_ENV === "development"

/**
 * Logs a development build in without anybody typing the credentials.
 *
 * The backend requires a real token from every caller, in development exactly as in production,
 * and has no switch to turn that off. That is deliberate: a bypass is a code path that can end up
 * enabled where it should not be, and the last one both hid unprotected endpoints and stopped the
 * native production binary from starting. Doing the login here instead leaves the rule every
 * endpoint enforces completely untouched - this only saves the typing.
 *
 * @return whether a session was established. False when this is not a development build, or when
 * the backend refused - which is the normal answer when a development frontend is pointed at a
 * real backend, and then the ordinary login screen is shown.
 */
export const devLogin = async () => {
    if (!isDevelopment()) return false

    try {
        await axios.post(
            properties.backend + "/authenticate",
            {username: USERNAME, password: PASSWORD},
            mutatingJson)
        return true
    } catch (error) {
        console.info("development auto-login was refused, showing the login screen")
        return false
    }
}
